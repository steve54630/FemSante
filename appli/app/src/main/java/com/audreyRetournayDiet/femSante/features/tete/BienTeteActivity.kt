package com.audreyRetournayDiet.femSante.features.tete

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity

class BienTeteActivity : AppCompatActivity() {

    private val tag = "ACT_BIEN_TETE"
    private lateinit var artTherapie: Button
    private lateinit var emotion: Button
    private lateinit var sophro: Button
    private lateinit var medit: Button
    private lateinit var hypnosis: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_tete)
        Log.d(tag, "onCreate: Chargement du menu Bien-être Mental")

        initViews()
        setupListeners()
    }

    private fun initViews() {
        artTherapie = findViewById(R.id.buttonArt)
        sophro = findViewById(R.id.buttonSophro)
        emotion = findViewById(R.id.buttonEmotion)
        medit = findViewById(R.id.buttonMedit)
        hypnosis = findViewById(R.id.buttonHypno)
    }

    private fun setupListeners() {
        val intentVideo = Intent(this, VideoActivity::class.java)
        val intentAudio = Intent(this, AudioActivity::class.java)

        // --- SECTION AUDIO : HYPNOSE ---
        hypnosis.setOnClickListener {
            val title = hypnosis.text.toString()
            val tracks = arrayListOf(
                "Auto hypnose pour le stress",
                "Auto-hypnose pour l'apaisement"
            )
            Log.i(tag, "Navigation: Vers AudioActivity (Mode: $title, Tracks: ${tracks.size})")

            intentAudio.putExtra("map", tracks)
            intentAudio.putExtra("Titre", title)
            startActivity(intentAudio)
        }

        // --- SECTION AUDIO : MÉDITATION ---
        medit.setOnClickListener {
            val title = medit.text.toString()
            val tracks = arrayListOf(
                "Calmer la colère",
                "Calmer la douleur",
                "Confiance en soi",
                "Relaxation"
            )
            Log.i(tag, "Navigation: Vers AudioActivity (Mode: $title, Tracks: ${tracks.size})")

            intentAudio.putExtra("map", tracks)
            intentAudio.putExtra("Titre", title)
            startActivity(intentAudio)
        }

        // --- SECTION NAVIGATION DIRECTE ---
        sophro.setOnClickListener {
            Log.d(tag, "Navigation: Vers SophroActivity")
            startActivity(Intent(this, SophroActivity::class.java))
        }

        artTherapie.setOnClickListener {
            Log.d(tag, "Navigation: Vers ArtTherapieActivity")
            startActivity(Intent(this, ArtTherapieActivity::class.java))
        }

        // --- SECTION VIDÉO DIRECTE ---
        emotion.setOnClickListener {
            val videoTitle = emotion.text.toString()
            Log.i(tag, "Navigation: Lancement vidéo directe -> $videoTitle")
            videoLaunch(videoTitle, "non", intentVideo, this)
        }
    }
}