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

class SophroActivity : AppCompatActivity() {

    private val tag = "ACT_SOPHRO"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sophro)
        Log.d(tag, "onCreate: Initialisation de l'espace Sophrologie")

        setupVideoListeners()
        setupAudioListeners()
    }

    private fun setupVideoListeners() {
        val intentVideo = Intent(this, VideoActivity::class.java)

        // Liste des boutons qui lancent une vidéo directe
        val videoButtons = listOf(
            R.id.buttonShoulder,
            R.id.buttonMirror,
            R.id.buttonFans,
            R.id.buttonThoracic
        )

        videoButtons.forEach { resId ->
            findViewById<Button>(resId).setOnClickListener { button ->
                val title = (button as Button).text.toString()
                Log.i(tag, "Action: Lancement vidéo Sophro -> $title")
                videoLaunch(title, "non", intentVideo, this)
            }
        }
    }

    private fun setupAudioListeners() {
        // Pour l'audio, on définit le bouton et sa playlist
        val sophroButton = findViewById<Button>(R.id.buttonAudio)

        sophroButton.setOnClickListener {
            val title = sophroButton.text.toString()
            val playlist = arrayListOf("Base vivantielle", "Déplacement du négatif")

            Log.i(tag, "Action: Lancement Playlist Audio -> $title (${playlist.size} pistes)")

            val intentAudio = Intent(this, AudioActivity::class.java).apply {
                putExtra("map", playlist)
                putExtra("Titre", title)
            }
            startActivity(intentAudio)
        }
    }
}