package me.algosketch.timelog.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 진행 중인 세션의 시작 시각. 진행 중 세션은 정지 시점에만 DB에 저장되므로,
 * 저장된 세션을 수정할 때 "다음 세션 시작 시각"으로 쓰기 위해 메모리에 공유한다.
 */
@Singleton
class ActiveSessionHolder @Inject constructor() {
    private val _startedAt = MutableStateFlow<LocalDateTime?>(null)
    val startedAt: StateFlow<LocalDateTime?> = _startedAt.asStateFlow()

    fun set(startedAt: LocalDateTime?) {
        _startedAt.value = startedAt
    }
}
