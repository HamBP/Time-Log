package me.algosketch.timelog.data.local.entity

data class DailyAggregate(
    val date: String,
    val sessionCount: Int,
    val totalSecs: Long,
    val workSecs: Long,
    val restSecs: Long,
)
