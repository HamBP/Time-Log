package me.algosketch.timelog.data

import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logTypeDao: LogTypeDao,
    private val logSessionDao: LogSessionDao,
) {
    suspend fun getActiveLogTypes(): List<LogTypeEntity> = logTypeDao.getAllActive()

    suspend fun saveSession(typeId: Int, startedAt: LocalDateTime, endedAt: LocalDateTime) {
        logSessionDao.insert(
            LogSessionEntity(id = 0, typeId = typeId, startedAt = startedAt, endedAt = endedAt)
        )
    }

    suspend fun getTodaySessions(): List<LogSessionEntity> {
        val startOfDay = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return logSessionDao.getSessionsAfter(startOfDay)
    }
}
