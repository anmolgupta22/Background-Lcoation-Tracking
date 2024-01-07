package com.example.background_location_tracking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.background_location_tracking.database.RoomDatabase

class LocationViewModelFactory(private val instance: RoomDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(RoomDatabase::class.java).newInstance(instance)
    }
}