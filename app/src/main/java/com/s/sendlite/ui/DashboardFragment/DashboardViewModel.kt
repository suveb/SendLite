package com.s.sendlite.ui.DashboardFragment

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.s.sendlite.dataClass.Repository

class DashboardViewModel(private val repository: Repository) : ViewModel() {
    fun getPermissions(activity: Activity): Boolean {
        return if (!checkPermission(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1
            )
            false
        } else true
    }

    private fun checkPermission(activity: Activity) = !((ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(
        activity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) != PackageManager.PERMISSION_GRANTED))
}
