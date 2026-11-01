package me.algosketch.timelog.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.entity.DailyAggregate
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import me.algosketch.timelog.data.local.entity.SessionDetail

@Dao
interface LogSessionDao {
    @Query("SELECT * FROM log_sessions")
    fun getAll(): Flow<List<LogSessionEntity>>

    @Query("SELECT * FROM log_sessions WHERE startedAt >= :startOfDay ORDER BY startedAt DESC")
    fun getSessionsAfterFlow(startOfDay: Long): Flow<List<LogSessionEntity>>

    @Query("SELECT * FROM log_sessions ORDER BY endedAt DESC LIMIT 1")
    suspend fun getLastSession(): LogSessionEntity?

    @Query("SELECT * FROM log_sessions WHERE id = :id")
    suspend fun getById(id: Int): LogSessionEntity?

    /** 시작 시각 기준으로 바로 앞 세션. 시작 시각이 같으면 id 순서로 판단한다. */
    @Query("""
        SELECT * FROM log_sessions
        WHERE startedAt < :startedAt OR (startedAt = :startedAt AND id < :id)
        ORDER BY startedAt DESC, id DESC
        LIMIT 1
    """)
    suspend fun getPrevious(id: Int, startedAt: Long): LogSessionEntity?

    /** 시작 시각 기준으로 바로 뒤 세션. 시작 시각이 같으면 id 순서로 판단한다. */
    @Query("""
        SELECT * FROM log_sessions
        WHERE startedAt > :startedAt OR (startedAt = :startedAt AND id > :id)
        ORDER BY startedAt ASC, id ASC
        LIMIT 1
    """)
    suspend fun getNext(id: Int, startedAt: Long): LogSessionEntity?

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

    @Query("""
        SELECT
          t.name AS typeName,
          t.colorHex AS colorHex,
          t.icon AS icon,
          t.includeEfficiency AS includeEfficiency,
          s.startedAt AS startedAt,
          s.endedAt AS endedAt
        FROM log_sessions s
        JOIN log_types t ON s.typeId = t.id
        WHERE date(s.startedAt, 'unixepoch', 'localtime') = :date
        ORDER BY s.startedAt ASC
    """)
    fun getSessionsForDate(date: String): Flow<List<SessionDetail>>

    @Insert
    suspend fun insert(logSession: LogSessionEntity)

    @Update
    suspend fun update(logSession: LogSessionEntity)

    @Delete
    suspend fun delete(logSession: LogSessionEntity)
}