package com.s.sendlite.ui.SplashFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.s.sendlite.dataClass.Repository


@Suppress("UNCHECKED_CAST")
class SplashModelFactory(private val repository: Repository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SplashViewModel(repository) as T
    }
}