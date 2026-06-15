package me.algosketch.timelog.data.local

import androidx.room.TypeConverter
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, java.time.ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateTimeToTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochSecond(java.time.ZoneOffset.UTC)
    }
}