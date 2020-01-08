package com.s.sendlite.ui.DashboardFragment

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.s.sendlite.R
import com.s.sendlite.WifiDirectMethodsImpl
import kotlinx.android.synthetic.main.dashboard_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class DashboardFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()
    private val broadcastReceiver: WifiDirectMethodsImpl by instance()
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
        if (sharedPref.getBoolean("FirstTime", true)) {
            findNavController().navigate(R.id.action_dashboardFragment_to_deviceNameFragment)
        }

        btn_next.setOnClickListener {
            if (viewModel.getPermissions(this.activity!!))
                findNavController().navigate(R.id.action_dashboardFragment_to_availableDeviceFragment)
            else
                Toast.makeText(
                    this.context,
                    "Need Permission to Work properly",
                    Toast.LENGTH_SHORT
                ).show()
        }

        viewModel.getPermissions(this.activity!!)
        broadcastReceiver.turnWifiOff()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                Toast.makeText(
                    this.context,
                    "This App Cannot Work Without Permission",
                    Toast.LENGTH_SHORT
                ).show()
        }
    }
}