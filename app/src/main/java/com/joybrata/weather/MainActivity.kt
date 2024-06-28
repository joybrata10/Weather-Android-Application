package com.joybrata.weather

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.location.*
import com.joybrata.weather.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

//https://api.openweathermap.org/data/2.5/forecast?q=Kolkata&appid=fc1d719c0809d3e30f19acb6e5b3f356&units=metric

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationCallback: LocationCallback

    private var lastFetchedLocation: Pair<Double, Double>? = null
    private var lastFetchedCity: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        } else {
            fetchWeatherDataByLocation()
        }

        binding.fabSearch.setOnClickListener {
            val bottomSheet = CityInputBottomSheetFragment()
            bottomSheet.show(supportFragmentManager, "CityInputBottomSheet")
        }

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

        updateDateAndWeekday()
    }

    private fun updateDateAndWeekday() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val weekdayFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        binding.date.text = dateFormat.format(calendar.time)
        binding.weekday.text = weekdayFormat.format(calendar.time)
    }

    fun fetchWeatherDataByLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1
            )
        } else {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        handleLocationUpdate(location)
                        lastFetchedLocation = Pair(location.latitude, location.longitude)
                        lastFetchedCity = null
                        break // Only handle the first location
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun handleLocationUpdate(location: Location) {
        Log.d("MainActivity", "Fetched location: Latitude = ${location.latitude}, Longitude = ${location.longitude}")
        val unit = getTemperatureUnit()
        WeatherUtils.fetchWeatherData(location.latitude, location.longitude, unit, object : Callback<data> {
            override fun onResponse(call: Call<data>, response: Response<data>) {
                updateWeatherUI(response.body())
            }

            override fun onFailure(call: Call<data>, t: Throwable) {
                Log.d("MainActivity", "Failed to fetch weather data: ${t.message}")
            }
        })

        // Remove location updates after receiving one
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun fetchWeatherData(cityName: String) {
        val unit = getTemperatureUnit()
        WeatherUtils.fetchWeatherDataByCity(cityName, unit, object : Callback<data> {
            override fun onResponse(call: Call<data>, response: Response<data>) {
                if (response.isSuccessful) {
                    lastFetchedCity = cityName
                    lastFetchedLocation = null
                    updateWeatherUI(response.body())
                } else if (response.code() == 404) {
                    Toast.makeText(this@MainActivity, "Please enter a Valid Location", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<data>, t: Throwable) {
                Log.d("MainActivity", "Failed to fetch weather data: ${t.message}")
            }
        })
    }

    private fun updateWeatherUI(weatherData: data?) {
        weatherData?.let { data ->
            binding.tvCity.text = data.city.name
            binding.tvTemp.text = data.list[0].main.temp.toInt().toString()
            binding.temperatureDegree.text = if (getTemperatureUnit() == "metric") "°C" else "°F"

            // Update weather type and image
            val weatherType = data.list[0].weather[0].main
            binding.weatherType.text = weatherType
            loadWeatherGif(binding.weatherImage, weatherType)

            // Update forecast for next 5 days
            updateForecast(data)
        }
    }

    private fun updateForecast(data: data) {
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val calendar = Calendar.getInstance()

        for (i in 1..5) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayOfWeek = dateFormat.format(calendar.time)
            val forecast = data.list.find { it.dt_txt.startsWith(calendar.get(Calendar.YEAR).toString() + "-" +
                    String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" +
                    String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))) }

            forecast?.let {
                when (i) {
                    1 -> {
                        binding.weekday1.text = dayOfWeek
                        binding.temp1.text = "${it.main.temp_max.toInt()}" + "" + if (getTemperatureUnit() == "metric") "°C" else "°F"
                        loadWeatherGif(binding.image1, it.weather[0].main)
                    }
                    2 -> {
                        binding.weekday2.text = dayOfWeek
                        binding.temp2.text = "${it.main.temp_max.toInt()}" + "" + if (getTemperatureUnit() == "metric") "°C" else "°F"
                        loadWeatherGif(binding.image2, it.weather[0].main)
                    }
                    3 -> {
                        binding.weekday3.text = dayOfWeek
                        binding.temp3.text = "${it.main.temp_max.toInt()}" + "" + if (getTemperatureUnit() == "metric") "°C" else "°F"
                        loadWeatherGif(binding.image3, it.weather[0].main)
                    }
                    4 -> {
                        binding.weekday4.text = dayOfWeek
                        binding.temp4.text = "${it.main.temp_max.toInt()}" + "" + if (getTemperatureUnit() == "metric") "°C" else "°F"
                        loadWeatherGif(binding.image4, it.weather[0].main)
                    }
                    5 -> {
                        binding.weekday5.text = dayOfWeek
                        binding.temp5.text = "${it.main.temp_max.toInt()}" + "" + if (getTemperatureUnit() == "metric") "°C" else "°F"
                        loadWeatherGif(binding.image5, it.weather[0].main)
                    }
                }
            }
        }
    }

    private fun getWeatherGifName(weatherType: String): String {
        return when (weatherType.lowercase(Locale.ROOT)) {
            "clear" -> "sun.gif"
            "clouds" -> "clouds.gif"
            "rain" -> "rain.gif"
            "snow" -> "winter.gif"
            else -> "alert.gif"
        }
    }

    private fun loadWeatherGif(imageView: ImageView, weatherType: String) {
        val gifName = getWeatherGifName(weatherType)
        Log.d("GifLoading", "Attempting to load gif: $gifName")
        Glide.with(this)
            .asGif()
            .load("file:///android_asset/$gifName")
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<GifDrawable>?, isFirstResource: Boolean): Boolean {
                    Log.e("GifLoading", "Failed to load gif: $gifName", e)
                    return false
                }

                override fun onResourceReady(resource: GifDrawable?, model: Any?, target: Target<GifDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    Log.d("GifLoading", "Successfully loaded gif: $gifName")
                    return false
                }
            })
            .into(imageView)
    }

    private fun showTemperatureUnitDialog() {
        val dialogFragment = TemperatureUnitDialogFragment { newUnit ->
            updateWeatherForNewUnit(newUnit)
        }
        dialogFragment.show(supportFragmentManager, "TemperatureUnitDialog")
    }

    private fun updateWeatherForNewUnit(newUnit: String) {
        when {
            lastFetchedCity != null -> fetchWeatherData(lastFetchedCity!!)
            lastFetchedLocation != null -> {
                val (lat, lon) = lastFetchedLocation!!
                WeatherUtils.fetchWeatherData(lat, lon, newUnit, object : Callback<data> {
                    override fun onResponse(call: Call<data>, response: Response<data>) {
                        updateWeatherUI(response.body())
                    }

                    override fun onFailure(call: Call<data>, t: Throwable) {
                        Log.d("MainActivity", "Failed to fetch weather data: ${t.message}")
                    }
                })
            }
            else -> fetchWeatherDataByLocation() // Fallback to current location if no data is stored
        }
    }

    private fun getTemperatureUnit(): String {
        val sharedPreferences = getSharedPreferences("WeatherPreferences", MODE_PRIVATE)
        return sharedPreferences.getString("temperature_unit", "metric") ?: "metric"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeatherDataByLocation()
        }
    }
}