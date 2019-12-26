package com.s.sendlite.ui.ConnectedFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.s.sendlite.dataClass.Repository

@Suppress("UNCHECKED_CAST")
class ConnectedModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConnectedViewModel(repository) as T
    }
}