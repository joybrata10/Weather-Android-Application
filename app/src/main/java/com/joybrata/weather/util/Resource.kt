package com.joybrata.weather.util

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * A generic wrapper describing the outcome of a data operation so the UI layer
 * can react to loading / success / error states without leaking exceptions.
 **/
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val code: Int? = null) : Resource<Nothing>()
}
