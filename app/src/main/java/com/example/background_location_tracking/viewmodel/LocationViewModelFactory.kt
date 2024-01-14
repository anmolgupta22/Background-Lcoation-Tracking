package com.example.background_location_tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.background_location_tracking.database.LocationRepository
import com.example.background_location_tracking.di.AppComponent
import javax.inject.Inject

class LocationViewModelFactory @Inject constructor(private val instance: LocationRepository) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(LocationRepository::class.java).newInstance(instance)
    }
}