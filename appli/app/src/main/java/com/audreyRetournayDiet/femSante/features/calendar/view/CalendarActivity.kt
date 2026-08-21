package com.audreyRetournayDiet.femSante.features.calendar.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hôte fin de [CalendarFragment], réutilisé par l'onglet Calendrier de
 * [com.audreyRetournayDiet.femSante.features.main.HomeActivity]. Reste un point d'entrée de
 * secours (deep link, notification…).
 */
@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)
    }
}
