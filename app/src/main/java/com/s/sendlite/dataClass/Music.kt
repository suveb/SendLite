package com.s.sendlite.dataClass

import android.net.Uri

data class Music(
    val uri: Uri,
    val name: String,
    val duration: Int,
    val size: Int
)