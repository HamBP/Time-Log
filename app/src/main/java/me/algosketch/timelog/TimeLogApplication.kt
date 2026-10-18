package me.algosketch.timelog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.algosketch.timelog.notification.RecordingNotifier
import javax.inject.Inject

@HiltAndroidApp
class TimeLogApplication : Application() {

    @Inject lateinit var recordingNotifier: RecordingNotifier

    override fun onCreate() {
        super.onCreate()
        recordingNotifier.createChannel()
        // 진행 중 세션은 메모리에만 있으므로, 이전 프로세스가 남긴 알림은 더 이상 유효하지 않다.
        recordingNotifier.cancel()
    }
}
