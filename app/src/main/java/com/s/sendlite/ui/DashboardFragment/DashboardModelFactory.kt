package com.s.sendlite.ui.DashboardFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.s.sendlite.dataClass.Repository

@Suppress("UNCHECKED_CAST")
class DashboardModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}