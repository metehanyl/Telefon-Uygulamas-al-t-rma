package com.metehanyl.ezanvakti

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.metehanyl.ezanvakti.data.AppSettings
import com.metehanyl.ezanvakti.data.DiyanetApi
import com.metehanyl.ezanvakti.data.PrayerRepository
import com.metehanyl.ezanvakti.data.model.CityOption
import com.metehanyl.ezanvakti.data.model.DistrictOption
import com.metehanyl.ezanvakti.data.model.PrayerBundle
import com.metehanyl.ezanvakti.data.model.RefreshResult
import com.metehanyl.ezanvakti.data.model.SelectedLocation
import com.metehanyl.ezanvakti.notification.PrayerNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrayerUiState(
    val isLoading: Boolean = false,
    val bundle: PrayerBundle? = null,
    val isShowingExactToday: Boolean = true,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    val notificationsEnabled: Boolean = false,
    val manualLocation: SelectedLocation? = null
)

data class LocationPickerState(
    val isOpen: Boolean = false,
    val isLoadingCities: Boolean = false,
    val isLoadingDistricts: Boolean = false,
    val cities: List<CityOption> = emptyList(),
    val districts: List<DistrictOption> = emptyList(),
    val selectedCity: CityOption? = null,
    val selectedDistrict: DistrictOption? = null,
    val errorMessage: String? = null
)

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository(application)
    private val settings = AppSettings(application)
    private val scheduler = PrayerNotificationScheduler(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val _pickerState = MutableStateFlow(LocationPickerState())
    val pickerState: StateFlow<LocationPickerState> = _pickerState.asStateFlow()

    init {
        val cached = repository.getCachedBundle()
        _uiState.update {
            it.copy(
                bundle = cached,
                isShowingExactToday = cached?.todayOrClosest()?.second ?: true,
                notificationsEnabled = settings.notificationsEnabled,
                manualLocation = repository.getManualLocation()
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            if (!repository.hasLocationPermission() && repository.getManualLocation() == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Konum izni verilmedi. Konuma izin verin ya da şehrinizi elle seçin.",
                        isOffline = it.bundle != null
                    )
                }
                return@launch
            }

            when (val result = repository.refresh()) {
                is RefreshResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bundle = result.bundle,
                            isShowingExactToday = true,
                            isOffline = false,
                            errorMessage = null
                        )
                    }
                    if (settings.notificationsEnabled) {
                        scheduler.scheduleFromBundle(result.bundle)
                    }
                }
                is RefreshResult.Failure -> {
                    val current = _uiState.value
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOffline = current.bundle != null,
                            isShowingExactToday = current.bundle?.todayOrClosest()?.second ?: true,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settings.notificationsEnabled = enabled
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        if (enabled) {
            _uiState.value.bundle?.let { scheduler.scheduleFromBundle(it) }
        } else {
            scheduler.cancelAll()
        }
    }

    fun openLocationPicker() {
        _pickerState.update { LocationPickerState(isOpen = true, isLoadingCities = true) }
        viewModelScope.launch {
            try {
                val cities = DiyanetApi.getCities()
                _pickerState.update { it.copy(isLoadingCities = false, cities = cities) }
            } catch (e: Exception) {
                _pickerState.update {
                    it.copy(isLoadingCities = false, errorMessage = "Şehir listesi alınamadı, internet bağlantınızı kontrol edin.")
                }
            }
        }
    }

    fun closeLocationPicker() {
        _pickerState.update { LocationPickerState() }
    }

    fun selectCity(city: CityOption) {
        _pickerState.update {
            it.copy(selectedCity = city, selectedDistrict = null, districts = emptyList(), isLoadingDistricts = true)
        }
        viewModelScope.launch {
            try {
                val districts = DiyanetApi.getDistricts(city.id)
                _pickerState.update { it.copy(isLoadingDistricts = false, districts = districts) }
            } catch (e: Exception) {
                _pickerState.update {
                    it.copy(isLoadingDistricts = false, errorMessage = "İlçe listesi alınamadı, internet bağlantınızı kontrol edin.")
                }
            }
        }
    }

    fun selectDistrict(district: DistrictOption) {
        _pickerState.update { it.copy(selectedDistrict = district) }
    }

    fun confirmManualLocation() {
        val picker = _pickerState.value
        val city = picker.selectedCity ?: return
        val district = picker.selectedDistrict ?: return
        val location = SelectedLocation(city.id, city.name, district.id, district.name)
        repository.setManualLocation(location)
        _uiState.update { it.copy(manualLocation = location) }
        closeLocationPicker()
        refresh()
    }

    fun useAutomaticLocation() {
        repository.setManualLocation(null)
        _uiState.update { it.copy(manualLocation = null) }
        refresh()
    }
}
