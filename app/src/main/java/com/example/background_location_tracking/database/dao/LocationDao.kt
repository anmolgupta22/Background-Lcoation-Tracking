package com.example.background_location_tracking.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.background_location_tracking.model.LocationDataList


@Dao
interface LocationDao : BaseDao<LocationDataList> {

    @Query("Select * from tbl_location")
    suspend fun fetchAllLocationTracking(): List<LocationDataList>
}