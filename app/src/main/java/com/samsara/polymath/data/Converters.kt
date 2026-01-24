package com.samsara.polymath.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromRankStatus(value: RankStatus): String {
        return value.name
    }

    @TypeConverter
    fun toRankStatus(value: String): RankStatus {
        return try {
            RankStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            RankStatus.STABLE
        }
    }
}


