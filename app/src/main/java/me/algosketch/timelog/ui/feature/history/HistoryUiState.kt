package me.algosketch.timelog.ui.feature.history

data class HistoryUiState(
    val records: List<DailyRecord> = emptyList()
) {
    val monthlyEfficiency: Int =
        if (records.isEmpty()) 0 else records.sumOf { it.efficiency } / records.size

    val totalWorkTime: Int = records.sumOf { parseMinutes(it.workTime) }

    val recordedDays: Int = records.size
}

private fun parseMinutes(timeStr: String): Int {
    val hours = Regex("""(\d+)시간""").find(timeStr)?.groupValues?.get(1)?.toInt() ?: 0
    val mins = Regex("""(\d+)분""").find(timeStr)?.groupValues?.get(1)?.toInt() ?: 0
    return hours * 60 + mins
}

data class DailyRecord(
    val date: String,
    val sessionCount: Int,
    val efficiency: Int,
    val workTime: String,
    val restTime: String,
)
