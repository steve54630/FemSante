package com.audreyRetournayDiet.femSante.domain.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.utilitaires.VideoActivity

class ArtTherapieActivity : AppCompatActivity() {

    private lateinit var joy: Button
    private lateinit var sadness: Button
    private lateinit var anger: Button
    private lateinit var fear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_art_therapie)
        joy = findViewById(R.id.buttonJoy)
        anger = findViewById(R.id.buttonAnger)
        fear = findViewById(R.id.buttonFear)
        sadness = findViewById(R.id.buttonSadness)

        val intentVideo = Intent(this
            , VideoActivity::class.java)

        joy.setOnClickListener {
            videoLaunch("Joie", "oui", intentVideo, this)
        }

        sadness.setOnClickListener {
            videoLaunch("Tristesse", "oui", intentVideo, this)
        }

        anger.setOnClickListener {
            videoLaunch("Colère", "oui", intentVideo, this)
        }

        fear.setOnClickListener {
            videoLaunch("Peur", "oui" , intentVideo, this)
        }
    }

}