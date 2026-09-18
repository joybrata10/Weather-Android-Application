package com.joybrata.weather.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * DataStore-backed replacement for the old SharedPreferences usage. Stores the
 * user's preferred temperature unit as the single source of truth.
 **/
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

class AppPreferences(private val context: Context) {

    private object Keys {
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    }

    /** Emits the current temperature unit, defaulting to metric. */
    val temperatureUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.TEMPERATURE_UNIT] ?: Constants.UNIT_METRIC
    }

    suspend fun getTemperatureUnit(): String = temperatureUnit.first()

    suspend fun setTemperatureUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TEMPERATURE_UNIT] = unit
        }
    }
}
