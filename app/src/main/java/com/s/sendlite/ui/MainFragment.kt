@file:Suppress("DEPRECATION")

package com.s.sendlite.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pDeviceList
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.s.sendlite.R
import com.s.sendlite.WifiDirectBroadcastReceiver
import com.s.sendlite.socket.ReceiverThread
import com.s.sendlite.socket.SenderThread
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

@Suppress("BlockingMethodInNonBlockingContext", "DEPRECATION")
class MainFragment : Fragment(),KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: MainModelFactory by instance()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)
    }

    private val intentFilter = IntentFilter()
    private var broadcastReceiver = WifiDirectBroadcastReceiver()
    private val deviceNameArray = mutableListOf<String>()
    private val deviceArray = mutableListOf<WifiP2pDevice>()

    private lateinit var receiver: ReceiverThread
    private lateinit var socket: Socket
    private val config = WifiP2pConfig()

    private lateinit var imageUri: Uri

    companion object {
        fun newInstance() = MainFragment()
        private lateinit var fragment: Fragment
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
        context.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDetach() {
        super.onDetach()
        this.activity!!.unregisterReceiver(broadcastReceiver)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fragment = this
        //val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)


        broadcastReceiver.deviceList.observe(this, Observer<WifiP2pDeviceList> {
            deviceNameArray.clear()
            deviceArray.clear()
            for (device in it.deviceList) {
                deviceNameArray.add(device.deviceName)
                deviceArray.add(device)
            }
            val adapter =
                ArrayAdapter(this.context!!, android.R.layout.simple_list_item_1, deviceNameArray)
            list_view.adapter = adapter
        })

        broadcastReceiver.member.observe(this, Observer<String> {
            if (it == "GroupOwner")
                server()
            else
                client(broadcastReceiver.address)
            connection_status.text = it
        })

        btn_permission.setOnClickListener {
            getPermission(this.activity!!)
        }

        btn_wifi_switch.setOnClickListener {
            enableWifi(this.activity!!.application)
        }

        btn_discover.setOnClickListener {
            broadcastReceiver.discoverPeers()
        }

        list_view.setOnItemClickListener { _, _, i, _ ->
            config.apply {
                deviceAddress = deviceArray[i].deviceAddress
                wps.setup = WpsInfo.PBC
            }
            broadcastReceiver.connectWith(config)
        }

        btn_send.setOnClickListener {
            val senderThread = SenderThread(context!!, socket, imageUri)
            senderThread.start()
            senderThread.byteSent.observe(fragment, Observer<Long> {
                bytes_text.text = it.toString()
            })
            senderThread.status.observe(fragment, Observer<String> {
                status_text.text = it
            })
        }

        btn_choose.setOnClickListener {
            chooseFile()
        }
    }

    private fun chooseFile() {
        val intent = Intent()
        intent.type = "*/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "select file"), 36)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 36 && resultCode == AppCompatActivity.RESULT_OK) {
            imageUri = data?.data!!
        }
    }

    private fun getPermission(activity: FragmentActivity) {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )

        } else {
            Toast.makeText(this.context, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    println("TAAAG granted")
                else {
                    println("TAAAG not granted")
                }
            }
        }
    }

    private fun enableWifi(applicationContext: Application) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        } else
            Toast.makeText(this.context, "wifi Already on", Toast.LENGTH_SHORT).show()
    }

    private fun server() {
        CoroutineScope(Dispatchers.IO).launch {
            val server = ServerSocket(12327)
            socket = server.accept()

            withContext(Dispatchers.Main) {
                receiver = ReceiverThread(socket, context!!)
                receiver.start()

                receiver.bytesReceived.observe(fragment, Observer<Long> {
                    bytes_text.text = it.toString()
                })

                receiver.status.observe(fragment, Observer<String> {
                    status_text.text = it
                })
            }
        }
    }

    private fun client(hostAddress: InetAddress) {
        CoroutineScope(Dispatchers.IO).launch {
            socket = Socket(hostAddress, 12327)
            withContext(Dispatchers.Main) {
                receiver = ReceiverThread(socket, context!!)
                receiver.start()

                receiver.bytesReceived.observe(fragment, Observer {
                    bytes_text.text = it.toString()
                })

                receiver.status.observe(fragment, Observer {
                    status_text.text = it
                })
            }
        }
    }
}