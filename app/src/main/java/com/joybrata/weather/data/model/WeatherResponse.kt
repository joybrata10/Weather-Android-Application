package com.joybrata.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Root response returned by the OpenWeatherMap 5-day / 3-hour forecast endpoint.
 */
data class WeatherResponse(
    @SerializedName("cod") val cod: String? = null,
    @SerializedName("message") val message: Int = 0,
    @SerializedName("cnt") val count: Int = 0,
    @SerializedName("list") val forecasts: List<Forecast> = emptyList(),
    @SerializedName("city") val city: City = City()
)
