package me.algosketch.timelog.ui.feature.historydetail

data class HistoryDetailUiState(
    val dateLabel: String = "",
    val totalWorkLabel: String = "",
    val totalRestLabel: String = "",
    val sessions: List<SessionItem> = emptyList(),
) {
    val totalSecs: Long = sessions.sumOf { it.durationSecs }

    val workSecs: Long = sessions.filter { it.includeEfficiency }.sumOf { it.durationSecs }

    val efficiency: Int =
        if (totalSecs > 0) (workSecs * 100L / totalSecs).toInt() else 0
}

/**
 * 상세 화면에 표시할 세션 한 건.
 * colorHex/includeEfficiency 는 그대로 두고, 실제 색상 해석(효율 미포함 = 중립색)은 UI 경계에서 수행한다.
 */
data class SessionItem(
    val typeName: String,
    val colorHex: String,
    val icon: String,
    val includeEfficiency: Boolean,
    val timeRange: String,
    val durationLabel: String,
    val durationSecs: Long,
)
