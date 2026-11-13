package me.algosketch.timelog.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/** 아직 끝나지 않은(log_sessions에 저장되기 전) 기록 세션. */
data class ActiveSession(
    val typeId: Int,
    val startedAt: LocalDateTime,
)

/**
 * 진행 중인 세션을 메모리와 디스크(SharedPreferences)에 함께 보관한다.
 * 기록을 시작하는 순간 디스크에 동기 기록하므로, 백그라운드에서 프로세스가 종료되어도
 * 다음 실행 때 이어서 기록하거나 알림의 정지 버튼으로 저장할 수 있다.
 *
 * Room 스키마를 바꾸면 현재 파괴적 마이그레이션으로 기록이 지워지므로 DB 테이블 대신 SharedPreferences를 쓴다.
 */
@Singleton
class ActiveSessionHolder @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val lock = Any()

    private val _session = MutableStateFlow(load())
    val session: StateFlow<ActiveSession?> = _session.asStateFlow()

    fun set(session: ActiveSession?) = synchronized(lock) {
        persist(session)
        _session.value = session
    }

    /** 현재 값이 [expected]일 때만 [new]로 바꾼다. 그 사이 다른 세션이 시작됐으면 건드리지 않는다. */
    fun compareAndSet(expected: ActiveSession, new: ActiveSession?): Boolean = synchronized(lock) {
        if (_session.value != expected) return false
        persist(new)
        _session.value = new
        true
    }

    private fun load(): ActiveSession? {
        if (!prefs.contains(KEY_TYPE_ID) || !prefs.contains(KEY_STARTED_AT)) return null
        val startedAt = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(prefs.getLong(KEY_STARTED_AT, 0L)),
            ZoneId.systemDefault(),
        )
        return ActiveSession(typeId = prefs.getInt(KEY_TYPE_ID, 0), startedAt = startedAt)
    }

    /** 프로세스가 곧바로 종료되어도 남도록 commit으로 동기 기록한다. 값이 작아 비용은 무시할 수준이다. */
    private fun persist(session: ActiveSession?) {
        prefs.edit(commit = true) {
            if (session == null) {
                remove(KEY_TYPE_ID)
                remove(KEY_STARTED_AT)
            } else {
                putInt(KEY_TYPE_ID, session.typeId)
                // DB와 같은 기준(시스템 기본 시간대의 epoch 초)으로 저장한다.
                putLong(KEY_STARTED_AT, session.startedAt.atZone(ZoneId.systemDefault()).toEpochSecond())
            }
        }
    }

    private companion object {
        const val PREFS_NAME = "active_session"
        const val KEY_TYPE_ID = "type_id"
        const val KEY_STARTED_AT = "started_at"
    }
}
