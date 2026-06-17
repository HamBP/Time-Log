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
import me.algosketch.timelog.ui.theme.toComposeColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class StopWatchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StopWatchUiState())
    val uiState: StateFlow<StopWatchUiState> = _uiState.asStateFlow()

    private var sessionStartTime: LocalDateTime? = null
    private var timerJob: Job? = null
    private val accumulatedSeconds = mutableMapOf<Int, Long>()
    private var currentTypes = listOf<LogTypeEntity>()

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

    fun onTypeClick(typeId: Int) {
        val current = _uiState.value.activeTypeId
        if (current == typeId) return
        if (current != null) finishSession(current)
        startSession(typeId)
    }

    fun onStopClick() {
        val current = _uiState.value.activeTypeId ?: return
        finishSession(current)
        _uiState.update { it.copy(
            activeTypeId = null,
            elapsedTime = formatElapsed(0L),
            logTypes = buildLogTypeUiItems(activeTypeId = null),
        ) }
    }

    private fun startSession(typeId: Int) {
        timerJob?.cancel()
        sessionStartTime = LocalDateTime.now()
        _uiState.update { it.copy(
            activeTypeId = typeId,
            elapsedTime = formatElapsed(0L),
            logTypes = buildLogTypeUiItems(activeTypeId = typeId),
        ) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1.seconds)
                val elapsed = ChronoUnit.SECONDS.between(sessionStartTime, LocalDateTime.now())
                _uiState.update { state ->
                    state.copy(
                        elapsedTime = formatElapsed(elapsed),
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
    }
}
