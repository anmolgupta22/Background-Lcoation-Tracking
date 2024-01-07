package com.example.background_location_tracking.database

import com.example.background_location_tracking.model.LocationDataList
import com.example.background_location_tracking.database.dao.LocationDao
import javax.inject.Inject


class RoomDatabase @Inject constructor(
    private val locationDao: LocationDao,
) {

    fun insertLocationList(locationDataList: LocationDataList) {
        locationDao.insert(locationDataList)
    }

    suspend fun fetchAllLocationTracking(): List<LocationDataList> {
        return locationDao.fetchAllLocationTracking()
    }
}
