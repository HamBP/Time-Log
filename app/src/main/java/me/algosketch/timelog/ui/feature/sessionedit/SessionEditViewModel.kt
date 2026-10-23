package me.algosketch.timelog.ui.feature.sessionedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import me.algosketch.timelog.data.ActiveSessionHolder
import me.algosketch.timelog.data.LogRepository
import me.algosketch.timelog.data.local.entity.LogSessionEntity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SessionEditViewModel @Inject constructor(
    private val repository: LogRepository,
    private val activeSessionHolder: ActiveSessionHolder,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionEditUiState())
    val uiState: StateFlow<SessionEditUiState> = _uiState.asStateFlow()

    private var sessionId: Int? = null

    fun load(sessionId: Int) {
        if (this.sessionId == sessionId) return
        this.sessionId = sessionId
        viewModelScope.launch {
            val session = repository.getSession(sessionId) ?: return@launch
            val type = repository.getLogType(session.typeId)
            val (minStart, maxEnd) = computeBounds(session)
            _uiState.value = SessionEditUiState(
                isLoaded = true,
                typeName = type?.name ?: "",
                colorHex = type?.colorHex ?: "",
                icon = type?.icon ?: "",
                includeEfficiency = type?.includeEfficiency ?: false,
                date = session.startedAt.toLocalDate(),
                startTime = session.startedAt.toLocalTime(),
                endTime = session.endedAt.toLocalTime(),
                minStartTime = minStart,
                maxEndTime = maxEnd,
            )
        }
    }

    fun onStartTimeChange(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun onEndTimeChange(time: LocalTime) {
        _uiState.update { it.copy(endTime = time) }
    }

    fun onConfirm(onDone: () -> Unit) {
        val id = sessionId ?: return
        viewModelScope.launch {
            val session = repository.getSession(id) ?: return@launch
            // 화면을 연 사이 다른 세션이 생겼을 수 있으므로 범위를 다시 계산해 검증한다.
            val (minStart, maxEnd) = computeBounds(session)
            val state = _uiState.updateAndGet { it.copy(minStartTime = minStart, maxEndTime = maxEnd) }
            if (!state.canSave) return@launch
            repository.updateSessionTime(
                id = id,
                startedAt = state.date.atTime(state.startTime),
                endedAt = state.date.atTime(state.endTime),
            )
            onDone()
        }
    }

    /**
     * 시작 시간 하한과 종료 시간 상한을 구한다. 세션은 자정 기준으로 나뉘어 저장되므로 같은 날 안으로 제한한다.
     * 기존 값이 이미 범위를 벗어나 있으면(시계 변경 등) 기존 값까지는 허용해 사용자가 갇히지 않게 한다.
     */
    private suspend fun computeBounds(session: LogSessionEntity): Pair<LocalTime, LocalTime> {
        val date = session.startedAt.toLocalDate()
        val (previous, next) = repository.getAdjacentSessions(session)

        val lower = listOfNotNull(previous?.endedAt, date.atStartOfDay()).max()
        val upper = listOfNotNull(
            next?.startedAt,
            activeSessionHolder.startedAt.value,
            LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
            date.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS),
        ).min()

        return minOf(lower, session.startedAt).toLocalTime() to maxOf(upper, session.endedAt).toLocalTime()
    }
}
