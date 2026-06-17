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
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import me.algosketch.timelog.R
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
import me.algosketch.timelog.ui.util.toMaterialIcon

@Composable
fun StopWatchScreen(viewModel: StopWatchViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StopWatchContent(
        uiState = uiState,
        onTypeClick = viewModel::onTypeClick,
        onStopClick = viewModel::onStopClick,
    )
}

@Composable
private fun StopWatchContent(
    uiState: StopWatchUiState,
    onTypeClick: (Int) -> Unit,
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
                activeType = uiState.logTypes.firstOrNull { it.isActive },
                elapsedTime = uiState.elapsedTime,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item {
            ActionButtonList(
                logTypes = uiState.logTypes,
                onTypeClick = onTypeClick,
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
                text = stringResource(R.string.app_header_label),
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
    activeType: LogTypeUiItem?,
    elapsedTime: String,
    modifier: Modifier = Modifier,
) {
    val accentColor = activeType?.color ?: TextTertiary
    val bgColor = if (activeType != null) accentColor.copy(alpha = 0.08f) else TextTertiary.copy(alpha = 0.06f)
    val borderColor = if (activeType != null) accentColor.copy(alpha = 0.25f) else TextTertiary.copy(alpha = 0.15f)
    val statusLabel = activeType?.name ?: stringResource(R.string.status_waiting)
    val hintText = if (activeType != null) stringResource(R.string.status_current_session) else stringResource(R.string.hint_tap_to_start)
    val dotAlpha = if (activeType != null) 0.35f else 1f

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
    logTypes: List<LogTypeUiItem>,
    onTypeClick: (Int) -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        logTypes.forEach { logType ->
            TimerActionButton(
                icon = logType.icon.toMaterialIcon(),
                title = logType.name,
                subtitle = if (logType.includeEfficiency) stringResource(R.string.label_focus_record) else stringResource(R.string.label_rest_record),
                time = logType.accumulatedTime,
                isActive = logType.isActive,
                activeColor = logType.color,
                onClick = { onTypeClick(logType.id) }
            )
        }
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
    onClick: () -> Unit,
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
                text = stringResource(R.string.btn_stop),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                lineHeight = 21.sp
            )
            Text(
                text = stringResource(R.string.btn_stop_subtitle),
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
    modifier: Modifier = Modifier,
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
            text = stringResource(R.string.today_summary_title),
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
                Text(text = stringResource(R.string.label_work), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = WorkGreen, lineHeight = 16.5.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = summary.workTime, fontSize = 15.sp, fontFamily = FontFamily.Monospace, color = TextPrimary, lineHeight = 22.5.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(R.string.label_efficiency), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = TextTertiary, textAlign = TextAlign.Center, lineHeight = 16.5.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = summary.efficiency, fontSize = 15.sp, fontFamily = FontFamily.Monospace, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.5.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = stringResource(R.string.label_rest), fontSize = 11.sp, fontWeight = FontWeight.Normal, color = RestOrange, textAlign = TextAlign.End, lineHeight = 16.5.sp)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = summary.restTime, fontSize = 15.sp, fontFamily = FontFamily.Monospace, color = TextPrimary, textAlign = TextAlign.End, lineHeight = 22.5.sp)
            }
        }
    }
}

@Composable
private fun SessionLogSection(
    sessions: List<SessionEntry>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.session_log_title),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            letterSpacing = 0.66.sp,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            sessions.forEach { session -> SessionItem(session = session) }
        }
    }
}

@Composable
private fun SessionItem(session: SessionEntry) {
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
                .background(session.color)
        )
        Text(
            text = session.typeName,
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
            color = session.color,
            lineHeight = 18.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Inactive")
@Composable
private fun PreviewInactive() {
    StopWatchContent(
        uiState = StopWatchUiState(
            currentTime = "오전 03:22",
            currentDate = "6월 7일 (일)",
            logTypes = listOf(
                LogTypeUiItem(1, "일하는 중", "▶", Color(0xFF4ADE80), "00:00", false, true),
                LogTypeUiItem(2, "쉬는 중", "☕", Color(0xFFFB923C), "00:00", false, false),
            )
        ),
        onTypeClick = {}, onStopClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Active - Work")
@Composable
private fun PreviewActiveWork() {
    StopWatchContent(
        uiState = StopWatchUiState(
            currentTime = "오전 12:17",
            currentDate = "6월 11일 (목)",
            activeTypeId = 1,
            elapsedTime = "00:02",
            logTypes = listOf(
                LogTypeUiItem(1, "일하는 중", "▶", Color(0xFF4ADE80), "00:02", true, true),
                LogTypeUiItem(2, "쉬는 중", "☕", Color(0xFFFB923C), "00:00", false, false),
            )
        ),
        onTypeClick = {}, onStopClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11, name = "Logged")
@Composable
private fun PreviewLogged() {
    StopWatchContent(
        uiState = StopWatchUiState(
            currentTime = "오전 03:23",
            currentDate = "6월 7일 (일)",
            logTypes = listOf(
                LogTypeUiItem(1, "일하는 중", "▶", Color(0xFF4ADE80), "00:03", false, true),
                LogTypeUiItem(2, "쉬는 중", "☕", Color(0xFFFB923C), "00:03", false, false),
            ),
            todaySummary = TodaySummary("00:03", "48%", "00:03", 0.48f),
            sessions = listOf(
                SessionEntry("일하는 중", Color(0xFF4ADE80), "오전 03:23", "00:01"),
                SessionEntry("쉬는 중", Color(0xFFFB923C), "오전 03:23", "00:00"),
                SessionEntry("쉬는 중", Color(0xFFFB923C), "오전 03:23", "00:03"),
                SessionEntry("일하는 중", Color(0xFF4ADE80), "오전 03:23", "00:02"),
            )
        ),
        onTypeClick = {}, onStopClick = {}
    )
}
