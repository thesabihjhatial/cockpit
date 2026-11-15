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
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

private const val A2dpSinkProfile = 11
private const val HeadsetClientProfile = 16
private const val SettleMs = 500L

private val ConnectionActions = listOf(
    "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED",
    "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED",
    BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED,
    BluetoothAdapter.ACTION_STATE_CHANGED,
    BluetoothDevice.ACTION_ACL_CONNECTED,
    BluetoothDevice.ACTION_ACL_DISCONNECTED,
    BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED,
)

private val ConnectionProfiles = listOf(
    A2dpSinkProfile,
    BluetoothProfile.A2DP,
    BluetoothProfile.HEADSET,
    HeadsetClientProfile,
)

@SuppressLint("MissingPermission")
fun bluetoothStatusFlow(context: Context): Flow<BluetoothStatus> = callbackFlow {
    val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
    if (adapter == null) {
        trySend(BluetoothStatus.Disconnected)
        awaitClose()
        return@callbackFlow
    }

    val expected = AtomicInteger(Int.MAX_VALUE)
    val proxies = ConcurrentHashMap<Int, BluetoothProfile>()

    fun publish() {
        if (!adapter.isEnabled) {
            trySend(BluetoothStatus.Disconnected)
            return
        }
        if (proxies.size < expected.get()) return
        val device = proxies.values.flatMap { it.connectedDevices }.firstOrNull()
        trySend(
            device?.let { BluetoothStatus.Connected(it.displayName()) }
                ?: BluetoothStatus.Disconnected,
        )
    }

    val serviceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            proxies[profile] = proxy
            publish()
        }

        override fun onServiceDisconnected(profile: Int) {
            if (proxies.remove(profile) != null) expected.decrementAndGet()
            publish()
        }
    }

    fun release() {
        proxies.forEach { (profile, proxy) -> adapter.closeProfileProxy(profile, proxy) }
        proxies.clear()
    }

    fun connect() {
        release()
        expected.set(Int.MAX_VALUE)
        expected.set(ConnectionProfiles.count { adapter.getProfileProxy(context, serviceListener, it) })
        publish()
    }

    var settling: Job? = null
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(receiverContext: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                connect()
            } else {
                settling?.cancel()
                settling = launch {
                    delay(SettleMs)
                    publish()
                }
            }
        }
    }

    val filter = IntentFilter().apply { ConnectionActions.forEach(::addAction) }
    ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    connect()
    awaitClose {
        context.unregisterReceiver(receiver)
        release()
    }
}

@SuppressLint("MissingPermission")
private fun BluetoothDevice.displayName(): String =
    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) alias else null) ?: name ?: address
