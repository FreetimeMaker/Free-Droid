package org.fdroid.database

import androidx.room.TypeConverter
import org.fdroid.index.v2.FileV2
import org.fdroid.index.v2.LocalizedFileV2
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

public object Converters {

    @TypeConverter
     public fun listStringToString(list: List<String>?): String? {
        return if (list.isNullOrEmpty()) null else Json.encodeToString(list)
    }

    @TypeConverter
     public fun fromStringToListString(string: String?): List<String>? {
        return if (string.isNullOrEmpty()) emptyList() else Json.decodeFromString(string)
    }

    @TypeConverter
     public fun localizedFileV2toString(file: LocalizedFileV2?): String? {
        return if (file == null) null else Json.encodeToString(file)
    }

    @TypeConverter
     public fun fromStringToLocalizedFileV2(string: String?): LocalizedFileV2? {
        return if (string.isNullOrEmpty()) null else Json.decodeFromString(string)
    }

    @TypeConverter
     public fun fileV2ToString(file: FileV2?): String? {
        return if (file == null) null else Json.encodeToString(file)
    }

    @TypeConverter
     public fun fromStringToFileV2(string: String?): FileV2? {
        return if (string.isNullOrEmpty()) null else Json.decodeFromString(string)
    }

    @TypeConverter
     public fun appMetadataToString(metadata: AppMetadata?): String? {
        return if (metadata == null) null else Json.encodeToString(metadata)
    }

    @TypeConverter
     public fun fromStringToAppMetadata(string: String?): AppMetadata? {
        return if (string.isNullOrEmpty()) null else Json.decodeFromString(string)
    }

    @TypeConverter
     public fun mapStringToString(map: Map<String, String>?): String? {
        return if (map.isNullOrEmpty()) null else Json.encodeToString(map)
    }

    @TypeConverter
     public fun fromStringToMapStringString(string: String?): Map<String, String>? {
        return if (string.isNullOrEmpty()) emptyMap() else Json.decodeFromString(string)
    }

    @TypeConverter
     public fun mapStringToLocalizedFileV2List(map: Map<String, List<LocalizedFileV2>>?): String? {
        return if (map.isNullOrEmpty()) null else Json.encodeToString(map)
    }

    @TypeConverter
     public fun fromStringToMapStringLocalizedFileV2List(string: String?): Map<String, List<LocalizedFileV2>>? {
        return if (string.isNullOrEmpty()) emptyMap() else Json.decodeFromString(string)
    }
}
