package com.globaloutcomes.phi.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Type converters for Room database
 * Handles JSON serialization for complex types
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // String List converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let {
            try {
                json.decodeFromString<List<String>>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

}
