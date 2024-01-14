package com.example.background_location_tracking.di

import com.example.background_location_tracking.database.LocationRepository
import com.example.background_location_tracking.viewmodel.LocationViewModel
import dagger.Module
import dagger.Provides

@Module
class ViewModelModule {
    @Provides
    fun provideMyViewModel(locationRepository: LocationRepository): LocationViewModel {
        return LocationViewModel(locationRepository)
    }
}
