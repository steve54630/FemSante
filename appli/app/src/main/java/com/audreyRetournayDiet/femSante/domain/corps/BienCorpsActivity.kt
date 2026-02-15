package com.audreyRetournayDiet.femSante.domain.corps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires
import com.audreyRetournayDiet.femSante.utilitaires.VideoActivity

class BienCorpsActivity : AppCompatActivity() {

    private lateinit var fitness : Button
    private lateinit var yoga : Button
    private lateinit var pilates : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_corps)

        fitness = findViewById(R.id.buttonFitness)
        yoga = findViewById(R.id.buttonYoga)
        pilates = findViewById(R.id.buttonPilates)

        val intentVideo = Intent(this
            , VideoActivity::class.java)

        yoga.setOnClickListener {
            startActivity(Intent(this, YogaActivity::class.java))
        }

        pilates.setOnClickListener {
            Utilitaires.videoLaunch("Pilates", "non", intentVideo, this)
        }

        fitness.setOnClickListener {
            Utilitaires.videoLaunch("Fitness", "non", intentVideo, this)
        }

    }
}