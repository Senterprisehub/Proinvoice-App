package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class NetworkStatus(
    val isOnline: Boolean,
    val connectionType: String,
    val statusLabel: String
)

class NetworkConnectivityObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun observe(): Flow<NetworkStatus> = callbackFlow {
        val currentStatus = getCurrentStatus()
        trySend(currentStatus)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getCurrentStatus())
            }

            override fun onLost(network: Network) {
                trySend(getCurrentStatus())
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                trySend(getCurrentStatus())
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager?.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            trySend(NetworkStatus(isOnline = true, connectionType = "Local/Available", statusLabel = "Online"))
        }

        awaitClose {
            try {
                connectivityManager?.unregisterNetworkCallback(callback)
            } catch (_: Exception) {}
        }
    }.distinctUntilChanged()

    fun getCurrentStatus(): NetworkStatus {
        val cm = connectivityManager ?: return NetworkStatus(isOnline = false, connectionType = "Offline", statusLabel = "Offline Mode")
        val activeNetwork = cm.activeNetwork ?: return NetworkStatus(isOnline = false, connectionType = "Offline", statusLabel = "Offline Mode")
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStatus(isOnline = false, connectionType = "Offline", statusLabel = "Offline Mode")

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

        val type = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Connected"
        }

        return if (hasInternet) {
            NetworkStatus(isOnline = true, connectionType = type, statusLabel = "Online ($type)")
        } else {
            NetworkStatus(isOnline = false, connectionType = "Offline", statusLabel = "Offline Mode")
        }
    }
}
