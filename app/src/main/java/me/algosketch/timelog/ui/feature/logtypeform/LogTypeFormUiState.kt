package me.algosketch.timelog.ui.feature.logtypeform

import androidx.compose.ui.graphics.Color

/** 색상 팔레트 (Figma settings > new type 색상 섹션 순서). 기본 선택은 인덱스 2(보라). */
val colorOptions = listOf(
    Color(0xFF4ADE80), // green
    Color(0xFF60A5FA), // blue
    Color(0xFFA78BFA), // purple
    Color(0xFFF472B6), // pink
    Color(0xFFFBBF24), // amber
    Color(0xFFF87171), // coral
)

/** 아이콘 옵션 (Figma 아이콘 섹션 순서, 6열 기준). */
val iconOptions = listOf(
    "play_arrow", "edit_note", "menu_book", "lightbulb", "track_changes", "directions_run",
    "palette", "laptop", "music_note", "bolt", "star", "favorite",
    "free_breakfast",
)

data class LogTypeFormUiState(
    val name: String = "",
    val selectedIconIndex: Int = 1,
    val selectedColorIndex: Int = 2,
    val includeEfficiency: Boolean = true,
    val isEditMode: Boolean = false,
)
