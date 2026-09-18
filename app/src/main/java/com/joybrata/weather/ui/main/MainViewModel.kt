package com.joybrata.weather.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joybrata.weather.data.repository.WeatherRepository
import com.joybrata.weather.ui.base.BaseViewModel
import com.joybrata.weather.util.AppPreferences
import com.joybrata.weather.util.Logger
import com.joybrata.weather.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Drives the main weather screen. Fetches forecast data via [WeatherRepository]
 * using coroutines and reports results back through [MainNavigator].
 **/
class MainViewModel(
    private val repository: WeatherRepository,
    private val appPreferences: AppPreferences
) : BaseViewModel<MainNavigator>() {

    private companion object {
        const val TAG = "MainViewModel"
    }

    private var lastLocation: Pair<Double, Double>? = null
    private var lastCity: String? = null

    fun fetchWeatherByLocation(lat: Double, lon: Double) {
        lastLocation = lat to lon
        lastCity = null
        viewModelScope.launch {
            val units = appPreferences.getTemperatureUnit()
            navigator?.showProgress()
            val result = withContext(Dispatchers.IO) {
                repository.getWeatherByLocation(lat, lon, units)
            }
            handleResult(result, units)
        }
    }

    fun fetchWeatherByCity(cityName: String) {
        viewModelScope.launch {
            val units = appPreferences.getTemperatureUnit()
            navigator?.showProgress()
            val result = withContext(Dispatchers.IO) {
                repository.getWeatherByCity(cityName, units)
            }
            if (result is Resource.Success) {
                lastCity = cityName
                lastLocation = null
            }
            handleResult(result, units)
        }
    }

    /** Re-fetches using the last known query after a unit change. */
    fun refreshForCurrentQuery() {
        val city = lastCity
        val location = lastLocation
        when {
            city != null -> fetchWeatherByCity(city)
            location != null -> fetchWeatherByLocation(location.first, location.second)
        }
    }

    private fun handleResult(
        result: Resource<com.joybrata.weather.data.model.WeatherResponse>,
        units: String
    ) {
        navigator?.hideProgress()
        when (result) {
            is Resource.Success -> navigator?.onWeatherLoaded(result.data, units)
            is Resource.Error -> {
                Logger.e(TAG, "Weather fetch failed: ${result.message} (code=${result.code})")
                if (result.code == 404) {
                    navigator?.onCityNotFound()
                } else {
                    navigator?.showError(result.message)
                }
            }
        }
    }
}

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Lightweight factory injecting the repository and preferences into [MainViewModel].
 **/
class MainViewModelFactory(
    private val repository: WeatherRepository,
    private val appPreferences: AppPreferences
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(MainViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return MainViewModel(repository, appPreferences) as T
    }
}
