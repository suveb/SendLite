package com.s.sendlite.ui.AvailableDeviceFragment

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.s.sendlite.R
import com.s.sendlite.WifiDirectBroadcastReceiver
import kotlinx.android.synthetic.main.available_device_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AvailableDeviceFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val broadcastReceiver: WifiDirectBroadcastReceiver by instance()
    private val viewModelFactory: AvailableDeviceModelFactory by instance()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AvailableDeviceViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.available_device_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val deviceNameArray = mutableListOf<String>()
        val deviceArray = mutableListOf<WifiP2pDevice>()
        val config = WifiP2pConfig()

        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!

        broadcastReceiver.deviceList.observe(this, Observer<WifiP2pDeviceList> {
            deviceNameArray.clear()
            deviceArray.clear()
            for (device in it.deviceList) {
                deviceNameArray.add(device.deviceName)
                deviceArray.add(device)
            }
            val adapter =
                ArrayAdapter(this.context!!, android.R.layout.simple_list_item_1, deviceNameArray)
            list_avail_devices.adapter = adapter
        })

        broadcastReceiver.member.observe(this, Observer<String> {
            if (it == "GroupOwner")
                findNavController().navigate(R.id.action_availableDeviceFragment_to_connectedFragment)
            else
                findNavController().navigate(
                    AvailableDeviceFragmentDirections.actionAvailableDeviceFragmentToConnectedFragment(
                        broadcastReceiver.address.hostAddress
                    )
                )
        })

        list_avail_devices.setOnItemClickListener { _, _, i, _ ->
            if (broadcastReceiver.connectWith(deviceArray[i]))
                Toast.makeText(this.activity, "Connection Rejected", Toast.LENGTH_SHORT).show()
        }

        btn_discover.setOnClickListener {
            val wifiManager =
                this.activity!!.application.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager.isWifiEnabled) {
                broadcastReceiver.changeName(sharedPref.getString("DeviceName", "NoName")!!)
            }
                broadcastReceiver.discoverPeers()
        }

        enableWifi(this.activity!!.application)
    }

    private fun enableWifi(application: Application) {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }
}