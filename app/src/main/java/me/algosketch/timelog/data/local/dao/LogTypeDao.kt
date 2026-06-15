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

    @Query("SELECT * FROM log_types WHERE isActive = 1 ORDER BY sortOrder ASC")
    suspend fun getAllActive(): List<LogTypeEntity>

    @Query("SELECT * FROM log_types WHERE isActive = 1 ORDER BY sortOrder ASC")
    fun getAllActiveFlow(): Flow<List<LogTypeEntity>>

    @Query("UPDATE log_types SET includeEfficiency = :includeEfficiency WHERE id = :id")
    suspend fun updateEfficiency(id: Int, includeEfficiency: Boolean)

    @Insert
    suspend fun insert(logType: LogTypeEntity)

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