package me.algosketch.timelog.ui.feature.stopwatch

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.data.LogRepository
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopWatchUiState())
    val uiState: StateFlow<StopWatchUiState> = _uiState.asStateFlow()

    private var sessionStartTime: LocalDateTime? = null
    private var timerJob: Job? = null
    private var midnightJob: Job? = null
    private val accumulatedSeconds = mutableMapOf<Int, Long>()
    private var currentTypes = listOf<LogTypeEntity>()
    // 화면에 표시 중인 "오늘" 기록의 기준 날짜
    private var currentDay: LocalDate = LocalDate.now()
    private var isInBackground = false

    init {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(currentTime = formatCurrentTime(), currentDate = formatCurrentDate()) }
                delay(1.seconds)
            }
        }
        viewModelScope.launch {
            repository.getLogTypesFlow().collect { types ->
                currentTypes = types
                _uiState.update { state ->
                    state.copy(
                        logTypes = buildLogTypeUiItems(),
                        todaySummary = computeTodaySummary(),
                    )
                }
                updateRecordingNotification()
            }
        }
        viewModelScope.launch {
            loadSessionsFromDb()
        }
        viewModelScope.launch {
            recordingNotifier.stopRequests.collect { onStopClick() }
        }
        scheduleMidnightRollover()
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

    private suspend fun loadSessionsFromDb() {
        val lastSession = repository.getLastSession()
        _uiState.update { state ->
            // 로딩 중에 이미 기록을 시작했다면 그 타입이 더 최신이다.
            state.copy(lastTypeId = state.lastTypeId ?: lastSession?.typeId)
        }

        val types = repository.getActiveLogTypes()
        val dbSessions = repository.getTodaySessions()

        for (session in dbSessions) {
            val duration = ChronoUnit.SECONDS.between(session.startedAt, session.endedAt)
            accumulatedSeconds[session.typeId] = (accumulatedSeconds[session.typeId] ?: 0L) + duration
        }

        if (dbSessions.isEmpty()) return

        val sessionEntries = dbSessions.map { session ->
            val type = types.firstOrNull { it.id == session.typeId }
            val duration = ChronoUnit.SECONDS.between(session.startedAt, session.endedAt)
            SessionEntry(
                typeName = type?.name ?: "",
                color = type?.colorHex?.toComposeColor() ?: Color(0xFF808080),
                time = formatSessionTime(session.startedAt),
                duration = formatElapsed(duration),
            )
        }

        _uiState.update { state ->
            state.copy(
                sessions = sessionEntries,
                logTypes = buildLogTypeUiItems(types),
                todaySummary = computeTodaySummary(types),
            )
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
        _uiState.update { it.copy(
            activeTypeId = null,
            logTypes = buildLogTypeUiItems(activeTypeId = null),
        ) }
        updateRecordingNotification()
    }

    private fun startSession(typeId: Int) {
        timerJob?.cancel()
        sessionStartTime = LocalDateTime.now()
        _uiState.update { it.copy(
            activeTypeId = typeId,
            lastTypeId = typeId,
            logTypes = buildLogTypeUiItems(activeTypeId = typeId),
        ) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                val elapsed = ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now())
                _uiState.update { state ->
                    state.copy(
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

        accumulatedSeconds[typeId] = (accumulatedSeconds[typeId] ?: 0L) + elapsed
        viewModelScope.launch { repository.saveSession(typeId, startTime, endTime) }

        val type = currentTypes.firstOrNull { it.id == typeId }
        val entry = SessionEntry(
            typeName = type?.name ?: "",
            color = type?.colorHex?.toComposeColor() ?: Color(0xFF808080),
            time = formatSessionTime(startTime),
            duration = formatElapsed(elapsed),
        )
        _uiState.update { state ->
            state.copy(
                sessions = listOf(entry) + state.sessions,
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
        if (!today.isAfter(currentDay)) return
        currentDay = today

        val activeTypeId = _uiState.value.activeTypeId
        val startTime = sessionStartTime
        if (activeTypeId != null && startTime != null) {
            // 여러 날에 걸쳐 있으면 날짜마다 끊어서 저장한다.
            val midnight = today.atStartOfDay()
            var segmentStart: LocalDateTime = startTime
            while (segmentStart < midnight) {
                val segmentEnd = segmentStart.toLocalDate().plusDays(1).atStartOfDay()
                val start = segmentStart
                viewModelScope.launch { repository.saveSession(activeTypeId, start, segmentEnd) }
                segmentStart = segmentEnd
            }
            sessionStartTime = segmentStart
        }

        accumulatedSeconds.clear()
        _uiState.update { state ->
            state.copy(
                sessions = emptyList(),
                logTypes = buildLogTypeUiItems(),
                todaySummary = computeTodaySummary(),
            )
        }
        // 세션 시작 시각이 자정으로 바뀌었으므로 알림의 크로노미터도 갱신한다.
        updateRecordingNotification()
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

    private fun formatCurrentTime(): String {
        val pattern = context.getString(R.string.time_format_pattern)
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    private fun formatCurrentDate(): String {
        val pattern = context.getString(R.string.date_format_pattern)
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    private fun formatSessionTime(time: LocalDateTime): String {
        val pattern = context.getString(R.string.time_format_pattern)
        return time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        recordingNotifier.cancel()
    }
}
