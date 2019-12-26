package com.s.sendlite.ui.AvailableDeviceFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders

import com.s.sendlite.R
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AvailableDeviceFragment : Fragment(), KodeinAware {
    override val kodein by closestKodein()

    private val viewModelFactory: AvailableDeviceModelFactory by instance()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(AvailableDeviceViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.available_device_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}
