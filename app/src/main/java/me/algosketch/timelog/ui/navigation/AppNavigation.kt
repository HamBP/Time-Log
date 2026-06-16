package me.algosketch.timelog.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.algosketch.timelog.R
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import me.algosketch.timelog.ui.feature.history.HistoryScreen
import me.algosketch.timelog.ui.feature.settings.SettingsScreen
import me.algosketch.timelog.ui.feature.stopwatch.StopWatchScreen
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen

@Serializable data object TimerDestination : NavKey
@Serializable data object HistoryDestination : NavKey
@Serializable data object SettingsDestination : NavKey

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(TimerDestination)
    val currentDest = backStack.lastOrNull()

    val activeTab = when (currentDest) {
        is HistoryDestination -> 1
        is SettingsDestination -> 2
        else -> 0
    }

    val switchTab: (NavKey) -> Unit = { dest ->
        backStack.clear()
        backStack.add(dest)
    }

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.weight(1f),
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            entryProvider = entryProvider {
                entry<TimerDestination> { StopWatchScreen() }
                entry<HistoryDestination> { HistoryScreen() }
                entry<SettingsDestination> { SettingsScreen() }
            }
        )

        BottomNavBar(
            activeTab = activeTab,
            onTimerClick = { switchTab(TimerDestination) },
            onHistoryClick = { switchTab(HistoryDestination) },
            onSettingsClick = { switchTab(SettingsDestination) }
        )
    }
}

@Composable
internal fun BottomNavBar(
    activeTab: Int,
    onTimerClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val topBorderColor = Color.White.copy(alpha = 0.08f)
    val tabs: List<Triple<ImageVector, String, () -> Unit>> = listOf(
        Triple(Icons.Default.Timer, stringResource(R.string.nav_timer), onTimerClick),
        Triple(Icons.Default.BarChart, stringResource(R.string.nav_history), onHistoryClick),
        Triple(Icons.Default.Settings, stringResource(R.string.nav_settings), onSettingsClick)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(64.dp)
            .background(Background)
            .drawBehind {
                drawLine(
                    color = topBorderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        tabs.forEachIndexed { index, (icon, label, onClick) ->
            val isActive = index == activeTab
            val color = if (isActive) WorkGreen else TextTertiary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .width(32.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                            .background(WorkGreen)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp),
                        tint = color
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = color,
                        lineHeight = 16.5.sp
                    )
                }
            }
        }
    }
}
