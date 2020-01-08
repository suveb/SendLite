package com.s.sendlite

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import androidx.lifecycle.MutableLiveData

class WifiDirectMethodsImpl(private val application: Application) : WifiDirectMethods,
    BroadcastReceiver() {

    val wifiStatus = MutableLiveData<Boolean>(false)
    val discoverPeerStatus = MutableLiveData<Boolean>(false)
    val availableDeviceList = MutableLiveData<WifiP2pDeviceList>()
    val hostAddress = MutableLiveData<String>()
    val memberType = MutableLiveData<String>("none")

    private val manager = application.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(application, application.mainLooper, null)

    fun turnWifiOn() {
        super.IturnWifiOn(application)
    }

    fun turnWifiOff() {
        super.IturnWifiOff(application)
    }

    fun startDiscoverPeer() {
        super.IstartDiscoverPeer(application, manager, channel)
    }

    fun hotspotState() = IhotspotState(manager)

    fun connectTo(device: WifiP2pDevice) {
        super.IconnectTo(device, application, manager, channel)
    }

    fun changeDeviceName(name: String) {
        super.IchangeDeviceName(name, manager, channel)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {

            //Check Wifi On/Off State
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    wifiStatus.postValue(true)
                    println("TAAAG wifi is on")
                } else {
                    wifiStatus.postValue(false)
                    println("TAAAG wifi is off")
                }
            }

            //Triggered When we start discovery
            WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION -> {
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1)
                if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                    discoverPeerStatus.postValue(true)
                    println("TAAAG Discovery Started")
                } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                    discoverPeerStatus.postValue(false)
                    println("TAAAG Discovery Stopped")
                }
            }

            //Update on Every New Device List
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                println("TAAAG Discovery Completed")
                manager.requestPeers(channel) {
                    availableDeviceList.postValue(it)
                }
            }

            //Triggered after we connect to a device
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                println("TAAAG ConnectionChanged Completed")
                manager.requestConnectionInfo(channel) {
                    if (it.groupFormed && it.isGroupOwner) {
                        memberType.postValue("Server")
                        hostAddress.postValue(it.groupOwnerAddress.hostAddress)
                    } else if (it.groupFormed) {
                        memberType.postValue("Client")
                        hostAddress.postValue(it.groupOwnerAddress.hostAddress)
                    } else {
                        memberType.postValue("none")
                    }
                }
            }
        }
    }
}