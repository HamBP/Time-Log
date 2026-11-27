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

/** 삭제 버튼을 눌렀을 때 띄우는 확인 다이얼로그. 관련 기록이 있으면 삭제 대신 숨김 처리한다. */
enum class DeleteDialog { Delete, Hide }

data class LogTypeFormUiState(
    val name: String = "",
    val selectedIconIndex: Int = 1,
    val selectedColorIndex: Int = 2,
    val includeEfficiency: Boolean = true,
    val isEditMode: Boolean = false,
    // 숨김 처리된 타입을 수정 중이면 저장·삭제 대신 복구 버튼을 보여준다.
    val isHidden: Boolean = false,
    val deleteDialog: DeleteDialog? = null,
)
