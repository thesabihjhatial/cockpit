package com.thesabihjhatial.cockpit.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private const val A2dpSinkProfile = 11
private const val AvrcpControllerProfile = 12
private const val HeadsetClientProfile = 16
private const val PbapClientProfile = 17
private const val SettleMs = 500L

private val AudioTypes = setOf(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO)

private val ConnectionActions = listOf(
    "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED",
    "android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED",
    "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED",
    "android.bluetooth.pbapclient.profile.action.CONNECTION_STATE_CHANGED",
    BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
    BluetoothAdapter.ACTION_STATE_CHANGED,
    BluetoothDevice.ACTION_ACL_CONNECTED,
    BluetoothDevice.ACTION_ACL_DISCONNECTED,
    BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
)

private val ConnectionProfiles = listOf(
    A2dpSinkProfile,
    AvrcpControllerProfile,
    BluetoothProfile.A2DP,
    BluetoothProfile.HEADSET,
    HeadsetClientProfile,
    PbapClientProfile,
)

@SuppressLint("MissingPermission")
fun bluetoothStatusFlow(context: Context): Flow<BluetoothStatus> = callbackFlow {
    val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
    if (adapter == null) {
        trySend(BluetoothStatus.Disconnected)
        awaitClose()
        return@callbackFlow
    }

    val audio = context.getSystemService(AudioManager::class.java)
    val links = ConcurrentHashMap<String, String>()
    val proxies = ConcurrentHashMap<Int, BluetoothProfile>()
    var settling: Job? = null

    fun audioName(): String? = audio.getDevices(AudioManager.GET_DEVICES_ALL)
        .firstOrNull { it.type in AudioTypes }
        ?.productName?.toString()

    fun bondedName(): String? = adapter.bondedDevices.orEmpty().firstOrNull { it.isLinkUp() }?.displayName()

    fun profileConnected(): Boolean = ConnectionProfiles.any {
        runCatching { adapter.getProfileConnectionState(it) == BluetoothProfile.STATE_CONNECTED }.getOrDefault(false)
    }

    fun profileName(): String? = proxies.values.flatMap { it.connectedDevices }.firstOrNull()?.displayName()

    fun publish() {
        if (!adapter.isEnabled) {
            trySend(BluetoothStatus.Disconnected)
            return
        }
        val name = profileName() ?: links.values.firstOrNull() ?: bondedName() ?: audioName()
        trySend(
            when {
                name != null -> BluetoothStatus.Connected(name)
                profileConnected() -> BluetoothStatus.Connected("")
                else -> BluetoothStatus.Disconnected
            },
        )
    }

    fun settle() {
        settling?.cancel()
        settling = launch {
            delay(SettleMs)
            publish()
        }
    }

    val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            proxies[profile] = proxy
            publish()
        }

        override fun onServiceDisconnected(profile: Int) {
            proxies.remove(profile)
            publish()
        }
    }

    fun release() {
        proxies.forEach { (profile, proxy) -> adapter.closeProfileProxy(profile, proxy) }
        proxies.clear()
    }

    fun connect() {
        release()
        links.clear()
        ConnectionProfiles.forEach { adapter.getProfileProxy(context, serviceListener, it) }
        publish()
    }

    val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            val device = IntentCompat.getParcelableExtra(intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> connect()
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    device?.let { links[it.address] = it.displayName() }
                    settle()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    device?.let { links.remove(it.address) }
                    settle()
                }
                else -> settle()
            }
        }
    }
    val audioCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            settle()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            settle()
        }
    }

    val filter = IntentFilter().apply { ConnectionActions.forEach(::addAction) }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    audio.registerAudioDeviceCallback(audioCallback, null)
    connect()
    awaitClose {
        context.unregisterReceiver(receiver)
        audio.unregisterAudioDeviceCallback(audioCallback)
        release()
    }
}

@SuppressLint("MissingPermission")
private fun BluetoothDevice.displayName(): String =
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) alias else null) ?: name ?: address

private fun BluetoothDevice.isLinkUp(): Boolean = runCatching {
    BluetoothDevice::class.java.getMethod("isConnected").invoke(this) as Boolean
}.getOrDefault(false)
