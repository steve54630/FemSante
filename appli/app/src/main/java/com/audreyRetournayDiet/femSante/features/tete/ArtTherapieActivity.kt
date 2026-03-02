package com.audreyRetournayDiet.femSante.features.tete

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity

class ArtTherapieActivity : AppCompatActivity() {

    private val tag = "ACT_ART_THERAPIE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art_therapie)
        Log.d(tag, "onCreate: Chargement de l'écran Art-Thérapie")

        setupListeners()
    }

    private fun setupListeners() {
        val intentVideo = Intent(this, VideoActivity::class.java)

        // Mapping des IDs de ressources vers les titres de vidéos
        val emotionConfig = mapOf(
            R.id.buttonJoy to "Joie",
            R.id.buttonSadness to "Tristesse",
            R.id.buttonAnger to "Colère",
            R.id.buttonFear to "Peur"
        )

        emotionConfig.forEach { (resId, title) ->
            findViewById<Button>(resId).setOnClickListener {
                Log.i(tag, "Action: Lancement vidéo Art-Thérapie -> ${title.uppercase()}")
                videoLaunch(title, "oui", intentVideo, this)
            }
        }
    }
}