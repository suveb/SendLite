package com.s.sendlite

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import java.lang.reflect.Method
import java.net.InetAddress


@SuppressLint("SetTextI18n")
class WifiDirectBroadcastReceiver : BroadcastReceiver() {

    val deviceList = MutableLiveData<WifiP2pDeviceList>()
    val member = MutableLiveData<String>()
    lateinit var address: InetAddress

    private lateinit var context: Context

    private val manager by lazy {
        context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    }

    private val channel by lazy {
        manager.initialize(context, context.mainLooper, null)
    }

    fun changeName(deviceName: String) {
        val paramTypes: Array<Class<*>?> = arrayOfNulls(3)
        paramTypes[0] = WifiP2pManager.Channel::class.java
        paramTypes[1] = String::class.java
        paramTypes[2] = WifiP2pManager.ActionListener::class.java
        val setDeviceName: Method = manager.javaClass.getMethod("setDeviceName", *paramTypes)
        setDeviceName.isAccessible = true
        setDeviceName.invoke(manager, channel, deviceName, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                print("TAAAG changeName Success")
            }

            override fun onFailure(reason: Int) {
                print("TAAAG changeName Failure")
            }
        })
    }

    fun getConnectionInfo(){}

    fun discoverPeers() {
        manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(context, "discoverPeers Start Success", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(context, "discoverPeers Start Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun connectWith(device: WifiP2pDevice): Boolean {
        val config = WifiP2pConfig().apply { deviceAddress = device.deviceAddress }
        var status = false
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                status = true
                Toast.makeText(context, "request send successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {
                status = false
                Toast.makeText(context, "request send failed $p0", Toast.LENGTH_SHORT).show()
            }
        })

        return status
    }

    override fun onReceive(context: Context, intent: Intent?) {
        this.context = context

        when (intent?.action) {

            //Check Wifi On/Off State
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    println("TAAAG wifi is on")
                } else {
                    println("TAAAG wifi is off")
                }
            }

            //Triggered When we start discovery
            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    println("TAAAG Discovery Started")
                } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                    println("TAAAG Discovery Stopped")
                }
            }

            //Update on Every New Device List
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                println("TAAAG Discovery Completed")
                manager.requestPeers(channel) {
                    deviceList.postValue(it)
                }
            }

            //Triggered after we connect to a device
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                manager.requestConnectionInfo(channel) {
                    if (it.groupFormed && it.isGroupOwner) {
                        member.postValue("GroupOwner")
                    } else if (it.groupFormed) {
                        member.postValue("GroupMember")
                        address = it.groupOwnerAddress
                    }
                }
            }
        }
    }
}