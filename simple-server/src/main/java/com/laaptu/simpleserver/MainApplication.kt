package com.laaptu.simpleserver

import android.app.Application
import timber.log.Timber

class MainApplication:Application(){
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}