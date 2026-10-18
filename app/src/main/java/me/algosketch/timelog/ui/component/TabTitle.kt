package me.algosketch.timelog.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.algosketch.timelog.ui.theme.TextPrimary

/** 하단 탭 최상위 화면(기록·히스토리·설정)의 공통 제목. */
@Composable
fun TabTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        letterSpacing = (-0.22).sp,
        lineHeight = 33.sp,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
    )
}
