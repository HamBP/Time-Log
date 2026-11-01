package me.algosketch.timelog.ui.feature.sessionedit

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

data class SessionEditUiState(
    val isLoaded: Boolean = false,
    val typeName: String = "",
    val colorHex: String = "",
    val icon: String = "",
    val includeEfficiency: Boolean = true,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.MIDNIGHT,
    val endTime: LocalTime = LocalTime.MIDNIGHT,
    // 시작 시간 하한: 이전 세션 종료 시각 (없으면 그날 0시)
    val minStartTime: LocalTime = LocalTime.MIDNIGHT,
    // 종료 시간 상한: 다음 세션(진행 중 포함) 시작 시각과 현재 시각 중 이른 값
    val maxEndTime: LocalTime = LocalTime.MIDNIGHT,
) {
    // 시작 시간의 상한은 종료 시간, 종료 시간의 하한은 시작 시간이다.
    val isStartValid: Boolean = startTime >= minStartTime && startTime < endTime

    val isEndValid: Boolean = endTime > startTime && endTime <= maxEndTime

    val canSave: Boolean = isLoaded && isStartValid && isEndValid

    val durationSecs: Long =
        if (endTime > startTime) Duration.between(startTime, endTime).seconds else 0L
}
