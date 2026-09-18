package com.joybrata.weather.util

import android.util.Log
import com.joybrata.weather.BuildConfig

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Centralized logging wrapper. Debug logs are emitted only in debug builds;
 * error logs are always emitted.
 **/
object Logger {

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
