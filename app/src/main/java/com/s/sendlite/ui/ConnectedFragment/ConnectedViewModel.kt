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

    fun calculatePercentage(fileSize: Long, received: Long) = ((received.toFloat()/fileSize)*100).toInt()

    fun sizeReceived(size : Long) : String{
        if (size<1024)
            return size.toString()+"Bytes"
        else if (size<1024*1024)
            return "%.2f".format((size/(1024f)))+"KB"
        else
            return "%.2f".format(size/(1024*1024f))+"MB"
    }
}