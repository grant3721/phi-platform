package com.globaloutcomes.phi.data.local.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room Type Converters
 * Handles conversion between complex types and database-storable types
 */
class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // String List Converters (for JSON arrays stored as strings)
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
                emptyList()
            }
        }
    }

    // Generic JSON String Converters (already stored as String, just validation)
    @TypeConverter
    fun fromJsonString(value: String?): String? = value

    @TypeConverter
    fun toJsonString(value: String?): String? = value
}
