package me.algosketch.timelog.ui.feature.stopwatch

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.SessionRecorder
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import me.algosketch.timelog.notification.RecordingNotifier
import me.algosketch.timelog.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class StopWatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LogRepository,
    private val recordingNotifier: RecordingNotifier,
    private val sessionRecorder: SessionRecorder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopWatchUiState())
    val uiState: StateFlow<StopWatchUiState> = _uiState.asStateFlow()

    private var sessionStartTime: LocalDateTime? = null
    private var timerJob: Job? = null
    private var midnightJob: Job? = null
    private val accumulatedSeconds = mutableMapOf<Int, Long>()
    private var currentTypes = listOf<LogTypeEntity>()
    private var todaySessions = listOf<LogSessionEntity>()
    // 화면에 표시 중인 "오늘" 기록의 기준 날짜
    private val currentDay = MutableStateFlow(LocalDate.now())
    private var isInBackground = false

    init {
        viewModelScope.launch {
            repository.getLogTypesFlow().collect { types ->
                currentTypes = types
                _uiState.update { state ->
                    state.copy(
                        logTypes = buildLogTypeUiItems(),
                        todaySummary = computeTodaySummary(),
                        sessions = buildSessionEntries(),
                    )
                }
                updateRecordingNotification()
            }
        }
        // 프로세스가 종료되기 전에 기록 중이던 세션이 있으면 이어서 기록한다.
        restoreActiveSession()
        viewModelScope.launch {
            loadLastType()
        }
        observeTodaySessions()
        observeExternalStop()
        scheduleMidnightRollover()
    }

    /**
     * 디스크에 남아 있는 진행 중 세션을 복원한다. 그 사이 날짜가 바뀌었으면 지난 날짜분은 자정마다 끊어 저장하고
     * 오늘 0시부터 이어서 기록한다.
     */
    private fun restoreActiveSession() {
        val session = sessionRecorder.splitBefore(currentDay.value) ?: return
        resumeSession(session.typeId, session.startedAt)
    }

    /** 알림의 정지 버튼처럼 화면 밖에서 세션이 저장·종료되면 정지 상태로 바꾼다. */
    private fun observeExternalStop() {
        viewModelScope.launch {
            sessionRecorder.activeSession.collect { session ->
                if (session != null || _uiState.value.activeTypeId == null) return@collect
                timerJob?.cancel()
                val elapsed = sessionStartTime
                    ?.let { ChronoUnit.SECONDS.between(it, LocalDateTime.now()) }
                    ?: 0L
                sessionStartTime = null
                _uiState.update { state ->
                    state.copy(
                        activeTypeId = null,
                        currentSessionTime = formatElapsed(elapsed),
                        logTypes = buildLogTypeUiItems(activeTypeId = null),
                        todaySummary = computeTodaySummary(activeTypeId = null),
                    )
                }
                updateRecordingNotification()
            }
        }
    }

    fun onAppBackground() {
        isInBackground = true
        updateRecordingNotification()
    }

    fun onAppForeground() {
        isInBackground = false
        recordingNotifier.cancel()
    }

    /** 백그라운드에서 기록 중일 때만 알림을 띄운다. */
    private fun updateRecordingNotification() {
        if (!isInBackground) return
        val activeTypeId = _uiState.value.activeTypeId
        val startTime = sessionStartTime
        if (activeTypeId == null || startTime == null) {
            recordingNotifier.cancel()
            return
        }
        val typeName = currentTypes.firstOrNull { it.id == activeTypeId }?.name ?: ""
        recordingNotifier.show(typeName, startTime)
    }

    /** 자정까지 남은 시간만큼 대기한 뒤 날짜 변경을 처리한다. 호출할 때마다 남은 시간을 새로 계산한다. */
    private fun scheduleMidnightRollover() {
        midnightJob?.cancel()
        midnightJob = viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
                delay(ChronoUnit.MILLIS.between(now, nextMidnight))
                rolloverIfDayChanged()
            }
        }
    }

    private suspend fun loadLastType() {
        val lastSession = repository.getLastSession()
        _uiState.update { state ->
            // 로딩 중에 이미 기록을 시작했다면 그 타입이 더 최신이다.
            state.copy(lastTypeId = state.lastTypeId ?: lastSession?.typeId)
        }
    }

    /** 오늘 세션을 DB에서 구독한다. 세션 수정 화면에서 바꾼 값도 그대로 반영된다. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeTodaySessions() {
        viewModelScope.launch {
            currentDay
                .flatMapLatest { day -> repository.getSessionsForDayFlow(day) }
                .collect { sessions ->
                    todaySessions = sessions
                    accumulatedSeconds.clear()
                    for (session in sessions) {
                        val duration = ChronoUnit.SECONDS.between(session.startedAt, session.endedAt)
                        accumulatedSeconds[session.typeId] = (accumulatedSeconds[session.typeId] ?: 0L) + duration
                    }
                    val activeElapsed = sessionStartTime
                        ?.takeIf { _uiState.value.activeTypeId != null }
                        ?.let { ChronoUnit.SECONDS.between(it, LocalDateTime.now()) }
                        ?: 0L
                    _uiState.update { state ->
                        state.copy(
                            sessions = buildSessionEntries(),
                            logTypes = buildLogTypeUiItems(activeElapsed = activeElapsed),
                            todaySummary = computeTodaySummary(activeElapsed = activeElapsed),
                            currentSessionTime = if (state.activeTypeId != null) {
                                formatElapsed(activeElapsed)
                            } else {
                                // 정지 상태에서는 가장 최근 세션의 길이를 보여준다. 수정 화면에서 바꾼 값도 반영된다.
                                formatElapsed(sessions.firstOrNull()?.let { ChronoUnit.SECONDS.between(it.startedAt, it.endedAt) } ?: 0L)
                            },
                        )
                    }
                }
        }
    }

    /** 백그라운드 동안 delay가 어긋났을 수 있으므로, 날짜 변경을 먼저 처리하고 자정 예약을 다시 계산한다. */
    fun onResume() {
        rolloverIfDayChanged()
        scheduleMidnightRollover()
    }

    fun onTypeClick(typeId: Int) {
        rolloverIfDayChanged()
        val current = _uiState.value.activeTypeId
        if (current == typeId) return
        if (current != null) finishSession(current)
        startSession(typeId)
    }

    fun onStopClick() {
        rolloverIfDayChanged()
        val current = _uiState.value.activeTypeId ?: return
        finishSession(current)
        sessionStartTime = null
        _uiState.update { it.copy(
            activeTypeId = null,
            logTypes = buildLogTypeUiItems(activeTypeId = null),
        ) }
        updateRecordingNotification()
    }

    private fun startSession(typeId: Int) {
        // 시작하는 순간 디스크에 기록해, 프로세스가 종료되어도 진행 중 세션이 남게 한다.
        val session = sessionRecorder.start(typeId, LocalDateTime.now())
        resumeSession(session.typeId, session.startedAt)
    }

    /** [startedAt]부터 진행 중인 세션을 화면에 반영하고 1초마다 경과 시간을 갱신한다. */
    private fun resumeSession(typeId: Int, startedAt: LocalDateTime) {
        timerJob?.cancel()
        sessionStartTime = startedAt
        val initialElapsed = ChronoUnit.SECONDS.between(startedAt, LocalDateTime.now())
        _uiState.update { it.copy(
            activeTypeId = typeId,
            lastTypeId = typeId,
            currentSessionTime = formatElapsed(initialElapsed),
            logTypes = buildLogTypeUiItems(activeTypeId = typeId, activeElapsed = initialElapsed),
            todaySummary = computeTodaySummary(activeTypeId = typeId, activeElapsed = initialElapsed),
        ) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                val elapsed = ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now())
                _uiState.update { state ->
                    state.copy(
                        currentSessionTime = formatElapsed(elapsed),
                        logTypes = buildLogTypeUiItems(activeTypeId = typeId, activeElapsed = elapsed),
                        todaySummary = computeTodaySummary(activeTypeId = typeId, activeElapsed = elapsed),
                    )
                }
            }
        }
    }

    private fun finishSession(typeId: Int) {
        timerJob?.cancel()
        val startTime = sessionStartTime ?: return
        val endTime = LocalDateTime.now()
        val elapsed = ChronoUnit.SECONDS.between(startTime, endTime)

        // 저장 후 DB 구독으로 다시 계산되지만, 그 사이 누적 시간이 잠깐 줄어 보이지 않도록 먼저 더해 둔다.
        accumulatedSeconds[typeId] = (accumulatedSeconds[typeId] ?: 0L) + elapsed
        // 화면이 곧바로 사라져도 저장이 취소되지 않도록 앱 스코프에서 저장한다.
        sessionRecorder.stop(endTime)

        _uiState.update { state ->
            state.copy(
                // 정지 후에도 방금 끝난 세션의 길이를 그대로 보여준다.
                currentSessionTime = formatElapsed(elapsed),
                logTypes = buildLogTypeUiItems(),
                todaySummary = computeTodaySummary(),
            )
        }
    }

    /**
     * 날짜가 바뀌었으면 진행 중인 세션을 자정 기준으로 끊어 저장하고, 오늘 기록을 초기화한다.
     * 진행 중이던 세션은 같은 타입으로 오늘 0시부터 이어서 기록한다.
     */
    private fun rolloverIfDayChanged() {
        val today = LocalDate.now()
        if (!today.isAfter(currentDay.value)) return
        currentDay.value = today

        val activeTypeId = _uiState.value.activeTypeId
        if (activeTypeId != null) {
            // 여러 날에 걸쳐 있으면 날짜마다 끊어서 저장하고, 오늘 0시부터 이어서 기록한다.
            sessionRecorder.splitBefore(today)?.let { sessionStartTime = it.startedAt }
        }

        todaySessions = emptyList()
        accumulatedSeconds.clear()
        _uiState.update { state ->
            state.copy(
                sessions = emptyList(),
                // 기록 중이면 0시부터 이어지는 세션의 경과 시간, 정지 상태면 오늘 세션이 없으므로 00:00
                currentSessionTime = formatElapsed(
                    sessionStartTime
                        ?.takeIf { activeTypeId != null }
                        ?.let { ChronoUnit.SECONDS.between(it, LocalDateTime.now()) }
                        ?: 0L
                ),
                logTypes = buildLogTypeUiItems(),
                todaySummary = computeTodaySummary(),
            )
        }
        // 세션 시작 시각이 자정으로 바뀌었으므로 알림의 크로노미터도 갱신한다.
        updateRecordingNotification()
    }

    /** 최신순 세션 목록을 만들고, 이웃한 세션 사이 간격이 [GAP_THRESHOLD_SECONDS] 이상이면 공백 항목을 끼워 넣는다. */
    private fun buildSessionEntries(): List<SessionLogItem> = buildList {
        todaySessions.forEachIndexed { index, session ->
            add(buildSessionEntry(session))
            // todaySessions는 시작 시각 내림차순이므로 다음 항목이 바로 이전 세션이다.
            val previous = todaySessions.getOrNull(index + 1) ?: return@forEachIndexed
            val gapSeconds = ChronoUnit.SECONDS.between(previous.endedAt, session.startedAt)
            if (gapSeconds >= GAP_THRESHOLD_SECONDS) {
                add(SessionGap(duration = formatGapDuration(gapSeconds)))
            }
        }
    }

    private fun buildSessionEntry(session: LogSessionEntity): SessionEntry {
        val type = currentTypes.firstOrNull { it.id == session.typeId }
        return SessionEntry(
            id = session.id,
            typeName = type?.name ?: "",
            color = type?.colorHex?.toComposeColor() ?: Color(0xFF808080),
            time = context.getString(
                R.string.session_time_range,
                formatSessionTime(session.startedAt),
                formatSessionTime(session.endedAt),
            ),
            duration = formatElapsed(ChronoUnit.SECONDS.between(session.startedAt, session.endedAt)),
        )
    }

    private fun formatGapDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        // "공백" 같은 문구 없이 간격만 보여준다.
        return if (hours > 0) {
            context.getString(R.string.duration_hours_and_minutes, hours, minutes)
        } else {
            context.getString(R.string.duration_minutes_only, minutes)
        }
    }

    private fun buildLogTypeUiItems(
        types: List<LogTypeEntity> = currentTypes,
        activeTypeId: Int? = _uiState.value.activeTypeId,
        activeElapsed: Long = 0L,
    ): List<LogTypeUiItem> = types.map { type ->
        val base = accumulatedSeconds[type.id] ?: 0L
        val total = if (type.id == activeTypeId) base + activeElapsed else base
        LogTypeUiItem(
            id = type.id,
            name = type.name,
            icon = type.icon,
            color = type.colorHex.toComposeColor(),
            accumulatedTime = formatElapsed(total),
            isActive = type.id == activeTypeId,
            includeEfficiency = type.includeEfficiency,
        )
    }

    private fun computeTodaySummary(
        types: List<LogTypeEntity> = currentTypes,
        activeTypeId: Int? = _uiState.value.activeTypeId,
        activeElapsed: Long = 0L,
    ): TodaySummary? {
        val workSecs = types.filter { it.includeEfficiency }
            .sumOf { (accumulatedSeconds[it.id] ?: 0L) + if (it.id == activeTypeId) activeElapsed else 0L }
        val restSecs = types.filter { !it.includeEfficiency }
            .sumOf { (accumulatedSeconds[it.id] ?: 0L) + if (it.id == activeTypeId) activeElapsed else 0L }
        val total = workSecs + restSecs
        if (total == 0L) return null
        return TodaySummary(
            workTime = formatElapsed(workSecs),
            efficiency = "${(workSecs * 100L / total).toInt()}%",
            restTime = formatElapsed(restSecs),
            efficiencyRatio = workSecs.toFloat() / total,
        )
    }

    private fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        } else {
            "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
        }
    }

    private fun formatSessionTime(time: LocalDateTime): String {
        val pattern = context.getString(R.string.time_format_pattern)
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        // 진행 중 세션은 디스크에 남아 다음 실행 때 이어지므로 지우지 않는다.
        // 기록 중 알림도 남겨 두어, 화면 없이도 정지 버튼으로 저장할 수 있게 한다.
    }

    private companion object {
        // 세션 사이 공백을 표시하는 최소 간격 (5분)
        const val GAP_THRESHOLD_SECONDS = 5 * 60L
    }
}
