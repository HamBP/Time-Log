package me.algosketch.timelog

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import me.algosketch.timelog.ui.feature.stopwatch.StopWatchViewModel
import me.algosketch.timelog.ui.navigation.AppNavigation
import me.algosketch.timelog.ui.theme.TimeLogTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // AppNavigation의 타이머 화면과 같은 Activity 스코프 인스턴스
    private val stopWatchViewModel: StopWatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            TimeLogTheme {
                AppNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        stopWatchViewModel.onAppForeground()
    }

    override fun onStop() {
        super.onStop()
        // 화면 회전 등 구성 변경은 백그라운드 진입이 아니다.
        if (!isChangingConfigurations) stopWatchViewModel.onAppBackground()
    }
}
