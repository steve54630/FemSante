package com.audreyRetournayDiet.femSante.features.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import timber.log.Timber

/**
 * Activité dédiée à la Sophrologie dans le module "Bien dans sa tête".
 *
 * Elle propose deux types d'exercices :
 * 1. **Relaxation Dynamique (Vidéo)** : Exercices physiques légers (épaules, miroir, éventails, respiration thoracique).
 * 2. **Sophronisation (Audio)** : Séances de relaxation profonde guidées par la voix.
 */
class SophroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sophro)
        Timber.d("onCreate: Initialisation de l'espace Sophrologie")

        setupVideoListeners()
        setupAudioListeners()
    }

    /**
     * Configure les boutons d'exercices physiques de sophrologie.
     * Récupère le texte du bouton dynamiquement pour identifier la vidéo à lancer.
     */
    private fun setupVideoListeners() {
        val intentVideo = Intent(this, VideoActivity::class.java)

        // Liste des IDs de boutons correspondant aux exercices de relaxation dynamique
        val videoButtons = listOf(
            R.id.buttonShoulder, // Exercice des épaules
            R.id.buttonMirror,   // Exercice du miroir
            R.id.buttonFans,     // Exercice des éventails
            R.id.buttonThoracic  // Respiration thoracique
        )

        videoButtons.forEach { resId ->
            findViewById<Button>(resId).setOnClickListener { view ->
                val button = view as Button
                val title = button.text.toString()

                Timber.i("Action: Lancement vidéo Sophro -> $title")

                // Utilisation de l'utilitaire global pour le lancement du viewer vidéo
                videoLaunch(title, "non", intentVideo, this)
            }
        }
    }

    /**
     * Configure l'accès aux séances audio de sophrologie.
     * Prépare une playlist spécifique envoyée au lecteur audio.
     */
    private fun setupAudioListeners() {
        val sophroButton = findViewById<Button>(R.id.buttonAudio)

        sophroButton.setOnClickListener {
            val title = sophroButton.text.toString()

            // Définition de la playlist audio (noms des fichiers/pistes)
            val playlist = arrayListOf(
                "Base vivantielle",
                "Déplacement du négatif"
            )

            Timber.i("Action: Lancement Playlist Audio -> $title (${playlist.size} pistes)")

            // Préparation de l'Intent avec la liste des pistes et le titre de la catégorie
            val intentAudio = Intent(this, AudioActivity::class.java).apply {
                putExtra("map", playlist)
                putExtra("Titre", title)
            }
            startActivity(intentAudio)
        }
    }
}