package com.example.background_location_tracking.viewmodel

import androidx.lifecycle.ViewModel
import com.example.background_location_tracking.model.LocationDataList
import com.example.background_location_tracking.database.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class LocationViewModel(private val roomDatabase: RoomDatabase) : ViewModel() {

    fun insertLocationList(list: LocationDataList) {
        roomDatabase.insertLocationList(list)
    }

    suspend fun fetchAllLocationTracking(): List<LocationDataList> {
        return withContext(Dispatchers.IO) {
            roomDatabase.fetchAllLocationTracking()
        }
    }


}