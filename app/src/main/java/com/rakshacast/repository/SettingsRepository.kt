package com.rakshacast.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rakshacast_settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val PUSH_NOTIFICATIONS = booleanPreferencesKey("push_notifications")
        val LOCATION_SERVICES = booleanPreferencesKey("location_services")
        val MIN_SEVERITY = stringPreferencesKey("min_severity")
        val LANGUAGE = stringPreferencesKey("language")
        val UNITS = stringPreferencesKey("units")
        val DEMO_SCENARIO = stringPreferencesKey("demo_scenario")
    }

    val pushNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[PUSH_NOTIFICATIONS] ?: true }
    val locationServicesEnabled: Flow<Boolean> = context.dataStore.data.map { it[LOCATION_SERVICES] ?: true }
    val minSeverity: Flow<String> = context.dataStore.data.map { it[MIN_SEVERITY] ?: "HIGH / SEVERE" }
    val language: Flow<String> = context.dataStore.data.map { it[LANGUAGE] ?: "English (India)" }
    val units: Flow<String> = context.dataStore.data.map { it[UNITS] ?: "Metric (°C, mm, km)" }
    val demoScenario: Flow<String> = context.dataStore.data.map { it[DEMO_SCENARIO] ?: "Live / Remote" }

    suspend fun setPushNotifications(enabled: Boolean) { context.dataStore.edit { it[PUSH_NOTIFICATIONS] = enabled } }
    suspend fun setLocationServices(enabled: Boolean) { context.dataStore.edit { it[LOCATION_SERVICES] = enabled } }
    suspend fun setMinSeverity(severity: String) { context.dataStore.edit { it[MIN_SEVERITY] = severity } }
    suspend fun setLanguage(lang: String) { context.dataStore.edit { it[LANGUAGE] = lang } }
    suspend fun setUnits(unit: String) { context.dataStore.edit { it[UNITS] = unit } }
    suspend fun setDemoScenario(scenario: String) { context.dataStore.edit { it[DEMO_SCENARIO] = scenario } }
}
