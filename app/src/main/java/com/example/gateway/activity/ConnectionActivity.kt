package com.example.gateway.activity

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gateway.R
import com.example.gateway.utils.NetworkUtils
import kotlinx.android.synthetic.main.activity_connection.*


class ConnectionActivity : AppCompatActivity() {

    private var connectivityManager: ConnectivityManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)
        checkBluetoothStatus()
        updateWIFIStatusUI(NetworkUtils.isNetworkConnected(applicationContext))
    }

    override fun onResume() {
        super.onResume()
        checkWifiConnectionStatus()
        registerReceiver(
            this.mBluetoothConnectionReceiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            "",
            null
        )
    }

    private fun checkWifiConnectionStatus() {
        try {
            val networkRequest =
                NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            connectivityManager =
                this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager!!.requestNetwork(networkRequest, mNetworkChangeCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mNetworkChangeCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                updateWIFIStatusUI(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                updateWIFIStatusUI(false)
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(this.mBluetoothConnectionReceiver)
        if (connectivityManager != null) {
            connectivityManager!!.unregisterNetworkCallback(mNetworkChangeCallback)
        }
    }

    // Broadcast receiver for the bluetooth connection status info
    private val mBluetoothConnectionReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            try {
                val action = intent.action
                if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    when (state) {
                        BluetoothAdapter.STATE_OFF -> {
                            updateBluetoothStatusUI(false)
                        }
                        BluetoothAdapter.STATE_ON -> {
                            updateBluetoothStatusUI(true)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // checkBluetoothEnableOrNot
    private fun checkBluetoothStatus() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter != null) {
            updateBluetoothStatusUI(mBluetoothAdapter.isEnabled)
        }
    }

    private fun updateBluetoothStatusUI(isOn: Boolean) {
        if (isOn) {
            runOnUiThread {
                itemTvBluetoothStatus.setText(R.string.str_on)
            }
        } else {
            runOnUiThread {
                itemTvBluetoothStatus.setText(R.string.str_off)
            }
        }
    }
    private fun updateWIFIStatusUI(isOn: Boolean) {
        if (isOn) {
            runOnUiThread {
                itemTvWifiStatus.setText(R.string.str_on)
            }
        } else {
            runOnUiThread {
                itemTvWifiStatus.setText(R.string.str_off)
            }
        }
    }
}
