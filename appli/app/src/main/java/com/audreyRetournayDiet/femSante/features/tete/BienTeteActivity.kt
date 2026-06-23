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
 * Menu principal du module "Bien dans sa tête" (Santé Mentale).
 * * Cette activité centralise l'accès aux différentes thérapies brèves et outils
 * de relaxation. Elle gère trois types de flux :
 * 1. **Navigation indirecte** : Vers des menus spécialisés ([ArtTherapieActivity], [SophroActivity]).
 * 2. **Flux Audio Dynamique** : Prépare des listes de pistes pour [AudioActivity] (Hypnose, Méditation).
 * 3. **Flux Vidéo Direct** : Lance un contenu vidéo unique via [videoLaunch].
 */
class BienTeteActivity : AppCompatActivity() {

    private lateinit var artTherapie: Button
    private lateinit var emotion: Button
    private lateinit var sophro: Button
    private lateinit var medit: Button
    private lateinit var hypnosis: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_tete)
        Timber.d("onCreate: Chargement du menu Bien-être Mental")

        initViews()
        setupListeners()
    }

    /**
     * Initialise les composants de l'interface utilisateur.
     */
    private fun initViews() {
        artTherapie = findViewById(R.id.buttonArt)
        sophro = findViewById(R.id.buttonSophro)
        emotion = findViewById(R.id.buttonEmotion)
        medit = findViewById(R.id.buttonMedit)
        hypnosis = findViewById(R.id.buttonHypno)
    }

    /**
     * Configure la logique de navigation et le passage de données entre activités.
     */
    private fun setupListeners() {
        val intentVideo = Intent(this, VideoActivity::class.java)
        val intentAudio = Intent(this, AudioActivity::class.java)

        // --- SECTION AUDIO : HYPNOSE ---
        // On définit dynamiquement la liste des pistes disponibles pour ce mode
        hypnosis.setOnClickListener {
            val title = hypnosis.text.toString()
            val tracks = arrayListOf(
                "Auto hypnose pour le stress",
                "Auto-hypnose pour l'apaisement"
            )
            Timber.i("Navigation: Vers AudioActivity (Mode: $title, Tracks: ${tracks.size})")

            intentAudio.putExtra("map", tracks)
            intentAudio.putExtra("Titre", title)
            startActivity(intentAudio)
        }

        // --- SECTION AUDIO : MÉDITATION ---
        // Structure identique à l'hypnose mais avec des contenus spécifiques
        medit.setOnClickListener {
            val title = medit.text.toString()
            val tracks = arrayListOf(
                "Calmer la colère",
                "Calmer la douleur",
                "Confiance en soi",
                "Relaxation"
            )
            Timber.i("Navigation: Vers AudioActivity (Mode: $title, Tracks: ${tracks.size})")

            intentAudio.putExtra("map", tracks)
            intentAudio.putExtra("Titre", title)
            startActivity(intentAudio)
        }

        // --- SECTION NAVIGATION DIRECTE ---
        // Redirection vers des activités ayant leur propre logique complexe
        sophro.setOnClickListener {
            Timber.d("Navigation: Vers SophroActivity")
            startActivity(Intent(this, SophroActivity::class.java))
        }

        artTherapie.setOnClickListener {
            Timber.d("Navigation: Vers ArtTherapieActivity")
            startActivity(Intent(this, ArtTherapieActivity::class.java))
        }

        // --- SECTION VIDÉO DIRECTE ---
        // Utilise l'utilitaire de lancement pour une vidéo liée à la gestion des émotions
        emotion.setOnClickListener {
            val videoTitle = emotion.text.toString()
            Timber.i("Navigation: Lancement vidéo directe -> $videoTitle")
            videoLaunch(videoTitle, "non", intentVideo, this)
        }
    }
}