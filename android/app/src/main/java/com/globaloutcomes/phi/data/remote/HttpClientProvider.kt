package com.globaloutcomes.phi.data.remote

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.authDataStore by preferencesDataStore(name = "auth")

@Singleton
class HttpClientProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")

    val client: HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(HttpTimeout) {
            requestTimeoutMillis = ApiConfig.READ_TIMEOUT
            connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
            socketTimeoutMillis = ApiConfig.WRITE_TIMEOUT
        }

        install(DefaultRequest) {
            url(ApiConfig.BASE_URL)
            contentType(ContentType.Application.Json)

            // Add auth token to all requests
            header(HttpHeaders.Authorization, "Bearer ${getAuthToken()}")
        }
    }

    private fun getAuthToken(): String {
        return runBlocking {
            context.authDataStore.data.map { preferences ->
                preferences[AUTH_TOKEN_KEY] ?: ""
            }.first()
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.authDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                set(AUTH_TOKEN_KEY, token)
            }
        }
    }

    suspend fun clearAuthToken() {
        context.authDataStore.updateData { preferences ->
            preferences.toMutablePreferences().apply {
                remove(AUTH_TOKEN_KEY)
            }
        }
    }
}
