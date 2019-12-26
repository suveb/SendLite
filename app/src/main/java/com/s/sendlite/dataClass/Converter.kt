package com.s.sendlite.dataClass

import androidx.room.TypeConverter
import java.util.*

class Converter {
    companion object{
        @TypeConverter
        @JvmStatic
        fun fromTimeStamp(value: Long) = Date(value)

        @TypeConverter
        @JvmStatic
        fun toTimeStamp(date: Date) = date.time
    }
}

