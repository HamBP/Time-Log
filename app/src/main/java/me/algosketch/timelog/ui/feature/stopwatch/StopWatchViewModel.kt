package me.algosketch.timelog.ui.feature.stopwatch

import androidx.compose.ui.graphics.Color
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
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import me.algosketch.timelog.ui.theme.toComposeColor
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class StopWatchViewModel @Inject constructor(
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopWatchUiState())
    val uiState: StateFlow<StopWatchUiState> = _uiState.asStateFlow()

    private var elapsedSeconds = 0L
    private var sessionStartTime: LocalDateTime? = null
    private var timerJob: Job? = null
    private val accumulatedSeconds = mutableMapOf<Int, Long>()
    private var currentTypes = listOf<LogTypeEntity>()

    init {
        viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(currentTime = formatCurrentTime(), currentDate = formatCurrentDate()) }
                delay(1000L)
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
            }
        }
        viewModelScope.launch {
            loadSessionsFromDb()
        }
    }

    private suspend fun loadSessionsFromDb() {
        val types = repository.getActiveLogTypes()
        val dbSessions = repository.getTodaySessions()

        for (session in dbSessions) {
            val duration = session.endedAt.toEpochSecond(ZoneOffset.UTC) -
                    session.startedAt.toEpochSecond(ZoneOffset.UTC)
            accumulatedSeconds[session.typeId] = (accumulatedSeconds[session.typeId] ?: 0L) + duration
        }

        if (dbSessions.isEmpty()) return

        val sessionEntries = dbSessions.map { session ->
            val type = types.firstOrNull { it.id == session.typeId }
            val duration = session.endedAt.toEpochSecond(ZoneOffset.UTC) -
                    session.startedAt.toEpochSecond(ZoneOffset.UTC)
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

    fun onTypeClick(typeId: Int) {
        val current = _uiState.value.activeTypeId
        if (current == typeId) return
        if (current != null) finishSession(current)
        startSession(typeId)
    }

    fun onStopClick() {
        val current = _uiState.value.activeTypeId ?: return
        finishSession(current)
        elapsedSeconds = 0L
        _uiState.update { it.copy(
            activeTypeId = null,
            elapsedTime = formatElapsed(0L),
            logTypes = buildLogTypeUiItems(activeTypeId = null),
        ) }
    }

    private fun startSession(typeId: Int) {
        timerJob?.cancel()
        elapsedSeconds = 0L
        sessionStartTime = LocalDateTime.now()
        _uiState.update { it.copy(
            activeTypeId = typeId,
            elapsedTime = formatElapsed(0L),
            logTypes = buildLogTypeUiItems(activeTypeId = typeId),
        ) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                elapsedSeconds++
                accumulatedSeconds[typeId] = (accumulatedSeconds[typeId] ?: 0L) + 1L
                _uiState.update { state ->
                    state.copy(
                        elapsedTime = formatElapsed(elapsedSeconds),
                        logTypes = buildLogTypeUiItems(activeTypeId = typeId),
                        todaySummary = computeTodaySummary(),
                    )
                }
            }
        }
    }

    private fun finishSession(typeId: Int) {
        timerJob?.cancel()
        val startTime = sessionStartTime ?: return
        val endTime = startTime.plusSeconds(elapsedSeconds)

        viewModelScope.launch { repository.saveSession(typeId, startTime, endTime) }

        val type = currentTypes.firstOrNull { it.id == typeId }
        val entry = SessionEntry(
            typeName = type?.name ?: "",
            color = type?.colorHex?.toComposeColor() ?: Color(0xFF808080),
            time = formatSessionTime(startTime),
            duration = formatElapsed(elapsedSeconds),
        )
        _uiState.update { state ->
            state.copy(
                sessions = listOf(entry) + state.sessions,
                logTypes = buildLogTypeUiItems(),
                todaySummary = computeTodaySummary(),
            )
        }
    }

    private fun buildLogTypeUiItems(
        types: List<LogTypeEntity> = currentTypes,
        activeTypeId: Int? = _uiState.value.activeTypeId,
    ): List<LogTypeUiItem> = types.map { type ->
        LogTypeUiItem(
            id = type.id,
            name = type.name,
            icon = type.icon,
            color = type.colorHex.toComposeColor(),
            accumulatedTime = formatElapsed(accumulatedSeconds[type.id] ?: 0L),
            isActive = type.id == activeTypeId,
            includeEfficiency = type.includeEfficiency,
        )
    }

    private fun computeTodaySummary(types: List<LogTypeEntity> = currentTypes): TodaySummary? {
        val workSecs = types.filter { it.includeEfficiency }.sumOf { accumulatedSeconds[it.id] ?: 0L }
        val restSecs = types.filter { !it.includeEfficiency }.sumOf { accumulatedSeconds[it.id] ?: 0L }
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
