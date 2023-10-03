package com.bhaskarblur.arcorenavigation.di

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class myApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}