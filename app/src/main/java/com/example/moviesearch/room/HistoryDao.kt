package com.example.moviesearch.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM History")
    fun getAll(): List<History>

    @Query(
        "SELECT searchWord FROM History " +
                "WHERE dateTime IN (SELECT MAX(dateTime)" +
                                    "FROM History " +
                                    "GROUP BY searchWord) " +
                                    "ORDER BY dateTime DESC " +
                                    "LIMIT :limit"
    )
    fun getSearchHistory(limit: Int): List<String>

    @Insert
    fun insertAll(vararg users: History)

    @Delete
    fun delete(user: History)

    @Insert
    fun insertSearchHistory(HistoryDTO: History)
}