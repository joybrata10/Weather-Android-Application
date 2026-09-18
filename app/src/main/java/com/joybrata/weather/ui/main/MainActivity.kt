package com.joybrata.weather.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.joybrata.weather.R
import com.joybrata.weather.data.model.WeatherResponse
import com.joybrata.weather.data.repository.WeatherRepository
import com.joybrata.weather.databinding.ActivityMainBinding
import com.joybrata.weather.ui.about.AboutActivity
import com.joybrata.weather.ui.main.fragment.CityInputBottomSheetFragment
import com.joybrata.weather.ui.main.fragment.TemperatureUnitDialogFragment
import com.joybrata.weather.util.AppPreferences
import com.joybrata.weather.util.Constants
import com.joybrata.weather.util.Logger
import com.joybrata.weather.util.showToast
import com.joybrata.weather.util.toDegreeSuffix
import com.joybrata.weather.util.toWeatherGifName
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Entry screen. Owns location acquisition and view rendering while delegating
 * all data work to [MainViewModel] (MVVM).
 **/
class MainActivity : AppCompatActivity(), MainNavigator {

    private companion object {
        const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(WeatherRepository(), AppPreferences(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.navigator = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupListeners()
        updateDateAndWeekday()

        if (hasLocationPermission()) {
            requestSingleLocationUpdate()
        } else {
            requestLocationPermission()
        }
    }

    private fun setupListeners() {
        binding.ibMenu.setOnClickListener { binding.drawerLayout.open() }
        binding.tvCity.setOnClickListener { showCityInput() }
        binding.fabSearch.setOnClickListener { showCityInput() }

        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    showTemperatureUnitDialog()
                    true
                }

                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }

    private fun showCityInput() {
        CityInputBottomSheetFragment { cityName ->
            viewModel.fetchWeatherByCity(cityName)
        }.show(supportFragmentManager, Constants.TAG_CITY_INPUT)
    }

    private fun showTemperatureUnitDialog() {
        TemperatureUnitDialogFragment {
            viewModel.refreshForCurrentQuery()
        }.show(supportFragmentManager, Constants.TAG_TEMPERATURE_UNIT)
    }

    private fun updateDateAndWeekday() {
        val calendar = Calendar.getInstance()
        binding.date.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        binding.weekday.text = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
    }

    // region Location

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Constants.LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun requestSingleLocationUpdate() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_UPDATE_INTERVAL_MS
        ).setMinUpdateIntervalMillis(Constants.LOCATION_MIN_UPDATE_INTERVAL_MS).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.firstOrNull()?.let { onLocationReceived(it) }
                locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun onLocationReceived(location: Location) {
        Logger.d(TAG, "Location: lat=${location.latitude}, lon=${location.longitude}")
        viewModel.fetchWeatherByLocation(location.latitude, location.longitude)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            requestSingleLocationUpdate()
        }
    }

    // endregion

    // region MainNavigator

    override fun showProgress() {
        binding.fabSearch.isEnabled = false
    }

    override fun hideProgress() {
        binding.fabSearch.isEnabled = true
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun onCityNotFound() {
        showToast(getString(R.string.error_invalid_location))
    }

    override fun onWeatherLoaded(response: WeatherResponse, units: String) {
        val current = response.forecasts.firstOrNull() ?: return
        binding.tvCity.text = response.city.name
        binding.tvTemp.text = current.main.temp.toInt().toString()
        binding.temperatureDegree.text = units.toDegreeSuffix()

        val weatherType = current.weather.firstOrNull()?.main.orEmpty()
        binding.weatherType.text = weatherType
        loadWeatherGif(binding.weatherImage, weatherType)

        renderForecast(response, units)
    }

    private fun renderForecast(response: WeatherResponse, units: String) {
        val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val suffix = units.toDegreeSuffix()

        val dayViews = listOf(
            Triple(binding.weekday1, binding.temp1, binding.image1),
            Triple(binding.weekday2, binding.temp2, binding.image2),
            Triple(binding.weekday3, binding.temp3, binding.image3),
            Triple(binding.weekday4, binding.temp4, binding.image4),
            Triple(binding.weekday5, binding.temp5, binding.image5)
        )

        for (i in 0 until Constants.FORECAST_DAYS) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val datePrefix = String.format(
                Locale.getDefault(),
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            val forecast = response.forecasts.find { it.dtTxt.startsWith(datePrefix) } ?: continue
            val (weekdayView, tempView, imageView) = dayViews[i]

            weekdayView.text = dayFormat.format(calendar.time)
            tempView.text = "${forecast.main.tempMax.toInt()}$suffix"
            loadWeatherGif(imageView, forecast.weather.firstOrNull()?.main.orEmpty())
        }
    }

    private fun loadWeatherGif(imageView: ImageView, weatherType: String) {
        val gifName = weatherType.toWeatherGifName()
        Glide.with(this)
            .asGif()
            .load("file:///android_asset/$gifName")
            .into(imageView)
    }

    // endregion

    override fun onDestroy() {
        super.onDestroy()
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        viewModel.navigator = null
    }
}
