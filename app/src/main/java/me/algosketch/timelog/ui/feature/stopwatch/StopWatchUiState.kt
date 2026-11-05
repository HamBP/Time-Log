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

/** 세션 기록 목록의 한 줄. 세션 카드 또는 세션 사이의 공백. */
sealed interface SessionLogItem

data class SessionEntry(
    val id: Int,
    val typeName: String,
    val color: Color,
    val time: String,
    val duration: String,
) : SessionLogItem

/** 연속한 두 세션 사이 간격이 5분 이상일 때 그 사이에 표시하는 공백. */
data class SessionGap(
    val duration: String,
) : SessionLogItem

data class TodaySummary(
    val workTime: String,
    val efficiency: String,
    val restTime: String,
    val efficiencyRatio: Float,
)

data class StopWatchUiState(
    val activeTypeId: Int? = null,
    // 정지 상태에서 시작 버튼으로 이어서 기록할 타입 (마지막으로 기록한 타입)
    val lastTypeId: Int? = null,
    // 타이머 카드의 큰 시간: 기록 중이면 현재 세션 경과 시간, 정지 상태면 마지막 세션 길이
    val currentSessionTime: String = "00:00",
    val logTypes: List<LogTypeUiItem> = emptyList(),
    val todaySummary: TodaySummary? = null,
    val sessions: List<SessionLogItem> = emptyList(),
)
