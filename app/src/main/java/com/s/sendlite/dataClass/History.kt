package com.s.sendlite.dataClass

import androidx.annotation.Keep
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
@Keep
data class History(
    @NonNull val fileName: String,
    @NonNull val fileSize: String,
    @NonNull @ColumnInfo(defaultValue = "Others") val fileType: String,
    @NonNull val fileLocation: String,
    @NonNull val status: String,
    @NonNull val senderName: String,
    @NonNull val receiverName: String,
    @PrimaryKey @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") val dateReceived: Date
)