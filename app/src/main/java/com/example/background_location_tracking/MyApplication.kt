package com.example.background_location_tracking

import android.app.Application
import com.example.background_location_tracking.di.AppComponent
import com.example.background_location_tracking.di.AppModule
import com.example.background_location_tracking.di.DaggerAppComponent


class MyApplication : Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }
}
