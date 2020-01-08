package com.s.sendlite.dataClass

import android.net.Uri

data class Photo(
    val uri: Uri,
    val name: String,
    val size: Int
)