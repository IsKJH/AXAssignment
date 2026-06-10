package com.ax.assignment.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromLocalDateTime(date: LocalDateTime): Long =
        date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long): LocalDateTime =
        Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
