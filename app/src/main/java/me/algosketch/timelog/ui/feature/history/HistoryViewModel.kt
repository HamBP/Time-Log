package me.algosketch.timelog.ui.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.DailyAggregate
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
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
}

private fun DailyAggregate.toDailyRecord(): DailyRecord {
    val date = LocalDate.parse(this.date)
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "월"; 2 -> "화"; 3 -> "수"; 4 -> "목"; 5 -> "금"; 6 -> "토"; else -> "일"
    }
    val efficiency = if (totalSecs > 0) (workSecs * 100L / totalSecs).toInt() else 0
    return DailyRecord(
        date = "${date.monthValue}월 ${date.dayOfMonth}일 ($dayOfWeek)",
        sessionCount = sessionCount,
        efficiency = efficiency,
        workTime = formatDuration(workSecs),
        restTime = formatDuration(restSecs),
    )
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return buildString {
        if (hours > 0) append("${hours}시간 ")
        append("${minutes}분")
    }
}
