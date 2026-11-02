package com.dev.myapplication.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.repository.UserPreferences
import com.dev.myapplication.data.repository.UserPreferencesRepository
import com.dev.myapplication.domain.privacy.PrivacyAuditReport
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import com.dev.myapplication.util.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyAuditorViewModel @Inject constructor(
    private val privacyRiskEngine: PrivacyRiskEngine,
    private val userPreferencesRepository: UserPreferencesRepository,
    val biometricHelper: BiometricHelper
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = userPreferencesRepository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserPreferences()
    )

    private val _report = MutableStateFlow<PrivacyAuditReport?>(null)
    val report: StateFlow<PrivacyAuditReport?> = _report.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.collect { prefs ->
                _report.value = privacyRiskEngine.generateFullReport(
                    isBiometricVaultEnabled = prefs.biometricVaultEnabled,
                    isPdfRedactionEnabled = prefs.redactExportMetadata
                )
            }
        }
    }

    fun refreshReport() {
        val prefs = userPreferences.value
        _report.value = privacyRiskEngine.generateFullReport(
            isBiometricVaultEnabled = prefs.biometricVaultEnabled,
            isPdfRedactionEnabled = prefs.redactExportMetadata
        )
    }

    fun setAllowBackendSync(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setAllowBackendSync(enabled)
            refreshReport()
        }
    }

    fun setRedactExportMetadata(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRedactExportMetadata(enabled)
            refreshReport()
        }
    }

    fun setCollectBatteryTelemetry(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCollectBatteryTelemetry(enabled)
            refreshReport()
        }
    }

    fun setCollectNetworkTelemetry(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCollectNetworkTelemetry(enabled)
            refreshReport()
        }
    }

    fun setCollectFramePerformance(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCollectFramePerformance(enabled)
            refreshReport()
        }
    }
}
