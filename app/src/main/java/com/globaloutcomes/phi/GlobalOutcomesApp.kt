package com.globaloutcomes.phi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Global Outcomes PHI Platform Application Class
 * Initialized with Hilt for dependency injection
 */
@HiltAndroidApp
class GlobalOutcomesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application initialization
        // Database, WorkManager, and other initializations handled by Hilt
    }
}
