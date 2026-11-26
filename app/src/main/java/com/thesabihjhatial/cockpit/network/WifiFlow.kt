package com.thesabihjhatial.cockpit.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

private const val UnknownSsid = "<unknown ssid>"

@Suppress("DEPRECATION")
private fun networkName(context: Context): String? =
    context.applicationContext.getSystemService(WifiManager::class.java)?.connectionInfo?.ssid
        ?.removeSurrounding("\"")
        ?.takeUnless { it.isEmpty() || it == UnknownSsid }

fun wifiStatusFlow(context: Context): Flow<WifiStatus> = callbackFlow {
    val manager = context.getSystemService(ConnectivityManager::class.java)
    val networks = ConcurrentHashMap.newKeySet<Network>()

    fun publish() {
        trySend(WifiStatus(networks.isNotEmpty(), if (networks.isNotEmpty()) networkName(context) else null))
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            networks.add(network)
            publish()
        }

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            publish()
        }

        override fun onLost(network: Network) {
            networks.remove(network)
            publish()
        }
    }
    val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    manager.registerNetworkCallback(request, callback)
    awaitClose { manager.unregisterNetworkCallback(callback) }
}
