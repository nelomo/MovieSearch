package com.example.moviesearch.room

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository private constructor() {
    companion object {
        private var instance: AppRepository? = null
        private lateinit var appDatabase: AppDatabase

        fun getInstance(application: Application): AppRepository =
            instance ?: synchronized(this) {
                instance ?: AppRepository().also {
                    instance = it
                    appDatabase = Room.databaseBuilder(application, AppDatabase::class.java, "LocalDB").build()
                }
            }
    }

    suspend fun getSearchHistory(limit: Int): List<String> = withContext(Dispatchers.IO) {
        appDatabase.historyDao().getSearchHistory(limit)
    }

    suspend fun insertSearchHistory(searchHistoryDTO: History) = withContext(Dispatchers.IO) {
        appDatabase.historyDao().insertSearchHistory(searchHistoryDTO)
    }
}