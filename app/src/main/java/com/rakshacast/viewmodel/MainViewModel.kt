package com.rakshacast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakshacast.model.*
import com.rakshacast.repository.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val USE_MOCK = false // Toggle this for local development

    private val repository: AppRepository = if (USE_MOCK) MockRepository() else RemoteRepository()

    private val _currentLocation = MutableStateFlow(Location("Hyderabad, Telangana", 17.3850, 78.4867))
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    private val _weatherRisk = MutableStateFlow<WeatherRisk?>(null)
    val weatherRisk: StateFlow<WeatherRisk?> = _weatherRisk.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _shelters = MutableStateFlow<List<Shelter>>(emptyList())
    val shelters: StateFlow<List<Shelter>> = _shelters.asStateFlow()

    private val _riskZones = MutableStateFlow<List<RiskZone>>(emptyList())
    val riskZones: StateFlow<List<RiskZone>> = _riskZones.asStateFlow()

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private val _safetyGuidance = MutableStateFlow<List<SafetyGuidance>>(emptyList())
    val safetyGuidance: StateFlow<List<SafetyGuidance>> = _safetyGuidance.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            repository.getRiskData().collect { result ->
                result.onSuccess { _weatherRisk.value = it }
                      .onFailure { _errorMessage.value = it.message ?: "Failed to load risk data" }
            }
        }
        viewModelScope.launch {
            repository.getAlerts().collect { result ->
                result.onSuccess { _alerts.value = it }
            }
        }
        viewModelScope.launch {
            repository.getShelters().collect { result ->
                result.onSuccess { _shelters.value = it }
            }
        }
        viewModelScope.launch {
            repository.getRiskZones().collect { result ->
                result.onSuccess { _riskZones.value = it }
            }
        }
        viewModelScope.launch {
            repository.getEmergencyContacts().collect { result ->
                result.onSuccess { _emergencyContacts.value = it }
            }
        }
        viewModelScope.launch {
            repository.getSafetyGuidance().collect { result ->
                result.onSuccess { 
                    _safetyGuidance.value = it 
                    _isLoading.value = false
                }
                .onFailure {
                    _isLoading.value = false
                }
            }
        }
    }

    fun markAlertAsRead(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(isRead = true) else it
        }
    }
}
