package com.joybrata.weather.ui.main

import com.joybrata.weather.data.model.WeatherResponse
import com.joybrata.weather.ui.base.BaseNavigator

/**
 * Created by Joybrata Paul on 10/08/2024
 **/
interface MainNavigator : BaseNavigator {
    fun onWeatherLoaded(response: WeatherResponse, units: String)
    fun onCityNotFound()
}
