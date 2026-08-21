package com.audreyRetournayDiet.femSante.features.main

import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.shared.UserStore

/**
 * Écran "Mes préférences" accessible depuis Mon compte.
 *
 * Regroupe le paramétrage du suivi de cycle (profil + durée moyenne du cycle + durée des
 * règles). Les valeurs sont stockées localement dans [UserStore] (prefs chiffrées) et ne
 * quittent jamais l'appareil (RGPD).
 *
 * Conçu pour accueillir d'autres préférences utilisateur par la suite.
 */
class PreferencesActivity : AppCompatActivity() {

    private val userStore by lazy { UserStore(this) }

    private lateinit var radioGroupProfile: RadioGroup
    private lateinit var pickerCycleLength: NumberPicker
    private lateinit var pickerPeriodLength: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        radioGroupProfile = findViewById(R.id.radioGroupProfile)
        pickerCycleLength = findViewById<NumberPicker>(R.id.pickerCycleLength).apply {
            minValue = 20; maxValue = 40; value = userStore.getCycleLength()
        }
        pickerPeriodLength = findViewById<NumberPicker>(R.id.pickerPeriodLength).apply {
            minValue = 1; maxValue = 10; value = userStore.getPeriodLength()
        }

        // Pré-sélection du profil courant (défaut prudent : IRREGULIER).
        radioGroupProfile.check(
            when (userStore.getCycleProfile()) {
                CycleProfile.REGULIER -> R.id.radioRegular
                CycleProfile.IRREGULIER -> R.id.radioIrregular
                CycleProfile.ABSENT_OU_PILULE -> R.id.radioAbsent
            }
        )

        findViewById<Button>(R.id.buttonSavePreferences).setOnClickListener { savePreferences() }
    }

    private fun savePreferences() {
        val profile = when (radioGroupProfile.checkedRadioButtonId) {
            R.id.radioRegular -> CycleProfile.REGULIER
            R.id.radioAbsent -> CycleProfile.ABSENT_OU_PILULE
            else -> CycleProfile.IRREGULIER
        }
        userStore.setCycleProfile(profile)
        userStore.setCycleLength(pickerCycleLength.value)
        userStore.setPeriodLength(pickerPeriodLength.value)

        Toast.makeText(this, R.string.preferences_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
}
