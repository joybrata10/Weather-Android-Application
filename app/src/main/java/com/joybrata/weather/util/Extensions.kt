package com.joybrata.weather.util

import android.content.Context
import android.widget.Toast

/**
 * Created by Joybrata Paul on 10/08/2024
 **/

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

/** Maps an OpenWeatherMap "main" weather condition to a bundled gif asset name. */
fun String.toWeatherGifName(): String = when (lowercase()) {
    "clear" -> Constants.GIF_SUN
    "clouds" -> Constants.GIF_CLOUDS
    "rain", "drizzle", "thunderstorm" -> Constants.GIF_RAIN
    "snow" -> Constants.GIF_SNOW
    else -> Constants.GIF_ALERT
}

/** Degree suffix for the given temperature unit. */
fun String.toDegreeSuffix(): String =
    if (this == Constants.UNIT_METRIC) "\u00B0C" else "\u00B0F"
