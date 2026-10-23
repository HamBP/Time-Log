package me.algosketch.timelog.ui.feature.sessionedit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.algosketch.timelog.R
import me.algosketch.timelog.ui.component.TimeLogAppBar
import me.algosketch.timelog.ui.component.TimeWheelPicker
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.ErrorRed
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen
import me.algosketch.timelog.ui.theme.toComposeColor
import me.algosketch.timelog.ui.util.toMaterialIcon
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val cardBorderColor = Color.White.copy(alpha = 0.06f)

@Composable
fun SessionEditScreen(
    sessionId: Int,
    onBack: () -> Unit = {},
    viewModel: SessionEditViewModel = hiltViewModel(),
) {
    LaunchedEffect(sessionId) { viewModel.load(sessionId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SessionEditContent(
        uiState = uiState,
        onBack = onBack,
        onStartTimeChange = viewModel::onStartTimeChange,
        onEndTimeChange = viewModel::onEndTimeChange,
        onConfirm = { viewModel.onConfirm(onBack) },
    )
}

@Composable
private fun SessionEditContent(
    uiState: SessionEditUiState,
    onBack: () -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onConfirm: () -> Unit,
) {
    // 효율 미포함 타입은 고유 색상 대신 중립색으로 표시한다.
    val accentColor = if (uiState.includeEfficiency && uiState.colorHex.isNotEmpty()) {
        uiState.colorHex.toComposeColor()
    } else {
        TextTertiary
    }
    val timePattern = stringResource(R.string.time_seconds_format_pattern)
    val timeFormatter = remember(timePattern) { DateTimeFormatter.ofPattern(timePattern, Locale.getDefault()) }
    val datePattern = stringResource(R.string.date_format_pattern)
    val dateFormatter = remember(datePattern) { DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        TimeLogAppBar(title = stringResource(R.string.session_edit_title), onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoaded) {
                SessionHeader(
                    uiState = uiState,
                    dateLabel = uiState.date.format(dateFormatter),
                    accentColor = accentColor,
                )
                TimeSection(
                    label = stringResource(R.string.field_start_time),
                    time = uiState.startTime,
                    onTimeChange = onStartTimeChange,
                    rangeLabel = stringResource(
                        R.string.time_range_limit,
                        uiState.minStartTime.format(timeFormatter),
                        uiState.endTime.format(timeFormatter),
                    ),
                    isValid = uiState.isStartValid,
                    accentColor = accentColor,
                )
                TimeSection(
                    label = stringResource(R.string.field_end_time),
                    time = uiState.endTime,
                    onTimeChange = onEndTimeChange,
                    rangeLabel = stringResource(
                        R.string.time_range_limit,
                        uiState.startTime.format(timeFormatter),
                        uiState.maxEndTime.format(timeFormatter),
                    ),
                    isValid = uiState.isEndValid,
                    accentColor = accentColor,
                )
            }
        }

        ConfirmButton(
            enabled = uiState.canSave,
            onClick = onConfirm,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )
    }
}

@Composable
private fun SessionHeader(
    uiState: SessionEditUiState,
    dateLabel: String,
    accentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = uiState.icon.toMaterialIcon(),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = uiState.typeName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = dateLabel,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextTertiary,
                lineHeight = 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = formatElapsed(uiState.durationSecs),
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = accentColor,
            lineHeight = 27.sp
        )
    }
}

@Composable
private fun TimeSection(
    label: String,
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    rangeLabel: String,
    isValid: Boolean,
    accentColor: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, if (isValid) cardBorderColor else ErrorRed.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.72.sp,
            color = TextTertiary,
            lineHeight = 18.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        TimeWheelPicker(
            time = time,
            onTimeChange = onTimeChange,
            accentColor = if (isValid) accentColor else ErrorRed,
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 선택할 수 있는 최소 ~ 최대 시각
        Text(
            text = rangeLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = if (isValid) TextSecondary else ErrorRed,
            lineHeight = 18.sp,
        )
        if (!isValid) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.error_time_out_of_range),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = ErrorRed,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
private fun ConfirmButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(12.dp))
            .background(WorkGreen.copy(alpha = 0.08f))
            .border(1.dp, WorkGreen.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.btn_confirm),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = WorkGreen,
            lineHeight = 22.5.sp,
        )
    }
}

private fun formatElapsed(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) {
        "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewSessionEdit() {
    SessionEditContent(
        uiState = SessionEditUiState(
            isLoaded = true,
            typeName = "일하는 중",
            colorHex = "#4ADE80",
            icon = "play_arrow",
            includeEfficiency = true,
            date = LocalDate.of(2026, 10, 23),
            startTime = LocalTime.of(9, 0, 12),
            endTime = LocalTime.of(10, 30, 0),
            minStartTime = LocalTime.of(8, 45, 3),
            maxEndTime = LocalTime.of(10, 30, 0),
        ),
        onBack = {}, onStartTimeChange = {}, onEndTimeChange = {}, onConfirm = {},
    )
}
