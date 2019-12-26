package com.s.sendlite.dataClass

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface Query {
    @Insert
    suspend fun insertFile(history: History)

    @Query("SELECT * FROM History ORDER BY dateReceived")
    suspend fun getHistory(): List<History>
}