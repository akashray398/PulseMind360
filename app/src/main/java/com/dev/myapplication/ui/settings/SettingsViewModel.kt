package com.dev.myapplication.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.ActivityLogDao
import com.dev.myapplication.data.repository.UserPreferences
import com.dev.myapplication.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val activityLogDao: ActivityLogDao
) : ViewModel() {

    val userPreferencesState: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesRepository.setLowBatteryThreshold(threshold)
        }
    }

    fun setSmartChargeGuardEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSmartChargeGuardEnabled(enabled)
        }
    }

    fun setOverheatAlarmEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOverheatAlarmEnabled(enabled)
        }
    }

    fun setBiometricVaultEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricVaultEnabled(enabled)
        }
    }

    fun setRedactExportMetadata(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setRedactExportMetadata(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun setSyncIntervalMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setSyncIntervalMinutes(minutes)
        }
    }

    fun purgeAllData() {
        viewModelScope.launch {
            activityLogDao.clearLogs()
        }
    }
}
