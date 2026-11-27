package me.algosketch.timelog.ui.feature.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.DailyAggregate
import java.time.LocalDate
import java.time.YearMonth
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

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    init {
        viewModelScope.launch {
            combine(repository.getDailyAggregatesFlow(), selectedMonth) { aggregates, month ->
                aggregates to month
            }.collect { (aggregates, month) ->
                // 집계는 날짜 내림차순이므로 마지막 항목이 가장 오래된 기록이다.
                val earliestMonth = aggregates.lastOrNull()?.let { YearMonth.from(LocalDate.parse(it.date)) }
                val currentMonth = YearMonth.now()
                _uiState.update { state ->
                    state.copy(
                        monthLabel = formatMonth(month, currentMonth),
                        canGoPrevMonth = earliestMonth != null && month > earliestMonth,
                        canGoNextMonth = month < currentMonth,
                        records = aggregates
                            .filter { YearMonth.from(LocalDate.parse(it.date)) == month }
                            .map { it.toDailyRecord() },
                    )
                }
            }
        }
    }

    fun onPrevMonthClick() {
        if (!_uiState.value.canGoPrevMonth) return
        selectedMonth.update { it.minusMonths(1) }
    }

    fun onNextMonthClick() {
        if (!_uiState.value.canGoNextMonth) return
        selectedMonth.update { it.plusMonths(1) }
    }

    /** 올해는 월만, 다른 해는 연도까지 표시한다. */
    private fun formatMonth(month: YearMonth, currentMonth: YearMonth): String {
        val patternRes = if (month.year == currentMonth.year) {
            R.string.month_format_pattern
        } else {
            R.string.year_month_format_pattern
        }
        val formatter = DateTimeFormatter.ofPattern(context.getString(patternRes), Locale.getDefault())
        return month.format(formatter)
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
