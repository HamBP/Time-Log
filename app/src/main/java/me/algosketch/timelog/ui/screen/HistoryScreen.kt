package me.algosketch.timelog.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.RestOrange
import me.algosketch.timelog.ui.theme.Surface
import me.algosketch.timelog.ui.theme.TextPrimary
import me.algosketch.timelog.ui.theme.TextSecondary
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen

data class DailyRecord(
    val date: String,
    val sessionCount: Int,
    val efficiencyPercent: Int,
    val workTime: String,
    val restTime: String,
)

@Composable
fun HistoryScreen() {
    val records = listOf(
        DailyRecord("6월 6일 (토)", 8, 80, "4시간 0분", "1시간 0분"),
        DailyRecord("6월 5일 (금)", 12, 77, "5시간 0분", "1시간 30분"),
        DailyRecord("6월 4일 (목)", 6, 81, "3시간 30분", "50분"),
        DailyRecord("6월 3일 (수)", 10, 79, "4시간 30분", "1시간 10분"),
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
    ) {
        item {
            HistoryHeader()
        }
        item {
            MonthSummaryCard(
                avgEfficiency = "79%",
                totalWork = "24h",
                recordedDays = "6일",
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item {
            DailyRecordSection(
                records = records,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun HistoryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp)
    ) {
        Text(
            text = "히스토리",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.88.sp,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "이번 달 생산성",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            lineHeight = 30.sp
        )
    }
}

@Composable
private fun MonthSummaryCard(
    avgEfficiency: String,
    totalWork: String,
    recordedDays: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryStatItem(
            label = "평균 효율",
            value = avgEfficiency,
            valueColor = WorkGreen,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(52.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )
        SummaryStatItem(
            label = "총 작업",
            value = totalWork,
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(52.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )
        SummaryStatItem(
            label = "기록 일수",
            value = recordedDays,
            valueColor = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            color = valueColor,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp
        )
    }
}

@Composable
private fun DailyRecordSection(
    records: List<DailyRecord>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "일별 기록",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            letterSpacing = 0.66.sp,
            color = TextTertiary,
            lineHeight = 16.5.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            records.forEach { record ->
                DailyRecordCard(record = record)
            }
        }
    }
}

@Composable
private fun DailyRecordCard(record: DailyRecord) {
    val efficiencyColor = if (record.efficiencyPercent >= 80) WorkGreen else RestOrange
    val efficiencyRatio = record.efficiencyPercent / 100f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(17.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = record.date,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 21.sp
                )
                Text(
                    text = "${record.sessionCount}개 세션",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextTertiary,
                    lineHeight = 16.5.sp
                )
            }
            Text(
                text = "${record.efficiencyPercent}%",
                fontSize = 18.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                color = efficiencyColor,
                lineHeight = 27.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(efficiencyRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(efficiencyColor)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "작업",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = WorkGreen,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.workTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "휴식",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = RestOrange,
                    textAlign = TextAlign.End,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = record.restTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextSecondary,
                    textAlign = TextAlign.End,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F0F11)
@Composable
private fun PreviewHistoryScreen() {
    HistoryScreen()
}
