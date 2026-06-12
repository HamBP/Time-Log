package me.algosketch.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.entity.LogSessionEntity

@Dao
interface LogSessionDao {
    @Query("SELECT * FROM log_sessions")
    fun getAll(): Flow<List<LogSessionEntity>>

    @Insert
    suspend fun insert(logSession: LogSessionEntity): Int

    @Update
    suspend fun update(logSession: LogSessionEntity)

    @Delete
    suspend fun delete(logSession: LogSessionEntity)
}