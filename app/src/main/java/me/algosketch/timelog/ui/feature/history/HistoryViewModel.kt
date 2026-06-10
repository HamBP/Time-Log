package me.algosketch.timelog.ui.feature.history

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HistoryUiState(
            records = listOf(
                DailyRecord("6월 6일 (토)", 8, 80, "4시간 0분", "1시간 0분"),
                DailyRecord("6월 5일 (금)", 12, 77, "5시간 0분", "1시간 30분"),
                DailyRecord("6월 4일 (목)", 6, 81, "3시간 30분", "50분"),
                DailyRecord("6월 3일 (수)", 10, 79, "4시간 30분", "1시간 10분"),
            )
        )
    )
    val uiState = _uiState.asStateFlow()
}
