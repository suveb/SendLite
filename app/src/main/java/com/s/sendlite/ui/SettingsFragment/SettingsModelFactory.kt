package com.s.sendlite.ui.SettingsFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.s.sendlite.dataClass.Repository

@Suppress("UNCHECKED_CAST")
class SettingsModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}