package com.joybrata.weather.ui.base

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Root navigator contract implemented by every screen. ViewModels talk back to
 * the UI exclusively through a navigator so they never hold Android references.
 **/
interface BaseNavigator {
    fun showProgress()
    fun hideProgress()
    fun showError(message: String)
}
