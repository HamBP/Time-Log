package me.algosketch.timelog.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? =
        value?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(it), ZoneId.systemDefault()) }

    @TypeConverter
    fun dateTimeToTimestamp(dateTime: LocalDateTime?): Long? =
        dateTime?.atZone(ZoneId.systemDefault())?.toEpochSecond()
}