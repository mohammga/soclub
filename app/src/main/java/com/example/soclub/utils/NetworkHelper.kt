package com.example.soclub.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue



/**
 * A utility class to monitor and check the network connectivity status.
 *
 * This class provides real-time updates on the network connectivity status and
 * ensures that the state is updated whenever the connection is established or lost.
 *
 * @param context The application context used to retrieve the ConnectivityManager system service.
 */
class NetworkHelper(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * A mutable state that indicates whether there is an active internet connection.
     * This property is observable and can be used in Compose UI for reactive updates.
     */
    var hasInternetConnection by mutableStateOf(isNetworkAvailable())
        private set


    init {
        monitorNetworkConnection()
    }

    /**
     * Checks if the network is currently available.
     *
     * This method determines whether there is an active network and if it has internet capabilities.
     *
     * @return `true` if there is an active network with internet capability, `false` otherwise.
     */
    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Monitors the network connection status by registering a network callback.
     *
     * This method listens for changes in network availability and updates the
     * [hasInternetConnection] property accordingly.
     */
    private fun monitorNetworkConnection() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            /**
             * Called when a network becomes available.
             *
             * @param network The network that became available.
             */
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                hasInternetConnection = true
            }

            /**
             * Called when a network is lost.
             *
             * @param network The network that was lost.
             */
            override fun onLost(network: Network) {
                super.onLost(network)
                hasInternetConnection = false
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }
}

