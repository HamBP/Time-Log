package me.algosketch.timelog.ui.feature.history

data class HistoryUiState(
    val records: List<DailyRecord> = emptyList()
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
)
