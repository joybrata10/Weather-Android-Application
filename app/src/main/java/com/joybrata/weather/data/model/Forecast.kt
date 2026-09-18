package com.joybrata.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * A single forecast entry (one 3-hour slot) inside [WeatherResponse.forecasts].
 */
data class Forecast(
    @SerializedName("dt") val dt: Long = 0,
    @SerializedName("main") val main: Main = Main(),
    @SerializedName("weather") val weather: List<Weather> = emptyList(),
    @SerializedName("clouds") val clouds: Clouds = Clouds(),
    @SerializedName("wind") val wind: Wind = Wind(),
    @SerializedName("visibility") val visibility: Int = 0,
    @SerializedName("pop") val pop: Double = 0.0,
    @SerializedName("rain") val rain: Rain? = null,
    @SerializedName("sys") val sys: Sys = Sys(),
    @SerializedName("dt_txt") val dtTxt: String = ""
)
