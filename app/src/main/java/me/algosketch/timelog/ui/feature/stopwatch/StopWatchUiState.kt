package me.algosketch.timelog.ui.feature.stopwatch

data class StopWatchUiState(
    val currentTime: String = "",
    val currentDate: String = "",
    val timerState: TimerState = TimerState.IDLE,
    val elapsedTime: String = "00:00",
    val workAccumulatedTime: String = "00:00",
    val restAccumulatedTime: String = "00:00",
    val todaySummary: TodaySummary? = null,
    val sessions: List<SessionEntry> = emptyList()
)
