package me.algosketch.timelog.data

import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import me.algosketch.timelog.data.local.entity.DailyAggregate
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

    fun getLogTypesFlow(): Flow<List<LogTypeEntity>> = logTypeDao.getAllActiveFlow()

    suspend fun addLogType(name: String, colorHex: String, icon: String, includeEfficiency: Boolean) {
        val sortOrder = logTypeDao.getAllActive().size
        logTypeDao.insert(
            LogTypeEntity(
                id = 0,
                name = name,
                colorHex = colorHex,
                icon = icon,
                sortOrder = sortOrder,
                isActive = true,
                includeEfficiency = includeEfficiency,
            )
        )
    }

    suspend fun updateLogTypeEfficiency(id: Int, includeEfficiency: Boolean) {
        logTypeDao.updateEfficiency(id, includeEfficiency)
    }

    suspend fun saveSession(typeId: Int, startedAt: LocalDateTime, endedAt: LocalDateTime) {
        logSessionDao.insert(
            LogSessionEntity(id = 0, typeId = typeId, startedAt = startedAt, endedAt = endedAt)
        )
    }

    suspend fun getTodaySessions(): List<LogSessionEntity> {
        val startOfDay = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        return logSessionDao.getSessionsAfter(startOfDay)
    }

    fun getDailyAggregatesFlow(): Flow<List<DailyAggregate>> = logSessionDao.getDailyAggregates()
}
