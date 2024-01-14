package com.example.background_location_tracking.database

import com.example.background_location_tracking.database.dao.LocationDao
import com.example.background_location_tracking.model.LocationDataList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class LocationRepository @Inject constructor(private val locationDao: LocationDao) {

    suspend fun insertLocationList(locationDataList: LocationDataList) {
        CoroutineScope(Dispatchers.IO).launch {
            locationDao.insert(locationDataList)
        }
    }

    suspend fun fetchAllLocationTracking(): List<LocationDataList> {
        return withContext(Dispatchers.IO) {
            locationDao.fetchAllLocationTracking()
        }
    }
}
