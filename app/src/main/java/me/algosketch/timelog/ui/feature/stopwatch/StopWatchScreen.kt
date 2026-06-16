package me.algosketch.timelog.ui.feature.stopwatch

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.RestOrange
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen

@Composable
fun StopWatchScreen(viewModel: StopWatchViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StopWatchContent(
        uiState = uiState,
        onWorkClick = viewModel::onWorkClick,
        onRestClick = viewModel::onRestClick,
        onStopClick = viewModel::onStopClick,
    )
}

@Composable
private fun StopWatchContent(
    uiState: StopWatchUiState,
    onWorkClick: () -> Unit,
    onRestClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        item {
            AppHeader(currentTime = uiState.currentTime, currentDate = uiState.currentDate)
        }
        item {
            TimerDisplay(
                timerState = uiState.timerState,
                elapsedTime = uiState.elapsedTime,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item {
            ActionButtonList(
                timerState = uiState.timerState,
                workAccumulatedTime = uiState.workAccumulatedTime,
                restAccumulatedTime = uiState.restAccumulatedTime,
                onWorkClick = onWorkClick,
                onRestClick = onRestClick,
                onStopClick = onStopClick,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
            )
        }
        if (uiState.todaySummary != null) {
            item {
                TodaySummaryCard(
                    summary = uiState.todaySummary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            }
        }
        if (uiState.sessions.isNotEmpty()) {
            item {
                SessionLogSection(
                    sessions = uiState.sessions,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp)
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AppHeader(currentTime: String, currentDate: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "타임 로그",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.88.sp,
                color = TextTertiary,
                lineHeight = 16.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = currentDate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 19.5.sp
            )
        }
        Text(
            text = currentTime,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 19.5.sp
        )
    }
}

@Composable
private fun TimerDisplay(
    timerState: TimerState,
    elapsedTime: String,
    modifier: Modifier = Modifier
) {
    val accentColor = when (timerState) {
        TimerState.WORK -> WorkGreen
        TimerState.REST -> RestOrange
        TimerState.IDLE -> TextTertiary
    }
    val bgColor = when (timerState) {
        TimerState.WORK -> WorkGreen.copy(alpha = 0.08f)
        TimerState.REST -> RestOrange.copy(alpha = 0.08f)
        TimerState.IDLE -> TextTertiary.copy(alpha = 0.06f)
    }
    val borderColor = when (timerState) {
        TimerState.WORK -> WorkGreen.copy(alpha = 0.25f)
        TimerState.REST -> RestOrange.copy(alpha = 0.25f)
        TimerState.IDLE -> TextTertiary.copy(alpha = 0.15f)
    }
    val statusLabel = when (timerState) {
        TimerState.WORK -> "집중 모드"
        TimerState.REST -> "휴식 모드"
        TimerState.IDLE -> "대기 중"
    }
    val hintText = when (timerState) {
        TimerState.IDLE -> "버튼을 눌러 시작하세요"
        else -> "현재 세션"
    }
    val dotAlpha = if (timerState != TimerState.IDLE) 0.35f else 1f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 25.dp, vertical = 29.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = dotAlpha))
            )
            Text(
                text = statusLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = accentColor,
                letterSpacing = 0.72.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = elapsedTime,
            fontSize = 54.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = accentColor,
            letterSpacing = (-1.08).sp,
            textAlign = TextAlign.Center,
            lineHeight = 54.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = hintText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun ActionButtonList(
    timerState: TimerState,
    workAccumulatedTime: String,
    restAccumulatedTime: String,
    onWorkClick: () -> Unit,
    onRestClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimerActionButton(
            icon = Icons.Default.PlayArrow,
            title = "일하는 중",
            subtitle = "집중 시간 기록",
            time = workAccumulatedTime,
            isActive = timerState == TimerState.WORK,
            activeColor = WorkGreen,
            onClick = onWorkClick
        )
        TimerActionButton(
            icon = Icons.Default.FreeBreakfast,
            title = "쉬는 중",
            subtitle = "휴식 시간 기록",
            time = restAccumulatedTime,
            isActive = timerState == TimerState.REST,
            activeColor = RestOrange,
            onClick = onRestClick
        )
        StopActionButton(onClick = onStopClick)
    }
}

@Composable
private fun TimerActionButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    time: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isActive) activeColor.copy(alpha = 0.10f) else Surface
    val borderColor = if (isActive) activeColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.06f)
    val iconBgColor = if (isActive) activeColor.copy(alpha = 0.13f) else Color.White.copy(alpha = 0.04f)
    val textColor = if (isActive) activeColor else TextSecondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = textColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                lineHeight = 21.sp
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.55f),
                lineHeight = 16.5.sp
            )
        }
        Text(
            text = time,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            color = textColor,
            lineHeight = 19.5.sp
        )
    }
}

@Composable
private fun StopActionButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TextTertiary.copy(alpha = 0.07f))
            .border(1.dp, TextTertiary.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TextTertiary.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Stop,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TextSecondary
            )
        }
        Column {
            Text(
                text = "정지",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                lineHeight = 21.sp
            )
            Text(
                text = "타이머 중지",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary.copy(alpha = 0.55f),
                lineHeight = 16.5.sp
            )
        }
    }
}

@Composable
private fun TodaySummaryCard(
    summary: TodaySummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Text(
            text = "오늘 요약",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            letterSpacing = 0.66.sp,
            lineHeight = 16.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.06f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(summary.efficiencyRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(WorkGreen)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "작업",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = WorkGreen,
                    lineHeight = 16.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = summary.workTime,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    lineHeight = 22.5.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "효율",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = summary.efficiency,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.5.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "휴식",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = RestOrange,
                    textAlign = TextAlign.End,
                    lineHeight = 16.5.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = summary.restTime,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    textAlign = TextAlign.End,
                    lineHeight = 22.5.sp
                )
            }
        }
    }
}

@Composable
private fun SessionLogSection(
    sessions: List<SessionEntry>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "세션 기록",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            letterSpacing = 0.66.sp,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            sessions.forEach { session ->
                SessionItem(session = session)
            }
        }
    }
}

@Composable
private fun SessionItem(session: SessionEntry) {
    val dotColor = when (session.type) {
        TimerState.WORK -> WorkGreen
        TimerState.REST -> RestOrange
        TimerState.IDLE -> TextTertiary
    }
    val durationColor = dotColor
    val typeName = when (session.type) {
        TimerState.WORK -> "일하는 중"
        TimerState.REST -> "쉬는 중"
        TimerState.IDLE -> "대기 중"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(dotColor)
        )
        Text(
            text = typeName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
            lineHeight = 18.sp
        )
        Text(
            text = session.time,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = session.duration,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = durationColor,
            lineHeight = 18.sp
        )
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Inactive")
@Composable
private fun PreviewInactive() {
    StopWatchContent(
        uiState = StopWatchUiState(currentTime = "오전 03:22", currentDate = "6월 7일 (일)"),
        onWorkClick = {}, onRestClick = {}, onStopClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Active - Work")
@Composable
private fun PreviewActiveWork() {
    StopWatchContent(
        uiState = StopWatchUiState(
            currentTime = "오전 12:17",
            currentDate = "6월 11일 (목)",
            timerState = TimerState.WORK,
            elapsedTime = "00:02",
            workAccumulatedTime = "00:02"
        ),
        onWorkClick = {}, onRestClick = {}, onStopClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Logged")
@Composable
private fun PreviewLogged() {
    StopWatchContent(
        uiState = StopWatchUiState(
            currentTime = "오전 03:23",
            currentDate = "6월 7일 (일)",
            timerState = TimerState.IDLE,
            workAccumulatedTime = "00:03",
            restAccumulatedTime = "00:03",
            todaySummary = TodaySummary(
                workTime = "00:03",
                efficiency = "48%",
                restTime = "00:03",
                efficiencyRatio = 0.48f
            ),
            sessions = listOf(
                SessionEntry(type = TimerState.WORK, time = "오전 03:23", duration = "00:01"),
                SessionEntry(type = TimerState.REST, time = "오전 03:23", duration = "00:00"),
                SessionEntry(type = TimerState.REST, time = "오전 03:23", duration = "00:03"),
                SessionEntry(type = TimerState.WORK, time = "오전 03:23", duration = "00:02"),
            )
        ),
        onWorkClick = {}, onRestClick = {}, onStopClick = {}
    )
}
