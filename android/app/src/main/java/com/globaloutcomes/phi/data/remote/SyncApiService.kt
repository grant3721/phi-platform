package com.globaloutcomes.phi.data.remote

import com.globaloutcomes.phi.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncApiService @Inject constructor(
    private val httpClientProvider: HttpClientProvider
) {
    private val client: HttpClient get() = httpClientProvider.client

    /**
     * Upload local data to server
     */
    suspend fun uploadData(request: SyncUploadRequest): Result<SyncUploadResponse> {
        return try {
            val response = client.post(ApiConfig.Endpoints.SYNC_UPLOAD) {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Download updates from server
     */
    suspend fun downloadUpdates(lastSyncTimestamp: Long): Result<SyncDownloadResponse> {
        return try {
            val response = client.post(ApiConfig.Endpoints.SYNC_DOWNLOAD) {
                setBody(SyncDownloadRequest(lastSyncTimestamp))
            }

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Download failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get config thresholds from server
     */
    suspend fun getConfigThresholds(): Result<Map<String, Float>> {
        return try {
            val response = client.get(ApiConfig.Endpoints.CONFIG_THRESHOLDS)

            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Get thresholds failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
