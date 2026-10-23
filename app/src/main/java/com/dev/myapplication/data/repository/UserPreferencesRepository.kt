package com.dev.myapplication.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferences(
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val lowBatteryThreshold: Int = 20,
    val highBatteryThreshold: Int = 80,
    val smartChargeGuardEnabled: Boolean = true,
    val overheatAlarmEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val syncIntervalMinutes: Int = 15,
    val retentionDays: Int = 30
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val HIGH_BATTERY_THRESHOLD = intPreferencesKey("high_battery_threshold")
        val SMART_CHARGE_GUARD = booleanPreferencesKey("smart_charge_guard")
        val OVERHEAT_ALARM = booleanPreferencesKey("overheat_alarm")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SYNC_INTERVAL_MINUTES = intPreferencesKey("sync_interval_minutes")
        val RETENTION_DAYS = intPreferencesKey("retention_days")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                themeMode = preferences[PreferenceKeys.THEME_MODE] ?: "SYSTEM",
                lowBatteryThreshold = preferences[PreferenceKeys.LOW_BATTERY_THRESHOLD] ?: 20,
                highBatteryThreshold = preferences[PreferenceKeys.HIGH_BATTERY_THRESHOLD] ?: 80,
                smartChargeGuardEnabled = preferences[PreferenceKeys.SMART_CHARGE_GUARD] ?: true,
                overheatAlarmEnabled = preferences[PreferenceKeys.OVERHEAT_ALARM] ?: true,
                notificationsEnabled = preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] ?: true,
                syncIntervalMinutes = preferences[PreferenceKeys.SYNC_INTERVAL_MINUTES] ?: 15,
                retentionDays = preferences[PreferenceKeys.RETENTION_DAYS] ?: 30
            )
        }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }

    suspend fun setLowBatteryThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.LOW_BATTERY_THRESHOLD] = threshold
        }
    }

    suspend fun setHighBatteryThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.HIGH_BATTERY_THRESHOLD] = threshold
        }
    }

    suspend fun setSmartChargeGuardEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SMART_CHARGE_GUARD] = enabled
        }
    }

    suspend fun setOverheatAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.OVERHEAT_ALARM] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setSyncIntervalMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.SYNC_INTERVAL_MINUTES] = minutes
        }
    }

    suspend fun setRetentionDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.RETENTION_DAYS] = days
        }
    }
}
