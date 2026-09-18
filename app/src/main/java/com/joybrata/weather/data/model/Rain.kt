package com.joybrata.weather.data.model

import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("3h") val threeHour: Double = 0.0
)
