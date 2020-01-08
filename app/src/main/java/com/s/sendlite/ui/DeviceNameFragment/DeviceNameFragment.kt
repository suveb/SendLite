package com.s.sendlite.ui.DeviceNameFragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.s.sendlite.R
import kotlinx.android.synthetic.main.device_name_fragment.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class DeviceNameFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: DeviceNameModelFactory by instance()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(DeviceNameViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.device_name_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("local", Context.MODE_PRIVATE)!!
        edt_device_name.setText(sharedPref.getString("DeviceName", ""))

        btn_device_name.setOnClickListener {
            if (edt_device_name.text.toString() == "")
                Toast.makeText(this.context, "Name Cannot be empty", Toast.LENGTH_SHORT).show()
            else {
                with(sharedPref.edit()) {
                    putString("DeviceName", edt_device_name.text.toString())
                    apply()
                }

                Toast.makeText(this.context, "Name changed successfully", Toast.LENGTH_SHORT).show()

                if (sharedPref.getBoolean("FirstTime", true)) {
                    with(sharedPref.edit()) {
                        putBoolean("FirstTime", false)
                        apply()
                    }
                    findNavController().navigate(R.id.action_deviceNameFragment_to_dashboardFragment)
                } else
                    findNavController().navigate(R.id.action_deviceNameFragment_to_settingsFragment)
            }
        }
    }
}