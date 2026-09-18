package com.joybrata.weather.data.model

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id") val id: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("coord") val coord: Coord = Coord(),
    @SerializedName("country") val country: String = "",
    @SerializedName("population") val population: Int = 0,
    @SerializedName("timezone") val timezone: Int = 0,
    @SerializedName("sunrise") val sunrise: Long = 0,
    @SerializedName("sunset") val sunset: Long = 0
)
