package com.s.sendlite.ui.DeviceNameFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.s.sendlite.dataClass.Repository

@Suppress("UNCHECKED_CAST")
class DeviceNameModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DeviceNameViewModel(repository) as T
    }
}