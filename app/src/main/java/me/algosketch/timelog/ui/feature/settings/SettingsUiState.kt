package me.algosketch.timelog.ui.feature.settings

import androidx.compose.ui.graphics.Color

val colorOptions = listOf(
    Color(0xFF4ADE80),
    Color(0xFFFB923C),
    Color(0xFFA78BFA),
    Color(0xFF60A5FA),
    Color(0xFFF472B6),
    Color(0xFFFBBF24),
)

val iconOptions = listOf(
    "edit_note", "menu_book", "lightbulb", "track_changes", "directions_run", "palette",
    "laptop", "music_note", "bolt", "star",
)

data class LogType(
    val name: String,
    val icon: String,
    val color: Color,
    val includeEfficiency: Boolean,
    val id: Int = 0,
)

data class SettingsUiState(
    val logTypes: List<LogType> = emptyList(),
    val showAddForm: Boolean = false,
    val newTypeName: String = "",
    val selectedColorIndex: Int = 2,
    val selectedIconIndex: Int = 2,
)
