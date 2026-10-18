package me.algosketch.timelog.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** 기록 중 알림의 정지 버튼을 처리한다. */
class RecordingStopReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecordingStopReceiverEntryPoint {
        fun recordingNotifier(): RecordingNotifier
    }

    override fun onReceive(context: Context, intent: Intent) {
        EntryPointAccessors
            .fromApplication(context, RecordingStopReceiverEntryPoint::class.java)
            .recordingNotifier()
            .requestStop()
    }
}
