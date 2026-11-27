package me.algosketch.timelog.data

import kotlinx.coroutines.flow.Flow
import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import me.algosketch.timelog.data.local.entity.DailyAggregate
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import me.algosketch.timelog.data.local.entity.SessionDetail
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogRepository @Inject constructor(
    private val logTypeDao: LogTypeDao,
    private val logSessionDao: LogSessionDao,
    private val activeSessionHolder: ActiveSessionHolder,
) {
    suspend fun getActiveLogTypes(): List<LogTypeEntity> = logTypeDao.getAllActive()

    fun getLogTypesFlow(): Flow<List<LogTypeEntity>> = logTypeDao.getAllActiveFlow()

    /** 숨김 처리된 타입까지 포함한 전체 타입 (sortOrder 순). 과거 기록의 이름·색상을 찾을 때 쓴다. */
    fun getAllLogTypesFlow(): Flow<List<LogTypeEntity>> = logTypeDao.getAll()

    fun getHiddenLogTypesFlow(): Flow<List<LogTypeEntity>> = logTypeDao.getAllHiddenFlow()

    suspend fun addLogType(name: String, colorHex: String, icon: String, includeEfficiency: Boolean) {
        // 숨김 타입과 순서가 겹치지 않도록 전체 최댓값 다음 순서를 쓴다.
        val sortOrder = logTypeDao.getMaxSortOrder() + 1
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

    suspend fun getLogType(id: Int): LogTypeEntity? = logTypeDao.getById(id)

    suspend fun updateLogType(
        id: Int,
        name: String,
        colorHex: String,
        icon: String,
        includeEfficiency: Boolean,
    ) {
        val existing = logTypeDao.getById(id) ?: return
        logTypeDao.update(
            existing.copy(
                name = name,
                colorHex = colorHex,
                icon = icon,
                includeEfficiency = includeEfficiency,
            )
        )
    }

    /** 저장된 세션이 있거나, 지금 이 타입으로 기록 중이면 관련 기록이 있는 것으로 본다. */
    suspend fun hasRelatedRecords(typeId: Int): Boolean =
        activeSessionHolder.typeId.value == typeId || logSessionDao.countByType(typeId) > 0

    /**
     * 관련 기록이 없으면 타입을 삭제하고, 있으면 기록을 보존하기 위해 숨김 처리한다.
     * 확인 다이얼로그를 띄운 뒤 기록이 생겼을 수 있으므로 실행 시점에 다시 판단한다.
     * @return 삭제했으면 true, 숨김 처리했으면 false
     */
    suspend fun deleteOrHideLogType(typeId: Int): Boolean {
        if (hasRelatedRecords(typeId)) {
            logTypeDao.hide(typeId, isActive = false)
            return false
        }
        logTypeDao.deleteById(typeId)
        return true
    }

    /** 숨김 처리된 타입을 다시 활성화한다. 순서는 숨기기 전 위치를 유지한다. */
    suspend fun restoreLogType(typeId: Int) {
        logTypeDao.hide(typeId, isActive = true)
    }

    suspend fun saveSession(typeId: Int, startedAt: LocalDateTime, endedAt: LocalDateTime) {
        logSessionDao.insert(
            LogSessionEntity(id = 0, typeId = typeId, startedAt = startedAt, endedAt = endedAt)
        )
    }

    fun getSessionsForDayFlow(day: LocalDate): Flow<List<LogSessionEntity>> {
        val startOfDay = day.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        return logSessionDao.getSessionsAfterFlow(startOfDay)
    }

    suspend fun getSession(id: Int): LogSessionEntity? = logSessionDao.getById(id)

    /** 시간 순서상 바로 앞/뒤 세션. */
    suspend fun getAdjacentSessions(session: LogSessionEntity): Pair<LogSessionEntity?, LogSessionEntity?> {
        val startedAt = session.startedAt.atZone(ZoneId.systemDefault()).toEpochSecond()
        return logSessionDao.getPrevious(session.id, startedAt) to logSessionDao.getNext(session.id, startedAt)
    }

    suspend fun updateSessionTime(id: Int, startedAt: LocalDateTime, endedAt: LocalDateTime) {
        val existing = logSessionDao.getById(id) ?: return
        logSessionDao.update(existing.copy(startedAt = startedAt, endedAt = endedAt))
    }

    suspend fun getLastSession(): LogSessionEntity? = logSessionDao.getLastSession()

    fun getDailyAggregatesFlow(): Flow<List<DailyAggregate>> = logSessionDao.getDailyAggregates()

    fun getSessionsForDateFlow(date: String): Flow<List<SessionDetail>> =
        logSessionDao.getSessionsForDate(date)
}
