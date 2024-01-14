package com.example.background_location_tracking.di

import com.example.background_location_tracking.fragment.LocationTrackingFragment
import com.example.background_location_tracking.fragment.PreviousTrackingFragment
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AppModule::class, ViewModelModule::class, DatabaseModule::class])
@Singleton
interface AppComponent {
    fun inject(activity: LocationTrackingFragment)
    fun inject(activity: PreviousTrackingFragment)
}