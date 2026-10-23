package me.algosketch.timelog.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.algosketch.timelog.R
import me.algosketch.timelog.ui.theme.TextTertiary
import java.time.LocalTime
import kotlin.math.abs

private val WheelItemHeight = 40.dp
private const val VisibleItemCount = 3

/** 시 · 분 · 초를 각각 스크롤 휠로 고르는 24시간제 시간 선택기. */
@Composable
fun TimeWheelPicker(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(R.string.unit_hour, R.string.unit_minute, R.string.unit_second).forEach { unit ->
                Text(
                    text = stringResource(unit),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(contentAlignment = Alignment.Center) {
            // 가운데 줄(선택 값) 강조
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.08f))
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                NumberWheel(
                    value = time.hour,
                    count = 24,
                    onValueChange = { onTimeChange(time.withHour(it)) },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                NumberWheel(
                    value = time.minute,
                    count = 60,
                    onValueChange = { onTimeChange(time.withMinute(it)) },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                NumberWheel(
                    value = time.second,
                    count = 60,
                    onValueChange = { onTimeChange(time.withSecond(it)) },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** 0 until [count] 값을 세로로 나열하고, 가운데에 멈춘 값을 선택 값으로 쓴다. */
@Composable
private fun NumberWheel(
    value: Int,
    count: Int,
    onValueChange: (Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = value)
    val flingBehavior = rememberSnapFlingBehavior(listState, SnapPosition.Center)
    val scope = rememberCoroutineScope()
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)

    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            layoutInfo.visibleItemsInfo
                .minByOrNull { abs(it.offset + it.size / 2 - center) }
                ?.index ?: currentValue
        }
    }

    // 스크롤이 멈추면 가운데 값을 선택 값으로 반영한다.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .filter { !it }
            .collect { if (centerIndex != currentValue) currentOnValueChange(centerIndex) }
    }

    // 바깥에서 값이 바뀌면 휠 위치를 맞춘다.
    LaunchedEffect(value) {
        if (!listState.isScrollInProgress && centerIndex != value) listState.scrollToItem(value)
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        // 첫/마지막 값도 가운데 줄에 올 수 있도록 위아래에 한 칸씩 여백을 둔다.
        contentPadding = PaddingValues(vertical = WheelItemHeight * ((VisibleItemCount - 1) / 2)),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.height(WheelItemHeight * VisibleItemCount)
    ) {
        items(count) { index ->
            val isSelected = index == centerIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { scope.launch { listState.animateScrollToItem(index) } },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString().padStart(2, '0'),
                    fontSize = if (isSelected) 22.sp else 16.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    color = if (isSelected) accentColor else TextTertiary,
                )
            }
        }
    }
}
