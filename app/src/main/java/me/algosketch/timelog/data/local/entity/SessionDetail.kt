package me.algosketch.timelog.data.local.entity

import java.time.LocalDateTime

/**
 * 특정 날짜의 개별 세션 한 건을 로그 타입 정보와 함께 조회한 projection.
 * startedAt/endedAt 은 epoch seconds 컬럼이 Converters 를 통해 LocalDateTime 으로 변환된다.
 */
data class SessionDetail(
    val typeName: String,
    val colorHex: String,
    val icon: String,
    val includeEfficiency: Boolean,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
)
