package com.s.sendlite.dataClass

import androidx.room.TypeConverter
import java.util.*

class Converter {
    companion object{
        @TypeConverter
        @JvmStatic
        fun fromTimeStamp(value: Long?) = value?.let { Date(it) }

        @TypeConverter
        @JvmStatic
        fun toTimeStamp(date: Date?) = date?.time
    }
}

