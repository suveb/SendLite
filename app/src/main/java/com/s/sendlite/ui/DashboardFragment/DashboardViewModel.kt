package com.s.sendlite.ui.DashboardFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s.sendlite.dataClass.History
import com.s.sendlite.dataClass.Repository
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(private val repository: Repository) : ViewModel() {
    fun insertValue(name: String) {
        val fileInfo = History()
        fileInfo.apply {
            fileName = name
            fileSize = "0MB"
            fileType = "ot"
            fileLocation = "emu/0"
            status = "f"
            senderName = "s"
            receiverName = ""
            dateReceived = Date(System.currentTimeMillis())
        }
        viewModelScope.launch {
            repository.insertFile(fileInfo)
        }
    }
}
