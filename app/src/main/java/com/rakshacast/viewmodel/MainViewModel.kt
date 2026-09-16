package com.rakshacast.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rakshacast.model.*
import com.rakshacast.repository.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.Duration

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val alertManager = com.rakshacast.service.AlertManager()
    private var USE_MOCK = false

    private val _repository = MutableStateFlow<AppRepository>(RemoteRepository())
    private val repository get() = _repository.value

    val settingsRepository = SettingsRepository(application)

    private val _currentLocation = MutableStateFlow(Location("Hyderabad, Telangana", 17.3850, 78.4867))
    val currentLocation: StateFlow<Location> = _currentLocation.asStateFlow()

    private val _weatherRisk = MutableStateFlow<WeatherRisk?>(null)
    val weatherRisk: StateFlow<WeatherRisk?> = _weatherRisk.asStateFlow()
    
    private val _previousRiskLevel = MutableStateFlow<RiskLevel?>(null)
    val previousRiskLevel: StateFlow<RiskLevel?> = _previousRiskLevel.asStateFlow()

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts = kotlinx.coroutines.flow.combine(alertManager.generatedAlerts, _alerts) { generated, remote -> 
        (generated + remote).distinctBy { it.id } 
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _shelters = MutableStateFlow<List<Shelter>>(emptyList())
    val shelters: StateFlow<List<Shelter>> = _shelters.asStateFlow()

    private val _riskZones = MutableStateFlow<List<RiskZone>>(emptyList())
    val riskZones: StateFlow<List<RiskZone>> = _riskZones.asStateFlow()

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: StateFlow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    private val _safetyGuidance = MutableStateFlow<List<SafetyGuidance>>(emptyList())
    val safetyGuidance: StateFlow<List<SafetyGuidance>> = _safetyGuidance.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _lastUpdatedTimestamp = MutableStateFlow<Instant?>(null)
    val lastUpdatedTimestamp: StateFlow<Instant?> = _lastUpdatedTimestamp.asStateFlow()
    
    private val _networkStatus = MutableStateFlow("ONLINE")
    val networkStatus: StateFlow<String> = _networkStatus.asStateFlow()

    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            settingsRepository.demoScenario.collect { scenario ->
                if (scenario == "Live / Remote") {
                    USE_MOCK = false
                    _repository.value = RemoteRepository()
                } else {
                    USE_MOCK = true
                    _repository.value = MockRepository(scenario)
                }
                loadData()
                startAutoRefresh()
            }
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L) // 5 minutes
                if (!USE_MOCK) {
                    refreshData()
                }
            }
        }
    }

    fun loadData() {
        _isLoading.value = true
        _errorMessage.value = null
        fetchData(isRefresh = false)
    }
    
    fun refreshData() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        fetchData(isRefresh = true)
    }

    private fun fetchData(isRefresh: Boolean) {
        viewModelScope.launch {
            repository.getRiskData().collect { result ->
                result.onSuccess { newRisk -> 
                    val oldRisk = _weatherRisk.value
                    if (oldRisk != null && oldRisk.overallRisk != newRisk.overallRisk) {
                        _previousRiskLevel.value = oldRisk.overallRisk
                    }
                    _weatherRisk.value = newRisk 
                    _lastUpdatedTimestamp.value = Instant.now()
                    _networkStatus.value = "ONLINE"
                }.onFailure { 
                    if (!isRefresh) {
                        _errorMessage.value = "Weather intelligence temporarily unavailable." 
                    }
                    _networkStatus.value = "BACKEND UNAVAILABLE"
                }
                _isLoading.value = false
                _isRefreshing.value = false
            }
        }
        viewModelScope.launch {
            repository.getAlerts().collect { result -> result.onSuccess { _alerts.value = it } }
        }
        viewModelScope.launch {
            repository.getShelters().collect { result -> result.onSuccess { _shelters.value = it } }
        }
        viewModelScope.launch {
            repository.getRiskZones().collect { result -> result.onSuccess { _riskZones.value = it } }
        }
        viewModelScope.launch {
            repository.getEmergencyContacts().collect { result -> result.onSuccess { _emergencyContacts.value = it } }
        }
        viewModelScope.launch {
            repository.getSafetyGuidance().collect { result -> result.onSuccess { _safetyGuidance.value = it } }
        }
    }
    
    fun getDataFreshness(): String {
        val lastUpdated = _lastUpdatedTimestamp.value ?: return "Unknown"
        val minutesAgo = Duration.between(lastUpdated, Instant.now()).toMinutes()
        return when {
            minutesAgo < 5 -> "Fresh"
            minutesAgo < 15 -> "Recent"
            else -> "Stale"
        }
    }
    
    fun getLastUpdatedText(): String {
        val lastUpdated = _lastUpdatedTimestamp.value ?: return "Updating..."
        val secondsAgo = Duration.between(lastUpdated, Instant.now()).seconds
        val minutesAgo = secondsAgo / 60
        return when {
            secondsAgo < 60 -> "Updated $secondsAgo sec ago"
            minutesAgo == 1L -> "Updated 1 min ago"
            else -> "Updated $minutesAgo min ago"
        }
    }
    
    fun getRiskTrendText(): String {
        val current = _weatherRisk.value?.overallRisk ?: return "Risk stable"
        val prev = _previousRiskLevel.value ?: return "Trend unavailable"
        
        return when {
            current.ordinal > prev.ordinal + 1 -> "Risk increasing rapidly ↑"
            current.ordinal > prev.ordinal -> "Risk increasing ↑"
            current.ordinal < prev.ordinal -> "Risk decreasing ↓"
            else -> "Risk stable"
        }
    }
}


