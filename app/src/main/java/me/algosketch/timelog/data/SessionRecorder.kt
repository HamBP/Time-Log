package me.algosketch.timelog.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 진행 중 세션의 시작·정지·자정 분할을 담당한다. 화면(ViewModel)이 없을 때도
 * 알림의 정지 버튼에서 같은 경로로 세션을 저장할 수 있도록 앱 스코프에서 동작한다.
 */
@Singleton
class SessionRecorder @Inject constructor(
    private val holder: ActiveSessionHolder,
    private val repository: LogRepository,
) {
    // 화면이 사라져도 저장이 취소되지 않도록 ViewModel이 아닌 앱 스코프에서 저장한다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val activeSession: StateFlow<ActiveSession?> = holder.session

    /** 새 세션을 시작하고 즉시 디스크에 기록한다. */
    fun start(typeId: Int, startedAt: LocalDateTime): ActiveSession {
        // 저장 단위(epoch 초)에 맞춰, 재실행 후 복원한 값과 같아지도록 초 미만을 버린다.
        val session = ActiveSession(typeId, startedAt.truncatedTo(ChronoUnit.SECONDS))
        holder.set(session)
        return session
    }

    /**
     * 진행 중 세션을 [endedAt]에 끝내 log_sessions에 저장한다. 자정을 넘겼으면 날짜마다 나눠 저장한다.
     * 저장이 끝난 뒤에 진행 중 상태를 지우므로, 도중에 프로세스가 죽어도 세션이 사라지지 않는다.
     */
    fun stop(endedAt: LocalDateTime): Job? {
        val session = holder.session.value ?: return null
        return scope.launch {
            saveSplitByDay(session.typeId, session.startedAt, endedAt)
            // 저장하는 사이 새 세션이 시작됐다면 그 세션은 지우지 않는다.
            holder.compareAndSet(session, null)
        }
    }

    /**
     * 진행 중 세션이 [day] 이전에 시작됐으면 [day] 0시 전까지를 날짜마다 나눠 저장하고,
     * 같은 타입으로 [day] 0시부터 이어서 기록한다. 이어지는 세션(없으면 null)을 돌려준다.
     */
    fun splitBefore(day: LocalDate): ActiveSession? {
        val session = holder.session.value ?: return null
        val midnight = day.atStartOfDay()
        if (session.startedAt >= midnight) return session
        val continued = session.copy(startedAt = midnight)
        if (!holder.compareAndSet(session, continued)) return holder.session.value
        scope.launch { saveSplitByDay(session.typeId, session.startedAt, midnight) }
        return continued
    }

    private suspend fun saveSplitByDay(typeId: Int, startedAt: LocalDateTime, endedAt: LocalDateTime) {
        var segmentStart = startedAt
        while (true) {
            val nextMidnight = segmentStart.toLocalDate().plusDays(1).atStartOfDay()
            if (endedAt <= nextMidnight) {
                repository.saveSession(typeId, segmentStart, endedAt)
                return
            }
            repository.saveSession(typeId, segmentStart, nextMidnight)
            segmentStart = nextMidnight
        }
    }
}
