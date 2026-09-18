package com.joybrata.weather.ui.base

import androidx.lifecycle.ViewModel
import java.lang.ref.WeakReference

/**
 * Created by Joybrata Paul on 10/08/2024
 *
 * Base for all screen ViewModels. Holds the navigator via a [WeakReference] to
 * avoid leaking the hosting Activity/Fragment.
 **/
open class BaseViewModel<N : BaseNavigator> : ViewModel() {

    private var navigatorRef: WeakReference<N>? = null

    var navigator: N?
        get() = navigatorRef?.get()
        set(value) {
            navigatorRef = value?.let { WeakReference(it) }
        }
}