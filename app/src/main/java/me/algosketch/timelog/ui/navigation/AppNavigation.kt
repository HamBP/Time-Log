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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import me.algosketch.timelog.ui.feature.history.HistoryScreen
import me.algosketch.timelog.ui.feature.historydetail.HistoryDetailScreen
import me.algosketch.timelog.ui.feature.logtypeform.LogTypeFormScreen
import me.algosketch.timelog.ui.feature.settings.SettingsScreen
import me.algosketch.timelog.ui.feature.stopwatch.StopWatchScreen
import me.algosketch.timelog.ui.theme.Background
import me.algosketch.timelog.ui.theme.TextTertiary
import me.algosketch.timelog.ui.theme.WorkGreen

@Serializable data object TimerDestination : NavKey
@Serializable data object HistoryDestination : NavKey
@Serializable data object SettingsDestination : NavKey
@Serializable data class HistoryDetailDestination(val date: String) : NavKey
@Serializable data object LogTypeAddDestination : NavKey
@Serializable data class LogTypeEditDestination(val typeId: Int) : NavKey

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(TimerDestination)
    val currentDest = backStack.lastOrNull()

    val activeTab = when (currentDest) {
        is HistoryDestination, is HistoryDetailDestination -> 1
        is SettingsDestination, is LogTypeAddDestination, is LogTypeEditDestination -> 2
        else -> 0
    }

    val switchTab: (NavKey) -> Unit = { dest ->
        backStack.clear()
        backStack.add(dest)
    }

    // NavDisplay 안에서는 LocalViewModelStoreOwner가 NavEntry 스코프로 덮어씌워지므로,
    // Activity 스코프 소유자를 미리 잡아둔다. StopWatch 전용으로 쓴다.
    val activityViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.weight(1f),
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            // 각 NavEntry에 전용 ViewModelStore를 부여한다. 없으면 모든 화면이 Activity 스코프
            // ViewModel 하나를 공유해 화면 간 상태가 섞인다.
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                // 진행 중인 세션은 정지 시점에만 DB에 저장되므로, 탭 전환으로 엔트리가 pop되어도
                // 타이머가 살아있도록 Activity 스코프 ViewModel을 유지한다.
                entry<TimerDestination> {
                    StopWatchScreen(viewModel = hiltViewModel(activityViewModelStoreOwner))
                }
                entry<HistoryDestination> {
                    HistoryScreen(
                        onRecordClick = { date -> backStack.add(HistoryDetailDestination(date)) }
                    )
                }
                entry<SettingsDestination> {
                    SettingsScreen(
                        onAddClick = { backStack.add(LogTypeAddDestination) },
                        onEditClick = { id -> backStack.add(LogTypeEditDestination(id)) }
                    )
                }
                entry<HistoryDetailDestination> { key ->
                    HistoryDetailScreen(
                        date = key.date,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<LogTypeAddDestination> {
                    LogTypeFormScreen(
                        typeId = null,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
                entry<LogTypeEditDestination> { key ->
                    LogTypeFormScreen(
                        typeId = key.typeId,
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
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
