package com.s.sendlite.dataClass

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import java.util.concurrent.locks.ReentrantLock

@Database(entities = arrayOf(History::class), version = 1)
@TypeConverters(Converter::class)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun query(): Query

    companion object {
        @Volatile
        private var instance: LocalDatabase? = null

        operator fun invoke(application: Application) = instance
            ?: synchronized(ReentrantLock()) {
                Room.databaseBuilder(application, LocalDatabase::class.java, "myDB").build()
            }
    }
}