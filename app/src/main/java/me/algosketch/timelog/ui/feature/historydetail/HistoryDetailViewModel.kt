package me.algosketch.timelog.ui.feature.historydetail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.SessionDetail
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun load(date: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getSessionsForDateFlow(date).collect { sessions ->
                _uiState.value = buildState(date, sessions)
            }
        }
    }

    private fun buildState(date: String, sessions: List<SessionDetail>): HistoryDetailUiState {
        val dateLabel = runCatching {
            val pattern = context.getString(R.string.date_format_pattern)
            LocalDate.parse(date)
                .format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
        }.getOrDefault(date)

        val timeFormatter = DateTimeFormatter.ofPattern(
            context.getString(R.string.time_format_pattern),
            Locale.getDefault(),
        )

        val items = sessions.map { session ->
            val secs = Duration.between(session.startedAt, session.endedAt).seconds
            SessionItem(
                typeName = session.typeName,
                colorHex = session.colorHex,
                icon = session.icon,
                includeEfficiency = session.includeEfficiency,
                timeRange = context.getString(
                    R.string.session_time_range,
                    session.startedAt.format(timeFormatter),
                    session.endedAt.format(timeFormatter),
                ),
                durationLabel = formatDuration(secs),
                durationSecs = secs,
            )
        }

        val workSecs = items.filter { it.includeEfficiency }.sumOf { it.durationSecs }
        val restSecs = items.filterNot { it.includeEfficiency }.sumOf { it.durationSecs }

        return HistoryDetailUiState(
            dateLabel = dateLabel,
            totalWorkLabel = formatDuration(workSecs),
            totalRestLabel = formatDuration(restSecs),
            sessions = items,
        )
    }

    private fun formatDuration(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        return if (hours > 0) {
            context.getString(R.string.duration_hours_and_minutes, hours, minutes)
        } else {
            context.getString(R.string.duration_minutes_only, minutes)
        }
    }
}
