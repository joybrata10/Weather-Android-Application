package com.joybrata.weather

data class data(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<DT>,
    val message: Int
)
data class WeatherResponse(
    val city: City,
    val list: List<WeatherForecast>
)

data class WeatherForecast(
    val dtTxt: String,
    val main: Main,
    val weather: List<Weather>
)
