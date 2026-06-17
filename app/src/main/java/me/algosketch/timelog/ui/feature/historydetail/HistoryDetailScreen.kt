package me.algosketch.timelog.ui.feature.historydetail

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.algosketch.timelog.R
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.RestOrange
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen
import me.algosketch.timelog.ui.theme.toComposeColor
import me.algosketch.timelog.ui.util.toMaterialIcon

/** 효율에 포함되지 않는 로그 타입(쉬는 중 등)은 고유 색상 대신 이 중립색으로 표시한다. */
private val NeutralSessionColor = TextTertiary

private val cardBorderColor = Color.White.copy(alpha = 0.06f)

@Composable
fun HistoryDetailScreen(
    date: String,
    onBack: () -> Unit = {},
    viewModel: HistoryDetailViewModel = viewModel(),
) {
    LaunchedEffect(date) { viewModel.load(date) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryDetailContent(uiState = uiState, onBack = onBack)
}

@Composable
private fun HistoryDetailContent(
    uiState: HistoryDetailUiState,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        item {
            DetailHeader(dateLabel = uiState.dateLabel, onBack = onBack)
        }
        item {
            SummaryCard(
                uiState = uiState,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item {
            SessionListSection(
                sessions = uiState.sessions,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DetailHeader(
    dateLabel: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 24.dp, top = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            tint = TextSecondary,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(8.dp)
                .size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = stringResource(R.string.history_detail_label),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.88.sp,
                color = TextTertiary,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 30.sp
            )
        }
    }
}

@Composable
private fun SummaryCard(
    uiState: HistoryDetailUiState,
    modifier: Modifier = Modifier,
) {
    val efficiencyColor = if (uiState.efficiency >= 80) WorkGreen else RestOrange

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = stringResource(R.string.label_efficiency),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${uiState.efficiency}%",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    color = efficiencyColor,
                    lineHeight = 34.sp
                )
            }
            Text(
                text = stringResource(R.string.session_count, uiState.sessions.size),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SegmentedEfficiencyBar(sessions = uiState.sessions)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.label_work),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = WorkGreen,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.totalWorkLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 21.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.label_rest),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = RestOrange,
                    textAlign = TextAlign.End,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = uiState.totalRestLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

/** 각 세션의 고유 색상을 지속시간 비율만큼 개별 세그먼트로 표시한다(효율 미포함 세션은 중립색). */
@Composable
private fun SegmentedEfficiencyBar(
    sessions: List<SessionItem>,
    modifier: Modifier = Modifier,
) {
    val barModifier = modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))

    if (sessions.isEmpty()) {
        Box(modifier = barModifier.background(Color.White.copy(alpha = 0.05f)))
        return
    }

    Row(
        modifier = barModifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        sessions.forEach { session ->
            val color = if (session.includeEfficiency) {
                session.colorHex.toComposeColor()
            } else {
                NeutralSessionColor
            }
            Box(
                modifier = Modifier
                    .weight(session.durationSecs.coerceAtLeast(1L).toFloat())
                    .fillMaxHeight()
                    .background(color)
            )
        }
    }
}

@Composable
private fun SessionListSection(
    sessions: List<SessionItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.session_log_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.66.sp,
            color = TextTertiary,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface)
                    .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_detail_empty),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 21.sp
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            sessions.forEach { session ->
                SessionRow(session = session)
            }
        }
    }
}

@Composable
private fun SessionRow(session: SessionItem) {
    val color = if (session.includeEfficiency) {
        session.colorHex.toComposeColor()
    } else {
        NeutralSessionColor
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = session.icon.toMaterialIcon(),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.typeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = session.timeRange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = session.durationLabel,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = TextSecondary,
            lineHeight = 21.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewHistoryDetailScreen() {
    HistoryDetailContent(
        uiState = HistoryDetailUiState(
            dateLabel = "6월 6일 (토)",
            totalWorkLabel = "4시간 0분",
            totalRestLabel = "1시간 0분",
            sessions = listOf(
                SessionItem("집중", "#4ADE80", "play_arrow", true, "오전 9:00 - 오전 10:30", "1시간 30분", 5400),
                SessionItem("쉬는 중", "#FB923C", "free_breakfast", false, "오전 10:30 - 오전 11:00", "30분", 1800),
                SessionItem("공부", "#60A5FA", "menu_book", true, "오전 11:00 - 오후 1:30", "2시간 30분", 9000),
            )
        ),
        onBack = {}
    )
}
