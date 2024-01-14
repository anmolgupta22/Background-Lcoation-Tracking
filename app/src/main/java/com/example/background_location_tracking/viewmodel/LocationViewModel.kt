package com.example.background_location_tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.background_location_tracking.database.LocationRepository
import com.example.background_location_tracking.model.LocationDataList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class LocationViewModel @Inject constructor(private val repository: LocationRepository) :
    ViewModel() {
    private val _locationData = MutableStateFlow<List<LocationDataList>>(emptyList())
    val locationData: StateFlow<List<LocationDataList>> get() = _locationData

    fun insertLocationList(list: LocationDataList) {
        viewModelScope.launch {
            repository.insertLocationList(list)
        }
    }

    fun fetchAllLocationTracking() = viewModelScope.launch {
        _locationData.value = repository.fetchAllLocationTracking()
    }
}