package me.algosketch.timelog.ui.feature.stopwatch

import androidx.compose.ui.graphics.Color

data class LogTypeUiItem(
    val id: Int,
    val name: String,
    val icon: String,
    val color: Color,
    val accumulatedTime: String,
    val isActive: Boolean,
    val includeEfficiency: Boolean,
)

data class SessionEntry(
    val typeName: String,
    val color: Color,
    val time: String,
    val duration: String,
)

data class TodaySummary(
    val workTime: String,
    val efficiency: String,
    val restTime: String,
    val efficiencyRatio: Float,
)

data class StopWatchUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val activeTypeId: Int? = null,
    val elapsedTime: String = "00:00",
    val logTypes: List<LogTypeUiItem> = emptyList(),
    val todaySummary: TodaySummary? = null,
    val sessions: List<SessionEntry> = emptyList(),
)
