package me.algosketch.timelog.ui.feature.settings

import androidx.compose.ui.graphics.Color

data class LogType(
    val name: String,
    val icon: String,
    val color: Color,
    val includeEfficiency: Boolean,
    val id: Int = 0,
)

data class SettingsUiState(
    val logTypes: List<LogType> = emptyList(),
)
