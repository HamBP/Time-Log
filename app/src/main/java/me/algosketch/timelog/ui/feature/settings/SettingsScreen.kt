package me.algosketch.timelog.ui.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.TextTertiary

@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "설정", color = TextTertiary)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewSettingsScreen() {
    SettingsScreen()
}
