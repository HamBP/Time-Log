package me.algosketch.timelog.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.algosketch.timelog.R
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** 앱이 백그라운드에 있는 동안 기록 중임을 알리는 진행 중(ongoing) 알림을 관리한다. */
@Singleton
class RecordingNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    private val _stopRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    /** 알림의 정지 버튼이 눌렸을 때 발행된다. */
    val stopRequests: SharedFlow<Unit> = _stopRequests.asSharedFlow()

    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.recording_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply { setShowBadge(false) }
        notificationManager.createNotificationChannel(channel)
    }

    /** 경과 시간은 시스템 크로노미터가 [startedAt] 기준으로 직접 갱신한다. */
    fun show(typeName: String, startedAt: LocalDateTime) {
        if (!hasPermission()) return

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            context.packageManager.getLaunchIntentForPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, RecordingStopReceiver::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(typeName)
            .setContentText(context.getString(R.string.recording_notification_text))
            .setWhen(startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.recording_notification_stop), stopIntent)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /** 세션을 들고 있는 화면이 없으면(프로세스가 새로 뜬 경우 등) 알림만 지운다. */
    fun requestStop() {
        if (_stopRequests.subscriptionCount.value == 0) {
            cancel()
        } else {
            _stopRequests.tryEmit(Unit)
        }
    }

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "recording"
        const val NOTIFICATION_ID = 1
    }
}
