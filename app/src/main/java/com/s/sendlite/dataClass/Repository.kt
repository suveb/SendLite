package com.s.sendlite.dataClass

class Repository(private val localDatabase: Query) {
    suspend fun insertFile(history: History) {
        localDatabase.insertFile(history)
    }

    suspend fun getHistory(): List<History> {
        return localDatabase.getHistory()
    }
}