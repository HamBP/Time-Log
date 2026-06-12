package me.algosketch.timelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "log_sessions"
)
data class LogSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val typeId: Int,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
)
