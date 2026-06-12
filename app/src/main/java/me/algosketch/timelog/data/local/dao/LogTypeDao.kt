package me.algosketch.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.entity.LogTypeEntity

@Dao
interface LogTypeDao {
    @Query("SELECT * FROM log_types")
    fun getAll(): Flow<List<LogTypeEntity>>

    @Insert
    suspend fun insert(logType: LogTypeEntity): Int

    @Update
    suspend fun update(logType: LogTypeEntity)

    @Query("""
        UPDATE log_types
        SET isActive = :isActive
        WHERE id = :id
    """)
    suspend fun hide(id: Int, isActive: Boolean)

    @Delete
    suspend fun delete(logType: LogTypeEntity)
}