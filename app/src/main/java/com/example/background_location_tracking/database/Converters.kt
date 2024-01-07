package com.example.background_location_tracking.database

import androidx.room.TypeConverter
import com.example.background_location_tracking.model.LocationData
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun toCategoriesList(list: String?): List<LocationData?>? {
        val typeToken: Type = object : TypeToken<List<LocationData?>?>() {}.type
        return Gson().fromJson<List<LocationData?>>(list, typeToken)
    }

    @TypeConverter
    fun fromCategoriesList(list: List<LocationData?>?): String? {
        return Gson().toJson(list)
    }
}