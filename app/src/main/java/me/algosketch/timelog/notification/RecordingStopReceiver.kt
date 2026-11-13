package me.algosketch.timelog.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import me.algosketch.timelog.data.SessionRecorder
import java.time.LocalDateTime

/**
 * 기록 중 알림의 정지 버튼을 처리한다. 화면이 살아 있는지와 무관하게 여기서 직접 세션을 저장하고,
 * 화면은 진행 중 세션이 사라진 것을 구독해 정지 상태로 바뀐다.
 */
class RecordingStopReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecordingStopReceiverEntryPoint {
        fun recordingNotifier(): RecordingNotifier
        fun sessionRecorder(): SessionRecorder
    }

    override fun onReceive(context: Context, intent: Intent) {
        val endedAt = LocalDateTime.now()
        val entryPoint = EntryPointAccessors
            .fromApplication(context, RecordingStopReceiverEntryPoint::class.java)
        entryPoint.recordingNotifier().cancel()

        val saveJob = entryPoint.sessionRecorder().stop(endedAt) ?: return
        // 백그라운드 프로세스가 저장 도중 종료되지 않도록 저장이 끝날 때까지 브로드캐스트를 붙잡아 둔다.
        val pendingResult = goAsync()
        saveJob.invokeOnCompletion { pendingResult.finish() }
    }
}
