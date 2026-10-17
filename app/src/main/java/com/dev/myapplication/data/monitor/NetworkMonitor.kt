package com.dev.myapplication.data.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkState(
    val isConnected: Boolean = false,
    val transportType: String = "NONE", // WIFI, CELLULAR, ETHERNET, NONE
    val isMetered: Boolean = false,
    val linkDownstreamKbps: Int = 0,
    val linkUpstreamKbps: Int = 0
)

class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkCapabilities(connectivityManager.getNetworkCapabilities(network))
        }

        override fun onLost(network: Network) {
            _networkState.value = NetworkState()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            updateNetworkCapabilities(networkCapabilities)
        }
    }

    init {
        val activeNetwork = connectivityManager.activeNetwork
        val initialCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        updateNetworkCapabilities(initialCapabilities)

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        try {
            connectivityManager.registerNetworkCallback(request, networkCallback)
        } catch (_: Exception) {}
    }

    private fun updateNetworkCapabilities(capabilities: NetworkCapabilities?) {
        if (capabilities == null) {
            _networkState.value = NetworkState()
            return
        }

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val transport = when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
            else -> "OTHER"
        }

        val isMetered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        val downKbps = capabilities.linkDownstreamBandwidthKbps
        val upKbps = capabilities.linkUpstreamBandwidthKbps

        _networkState.value = NetworkState(
            isConnected = hasInternet,
            transportType = transport,
            isMetered = isMetered,
            linkDownstreamKbps = downKbps,
            linkUpstreamKbps = upKbps
        )
    }

    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {}
    }
}
