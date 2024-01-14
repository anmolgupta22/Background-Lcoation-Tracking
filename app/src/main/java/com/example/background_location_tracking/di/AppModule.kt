package com.example.background_location_tracking.di

import android.app.Application
import com.example.background_location_tracking.MyApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val application: MyApplication) {

    @Provides
    @Singleton
    fun provideApplication(): Application = application
}
