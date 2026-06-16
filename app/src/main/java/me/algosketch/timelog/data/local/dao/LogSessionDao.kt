package me.algosketch.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.entity.DailyAggregate
import me.algosketch.timelog.data.local.entity.LogSessionEntity

@Dao
interface LogSessionDao {
    @Query("SELECT * FROM log_sessions")
    fun getAll(): Flow<List<LogSessionEntity>>

    @Query("SELECT * FROM log_sessions WHERE startedAt >= :startOfDay ORDER BY startedAt DESC")
    suspend fun getSessionsAfter(startOfDay: Long): List<LogSessionEntity>

    @Query("""
        SELECT
          date(s.startedAt, 'unixepoch', 'localtime') AS date,
          COUNT(*) AS sessionCount,
          SUM(s.endedAt - s.startedAt) AS totalSecs,
          SUM(CASE WHEN t.includeEfficiency = 1 THEN s.endedAt - s.startedAt ELSE 0 END) AS workSecs,
          SUM(CASE WHEN t.includeEfficiency = 0 THEN s.endedAt - s.startedAt ELSE 0 END) AS restSecs
        FROM log_sessions s
        JOIN log_types t ON s.typeId = t.id
        GROUP BY date
        ORDER BY date DESC
    """)
    fun getDailyAggregates(): Flow<List<DailyAggregate>>

    @Insert
    suspend fun insert(logSession: LogSessionEntity)

    @Update
    suspend fun update(logSession: LogSessionEntity)

    @Delete
    suspend fun delete(logSession: LogSessionEntity)
}