package com.example.background_location_tracking.di

import android.app.Application
import com.example.background_location_tracking.database.DBHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseModule {

    @Provides
    @Singleton
    fun providesDatabase(application: Application) = DBHelper.getInstance(application)

    @Provides
    @Singleton
    fun providesLocationDao(database: DBHelper) = database.locationDao()

}
