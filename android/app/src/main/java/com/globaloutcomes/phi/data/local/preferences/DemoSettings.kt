package com.globaloutcomes.phi.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.demoDataStore: DataStore<Preferences> by preferencesDataStore(name = "demo_settings")

@Singleton
class DemoSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dedupBypassKey = booleanPreferencesKey("dedup_bypass")

    val isDedupBypassed: Flow<Boolean> = context.demoDataStore.data
        .map { preferences ->
            preferences[dedupBypassKey] ?: false
        }

    suspend fun setDedupBypass(enabled: Boolean) {
        context.demoDataStore.edit { preferences ->
            preferences[dedupBypassKey] = enabled
        }
    }
}
