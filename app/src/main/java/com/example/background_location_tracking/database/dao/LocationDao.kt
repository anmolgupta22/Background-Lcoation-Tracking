package com.example.background_location_tracking.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.background_location_tracking.model.LocationDataList

@Dao
interface LocationDao {

    @Query("Select * from tbl_location")
    suspend fun fetchAllLocationTracking(): List<LocationDataList>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationDataList: LocationDataList)
}