package me.algosketch.timelog.ui.feature.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.DailyAggregate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: LogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getDailyAggregatesFlow().collect { aggregates ->
                _uiState.update { state ->
                    state.copy(records = aggregates.map { it.toDailyRecord() })
                }
            }
        }
    }

    private fun DailyAggregate.toDailyRecord(): DailyRecord {
        val date = LocalDate.parse(this.date)
        val pattern = context.getString(R.string.date_format_pattern)
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        val efficiency = if (totalSecs > 0) (workSecs * 100L / totalSecs).toInt() else 0
        return DailyRecord(
            date = date.format(formatter),
            sessionCount = sessionCount,
            efficiency = efficiency,
            workTime = formatDuration(workSecs),
            restTime = formatDuration(restSecs),
            workSecs = workSecs,
            rawDate = this.date,
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
