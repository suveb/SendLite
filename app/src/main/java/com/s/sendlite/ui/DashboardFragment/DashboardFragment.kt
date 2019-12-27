package com.s.sendlite.ui.DashboardFragment

import android.app.Application
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.s.sendlite.R
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class DashboardFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: DashboardModelFactory by instance()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(DashboardViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dashboard_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!
        if(sharedPref.getBoolean("FirstTime", true)){
            findNavController().navigate(R.id.action_dashboardFragment_to_deviceNameFragment)
        }

        btn_next.setOnClickListener {
                findNavController().navigate(R.id.action_dashboardFragment_to_availableDeviceFragment)
        }

        disableWifi(this.activity!!.application)
    }

    private fun disableWifi(application: Application) {
        val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = false
        }
    }
}