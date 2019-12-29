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
    @NonNull var fileName: String = "",
    @NonNull var fileSize: String = "",
    @NonNull @ColumnInfo(defaultValue = "Others") var fileType: String = "",
    @NonNull var fileLocation: String = "",
    @NonNull var status: String = "",
    @NonNull var senderName: String = "",
    @NonNull var receiverName: String = "",
    @PrimaryKey @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP") var dateReceived: Date? = null
)