package com.example.background_location_tracking.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

data class LocationEvent(
    var latitude: Double? = null,
    var longitude: Double? = null,
)

@Entity(tableName = "tbl_location")
data class LocationDataList(

    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo
    var locationData: MutableList<LocationData> = arrayListOf(),
)

data class LocationData(
    var latLng: LatLng? = null,
    var timestamp: String? = null,
)
