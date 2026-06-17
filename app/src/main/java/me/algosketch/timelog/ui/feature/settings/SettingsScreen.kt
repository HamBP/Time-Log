package me.algosketch.timelog.ui.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.algosketch.timelog.R
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.util.toMaterialIcon

/** 효율에 포함되지 않는 로그 타입은 고유 색상 대신 이 중립색으로 표시한다. */
private val NeutralLogTypeColor = TextTertiary

private val cardBorderColor = Color.White.copy(alpha = 0.06f)

@Composable
fun SettingsScreen(
    onAddClick: () -> Unit = {},
    onEditClick: (Int) -> Unit = {},
    viewModel: SettingsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        uiState = uiState,
        onAddClick = onAddClick,
        onEditClick = onEditClick,
    )
}

@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    onAddClick: () -> Unit,
    onEditClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        SettingsHeader()

        Text(
            text = stringResource(R.string.log_type_section_title),
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.72.sp,
            color = TextTertiary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.logTypes.forEach { logType ->
                LogTypeCard(
                    logType = logType,
                    onEditClick = { onEditClick(logType.id) },
                )
            }

            AddTypeButton(onClick = onAddClick)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SettingsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_label),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            letterSpacing = (-0.22).sp,
            lineHeight = 33.sp
        )
    }
}

@Composable
private fun LogTypeCard(
    logType: LogType,
    onEditClick: () -> Unit,
) {
    val displayColor = if (logType.includeEfficiency) logType.color else NeutralLogTypeColor
    val subtitle = if (logType.includeEfficiency) {
        stringResource(R.string.efficiency_included)
    } else {
        stringResource(R.string.efficiency_excluded)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(displayColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = logType.icon.toMaterialIcon(),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = displayColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = logType.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                lineHeight = 22.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(displayColor)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = displayColor,
                    lineHeight = 18.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, cardBorderColor, RoundedCornerShape(6.dp))
                .clickable(onClick = onEditClick)
                .padding(horizontal = 13.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.btn_edit),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = TextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AddTypeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.02f),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.15f),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(8.dp.toPx(), 4.dp.toPx())
                        )
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.btn_add_type),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            lineHeight = 19.5.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewSettingsScreen() {
    SettingsContent(
        uiState = SettingsUiState(
            logTypes = listOf(
                LogType("일하는 중", "play_arrow", Color(0xFF4ADE80), true),
                LogType("쉬는 중", "free_breakfast", Color(0xFFFB923C), false),
            )
        ),
        onAddClick = {},
        onEditClick = {},
    )
}
