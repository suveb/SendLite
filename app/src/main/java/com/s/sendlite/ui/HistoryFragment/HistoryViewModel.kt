package com.s.sendlite.ui.HistoryFragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s.sendlite.dataClass.Repository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: Repository)  : ViewModel() {

    val data = MutableLiveData<String>()
    fun show() {
        viewModelScope.launch { data.postValue(repository.getHistory().toString()) }
    }
}
