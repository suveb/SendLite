package com.s.sendlite.ui.ConnectedFragment

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import androidx.lifecycle.ViewModel
import com.s.sendlite.dataClass.Repository

class ConnectedViewModel(private val repository: Repository) : ViewModel() {

    fun disableWifi(application: Application) {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }
}