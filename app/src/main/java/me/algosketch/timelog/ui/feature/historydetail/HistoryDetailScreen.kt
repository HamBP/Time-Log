package me.algosketch.timelog.ui.feature.historydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.algosketch.timelog.ui.theme.Background

// TODO(Commit 3): Figma `history > detail` 프레임 기반 상세 UI 구현
@Composable
fun HistoryDetailScreen(
    date: String,
    onBack: () -> Unit = {},
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    )
}
