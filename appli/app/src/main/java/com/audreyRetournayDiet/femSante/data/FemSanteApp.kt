package com.audreyRetournayDiet.femSante.data

import android.app.Application
import com.audreyRetournayDiet.femSante.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FemSanteApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "FEM_SANTE_${super.createStackElementTag(element)}"
                }
            })
        }
    }
}