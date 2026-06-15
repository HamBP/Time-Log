package me.algosketch.timelog.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "log_types"
)
data class LogTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String, // "일하는 중"
    val colorHex: String, // "#123456"
    val icon: String, // "coffee"
    val sortOrder: Int,
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val includeEfficiency: Boolean = true,
)
