package com.audreyRetournayDiet.femSante.corps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import com.audreyRetournayDiet.femSante.utilitaires.VideoActivity

class YogaActivity : AppCompatActivity() {

    private lateinit var flow : Button
    private lateinit var calm : Button
    private lateinit var beginner : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)

        flow = findViewById(R.id.buttonFlow)
        calm = findViewById(R.id.buttonCalm)
        beginner = findViewById(R.id.buttonBeginner)

        val intentVideo = Intent(this
            , VideoActivity::class.java)

        flow.setOnClickListener {
            Utilitaires.videoLaunch("SOS Douleur", "non", intentVideo, this)
        }

        calm.setOnClickListener {
            Utilitaires.videoLaunch("Calme intérieur",  "non", intentVideo, this)
        }

        beginner.setOnClickListener {
            Utilitaires.videoLaunch("Débutant au Yoga",  "non", intentVideo, this)
        }

    }
}