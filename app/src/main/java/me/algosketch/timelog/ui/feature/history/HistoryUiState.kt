package me.algosketch.timelog.ui.feature.history

data class HistoryUiState(
    // 선택된 달 이름 (예: "11월", 올해가 아니면 "2025년 12월")
    val monthLabel: String = "",
    // 이전 달 이동 가능 여부: 가장 오래된 기록이 있는 달보다 뒤일 때만
    val canGoPrevMonth: Boolean = false,
    // 다음 달 이동 가능 여부: 이번 달보다 앞일 때만
    val canGoNextMonth: Boolean = false,
    // 선택된 달의 일별 기록
    val records: List<DailyRecord> = emptyList(),
) {
    val monthlyEfficiency: Int =
        if (records.isEmpty()) 0 else records.sumOf { it.efficiency } / records.size

    val totalWorkTime: Int = records.sumOf { (it.workSecs / 60).toInt() }

    val recordedDays: Int = records.size
}

data class DailyRecord(
    val date: String,
    val sessionCount: Int,
    val efficiency: Int,
    val workTime: String,
    val restTime: String,
    val workSecs: Long = 0L,
    val rawDate: String = "", // 상세 페이지 조회/재포맷용 ISO 날짜 (yyyy-MM-dd)
)
