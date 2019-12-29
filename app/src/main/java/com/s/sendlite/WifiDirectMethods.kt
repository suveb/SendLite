package com.s.sendlite

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import java.lang.reflect.Method

interface WifiDirectMethods {

    fun IturnWifiOn(application: Application) {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }

    fun IturnWifiOff(application: Application) {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }

    fun IstartDiscoverPeer(
        application: Application,
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel
    ) {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(application, "discoverPeers Start Success", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(application, "discoverPeers Start Failure", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    fun IconnectTo(
        device: WifiP2pDevice,
        application: Application,
        manager: WifiP2pManager,
        channel: WifiP2pManager.Channel
    ) {
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(application, "request send successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(application, "request send failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun IchangeDeviceName(name: String, manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        val paramTypes: Array<Class<*>?> = arrayOfNulls(3)
        paramTypes[0] = WifiP2pManager.Channel::class.java
        paramTypes[1] = String::class.java
        paramTypes[2] = WifiP2pManager.ActionListener::class.java
        val setDeviceName: Method = manager.javaClass.getMethod("setDeviceName", *paramTypes)
        setDeviceName.isAccessible = true
        setDeviceName.invoke(manager, channel, name, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                print("TAAAG changeName Success")
            }

            override fun onFailure(reason: Int) {
                print("TAAAG changeName Failure")
            }
        })
    }
}