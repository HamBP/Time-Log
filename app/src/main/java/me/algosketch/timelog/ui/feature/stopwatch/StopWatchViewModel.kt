package me.algosketch.timelog.ui.feature.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

enum class TimerState { IDLE, WORK, REST }

data class SessionEntry(
    val type: TimerState,
    val time: String,
    val duration: String
)

data class TodaySummary(
    val workTime: String,
    val efficiency: String,
    val restTime: String,
    val efficiencyRatio: Float
)

@HiltViewModel
class StopWatchViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopWatchUiState())
    val uiState: StateFlow<StopWatchUiState> = _uiState.asStateFlow()

    private var elapsedSeconds = 0L
    private var workAccumulatedSeconds = 0L
    private var restAccumulatedSeconds = 0L
    private var sessionStartTime: LocalDateTime? = null
    private var timerJob: Job? = null
    private var workTypeId: Int? = null
    private var restTypeId: Int? = null

    init {
        viewModelScope.launch {
            while (true) {
                _uiState.update {
                    it.copy(currentTime = formatCurrentTime(), currentDate = formatCurrentDate())
                }
                delay(1000L)
            }
        }
        viewModelScope.launch {
            loadFromDb()
        }
    }

    private suspend fun loadFromDb() {
        val types = repository.getActiveLogTypes()
        workTypeId = types.firstOrNull { it.includeEfficiency }?.id
        restTypeId = types.firstOrNull { !it.includeEfficiency }?.id

        val dbSessions = repository.getTodaySessions()
        if (dbSessions.isEmpty()) return

        val (sessions, workSecs, restSecs) = mapToSessionEntries(types, dbSessions)
        workAccumulatedSeconds = workSecs
        restAccumulatedSeconds = restSecs
        _uiState.update { state ->
            state.copy(
                sessions = sessions,
                workAccumulatedTime = formatElapsed(workSecs),
                restAccumulatedTime = formatElapsed(restSecs),
                todaySummary = computeTodaySummary()
            )
        }
    }

    private fun mapToSessionEntries(
        types: List<LogTypeEntity>,
        dbSessions: List<LogSessionEntity>,
    ): Triple<List<SessionEntry>, Long, Long> {
        val workId = types.firstOrNull { it.includeEfficiency }?.id
        val restId = types.firstOrNull { !it.includeEfficiency }?.id
        var workSecs = 0L
        var restSecs = 0L
        val entries = mutableListOf<SessionEntry>()

        for (session in dbSessions) {
            val duration = session.endedAt.toEpochSecond(ZoneOffset.UTC) -
                    session.startedAt.toEpochSecond(ZoneOffset.UTC)
            val timerState = when (session.typeId) {
                workId -> { workSecs += duration; TimerState.WORK }
                restId -> { restSecs += duration; TimerState.REST }
                else -> null
            } ?: continue
            entries.add(SessionEntry(
                type = timerState,
                time = formatSessionTime(session.startedAt),
                duration = formatElapsed(duration)
            ))
        }
        return Triple(entries, workSecs, restSecs)
    }

    fun onWorkClick() {
        val prev = _uiState.value.timerState
        if (prev == TimerState.WORK) return
        if (prev != TimerState.IDLE) finishSession(prev)
        startSession(TimerState.WORK)
    }

    fun onRestClick() {
        val prev = _uiState.value.timerState
        if (prev == TimerState.REST) return
        if (prev != TimerState.IDLE) finishSession(prev)
        startSession(TimerState.REST)
    }

    fun onStopClick() {
        val prev = _uiState.value.timerState
        if (prev == TimerState.IDLE) return
        finishSession(prev)
        elapsedSeconds = 0L
        _uiState.update { it.copy(timerState = TimerState.IDLE, elapsedTime = formatElapsed(0L)) }
    }

    private fun startSession(type: TimerState) {
        timerJob?.cancel()
        elapsedSeconds = 0L
        sessionStartTime = LocalDateTime.now()
        _uiState.update { it.copy(timerState = type, elapsedTime = formatElapsed(0L)) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                elapsedSeconds++
                when (type) {
                    TimerState.WORK -> workAccumulatedSeconds++
                    TimerState.REST -> restAccumulatedSeconds++
                    TimerState.IDLE -> {}
                }
                _uiState.update { state ->
                    state.copy(
                        elapsedTime = formatElapsed(elapsedSeconds),
                        workAccumulatedTime = formatElapsed(workAccumulatedSeconds),
                        restAccumulatedTime = formatElapsed(restAccumulatedSeconds),
                        todaySummary = computeTodaySummary()
                    )
                }
            }
        }
    }

    private fun finishSession(type: TimerState) {
        timerJob?.cancel()
        val startTime = sessionStartTime ?: return
        val endTime = startTime.plusSeconds(elapsedSeconds)

        val typeId = when (type) {
            TimerState.WORK -> workTypeId
            TimerState.REST -> restTypeId
            TimerState.IDLE -> null
        }
        if (typeId != null) {
            viewModelScope.launch { repository.saveSession(typeId, startTime, endTime) }
        }

        val entry = SessionEntry(
            type = type,
            time = formatSessionTime(startTime),
            duration = formatElapsed(elapsedSeconds)
        )
        _uiState.update { state ->
            state.copy(
                sessions = listOf(entry) + state.sessions,
                workAccumulatedTime = formatElapsed(workAccumulatedSeconds),
                restAccumulatedTime = formatElapsed(restAccumulatedSeconds),
                todaySummary = computeTodaySummary()
            )
        }
    }

    private fun computeTodaySummary(): TodaySummary? {
        val total = workAccumulatedSeconds + restAccumulatedSeconds
        if (total == 0L) return null
        val efficiencyPct = (workAccumulatedSeconds * 100L / total).toInt()
        return TodaySummary(
            workTime = formatElapsed(workAccumulatedSeconds),
            efficiency = "$efficiencyPct%",
            restTime = formatElapsed(restAccumulatedSeconds),
            efficiencyRatio = workAccumulatedSeconds.toFloat() / total
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
        val now = LocalDateTime.now()
        val amPm = if (now.hour < 12) "오전" else "오후"
        val hour12 = when {
            now.hour == 0 -> 12
            now.hour > 12 -> now.hour - 12
            else -> now.hour
        }
        return "$amPm ${hour12}:${now.minute.toString().padStart(2, '0')}"
    }

    private fun formatCurrentDate(): String {
        val now = LocalDateTime.now()
        val dayOfWeek = when (now.dayOfWeek.value) {
            1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; else -> "일"
        }
        return "${now.monthValue}월 ${now.dayOfMonth}일 ($dayOfWeek)"
    }

    private fun formatSessionTime(time: LocalDateTime): String {
        val amPm = if (time.hour < 12) "오전" else "오후"
        val hour12 = when {
            time.hour == 0 -> 12
            time.hour > 12 -> time.hour - 12
            else -> time.hour
        }
        return "$amPm ${hour12}:${time.minute.toString().padStart(2, '0')}"
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
