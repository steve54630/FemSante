package com.audreyRetournayDiet.femSante.data

import android.app.Application
import com.audreyRetournayDiet.femSante.BuildConfig
import timber.log.Timber

class FemSanteApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            // On crée un "Arbre" personnalisé pour ajouter un préfixe à TOUS tes logs
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    // Ajoute un préfixe fixe + le nom de la classe (ex: 🌸_PaymentActivity)
                    return "FEM_SANTE_${super.createStackElementTag(element)}"
                }
            })
        }
    }
}