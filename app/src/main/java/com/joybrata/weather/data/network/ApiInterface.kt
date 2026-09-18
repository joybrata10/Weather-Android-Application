package com.joybrata.weather.data.network

import com.joybrata.weather.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Retrofit endpoints for the OpenWeatherMap 5-day / 3-hour forecast API.
 **/
interface ApiInterface {
    @GET("forecast")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<WeatherResponse>
}