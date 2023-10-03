package com.bhaskarblur.arcorenavigation.di

import android.content.Context
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.core.content.ContextCompat.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class hiltModule {


    @Provides
    @Singleton
    fun getLocationManager(@ApplicationContext context : Context)
            : LocationManager = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager);

    @Provides
    @Singleton
    fun getSensorManager(@ApplicationContext context : Context)
    : SensorManager = (context.getSystemService(Context.SENSOR_SERVICE) as SensorManager);
}