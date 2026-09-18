package com.joybrata.weather.util

/**
 * Created by Joybrata Paul on 10/08/2024
 **/
object Constants {

    // Temperature units accepted by the OpenWeatherMap API.
    const val UNIT_METRIC = "metric"
    const val UNIT_IMPERIAL = "imperial"

    // Fragment tags
    const val TAG_CITY_INPUT = "CityInputBottomSheet"
    const val TAG_TEMPERATURE_UNIT = "TemperatureUnitDialog"

    // Location request tuning
    const val LOCATION_UPDATE_INTERVAL_MS = 10_000L
    const val LOCATION_MIN_UPDATE_INTERVAL_MS = 5_000L
    const val LOCATION_PERMISSION_REQUEST_CODE = 1

    // Number of forecast days rendered on the main screen.
    const val FORECAST_DAYS = 5

    // Weather asset gif file names.
    const val GIF_SUN = "sun.gif"
    const val GIF_CLOUDS = "clouds.gif"
    const val GIF_RAIN = "rain.gif"
    const val GIF_SNOW = "winter.gif"
    const val GIF_ALERT = "alert.gif"
}
