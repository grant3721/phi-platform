package com.globaloutcomes.phi.data.remote

/**
 * API configuration constants
 */
object ApiConfig {
    // Base URL configured in build.gradle
    const val BASE_URL = "https://phi-platform-api.azurewebsites.net/api/"

    // API Endpoints
    object Endpoints {
        // Auth
        const val AUTH_OTP_REQUEST = "auth/otp/request"
        const val AUTH_OTP_VERIFY = "auth/otp/verify"

        // Sync
        const val SYNC_UPLOAD = "sync/upload"
        const val SYNC_DOWNLOAD = "sync/download"

        // Config
        const val CONFIG_THRESHOLDS = "config/thresholds"
    }

    // Timeouts (milliseconds)
    const val CONNECT_TIMEOUT = 30_000L
    const val READ_TIMEOUT = 30_000L
    const val WRITE_TIMEOUT = 30_000L
}
