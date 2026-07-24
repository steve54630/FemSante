package com.audreyRetournayDiet.femSante.features.calendar.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Hôte fin de [CalendarFragment]. Toute la logique du calendrier vit désormais dans le
 * fragment, réutilisé par l'onglet "Calendrier" de [com.audreyRetournayDiet.femSante.features.main.HomeActivity].
 * Cette activité reste un point d'entrée de secours (deep link, notification…).
 */
@AndroidEntryPoint
class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom_calendar)
    }
}
