package com.joybrata.weather.data.repository

import com.joybrata.weather.BuildConfig
import com.joybrata.weather.data.model.WeatherResponse
import com.joybrata.weather.data.network.ApiClient
import com.joybrata.weather.data.network.ApiInterface
import com.joybrata.weather.util.Resource
import java.io.IOException

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Single entry point for all weather data. All methods are suspend functions
 * meant to be called from a coroutine on [kotlinx.coroutines.Dispatchers.IO].
 **/
class WeatherRepository(
    private val api: ApiInterface = ApiClient.apiInterface
) {

    suspend fun getWeatherByCity(cityName: String, units: String): Resource<WeatherResponse> =
        safeCall { api.getWeatherByCity(cityName, BuildConfig.WEATHER_API_KEY, units) }

    suspend fun getWeatherByLocation(
        lat: Double,
        lon: Double,
        units: String
    ): Resource<WeatherResponse> =
        safeCall { api.getWeatherByLocation(lat, lon, BuildConfig.WEATHER_API_KEY, units) }

    private inline fun <T> safeCall(block: () -> retrofit2.Response<T>): Resource<T> {
        return try {
            val response = block()
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Resource.Success(body)
            } else {
                Resource.Error(
                    message = response.message().ifBlank { "Something went wrong" },
                    code = response.code()
                )
            }
        } catch (e: IOException) {
            Resource.Error("No internet connection")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unexpected error")
        }
    }
}