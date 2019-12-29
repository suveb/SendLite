package com.s.sendlite.ui.AvailableDeviceFragment

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
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
import com.s.sendlite.WifiDirectMethodsImpl
import kotlinx.android.synthetic.main.available_device_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AvailableDeviceFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val broadcastReceiver: WifiDirectMethodsImpl by instance()
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
        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!

        broadcastReceiver.availableDeviceList.observe(this, Observer {
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

        broadcastReceiver.memberType.observe(this, Observer {
            if (it != "none")
                findNavController().navigate(R.id.action_availableDeviceFragment_to_connectedFragment)
        })

        list_avail_devices.setOnItemClickListener { _, _, i, _ ->
            broadcastReceiver.connectTo(deviceArray[i])
        }

        btn_discover.setOnClickListener {
            val wifiManager =
                this.activity!!.application.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (wifiManager.isWifiEnabled) {
                broadcastReceiver.changeDeviceName(sharedPref.getString("DeviceName", "NoName")!!)
                broadcastReceiver.startDiscoverPeer()
            } else {
                Toast.makeText(this.context, "Please Turn Wifi On", Toast.LENGTH_SHORT).show()
            }
        }

        broadcastReceiver.turnWifiOn()
    }
}