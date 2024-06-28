package com.joybrata.weather

import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherUtils {
    private const val API_KEY = "fc1d719c0809d3e30f19acb6e5b3f356"
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiInterface = retrofit.create(ApiInterface::class.java)

    fun fetchWeatherData(lat: Double, lon: Double, units: String, callback: Callback<data>) {
        val call = apiInterface.getWeatherDataByLocation(lat, lon, API_KEY, units)
        call.enqueue(callback)
    }

    fun fetchWeatherDataByCity(cityName: String, units: String, callback: Callback<data>) {
        val call = apiInterface.getWeatherData(cityName, API_KEY, units)
        call.enqueue(callback)
    }
}