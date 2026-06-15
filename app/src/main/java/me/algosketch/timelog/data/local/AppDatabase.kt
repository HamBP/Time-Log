package me.algosketch.timelog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.algosketch.timelog.data.local.dao.LogSessionDao
import me.algosketch.timelog.data.local.dao.LogTypeDao
import me.algosketch.timelog.data.local.entity.LogTypeEntity
import me.algosketch.timelog.data.local.entity.LogSessionEntity

@Database(entities = [LogTypeEntity::class, LogSessionEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun logTypeDao(): LogTypeDao
    abstract fun logSessionDao(): LogSessionDao
}