package com.audreyRetournayDiet.femSante.domain.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.AudioActivity
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.utilitaires.VideoActivity

class SophroActivity : AppCompatActivity() {

    private lateinit var shoulder: Button
    private lateinit var mirror: Button
    private lateinit var fans: Button
    private lateinit var sophronisation: Button
    private lateinit var thoracic: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sophro)

        shoulder = findViewById(R.id.buttonShoulder)
        mirror = findViewById(R.id.buttonMirror)
        fans = findViewById(R.id.buttonFans)
        sophronisation = findViewById(R.id.buttonAudio)
        thoracic = findViewById(R.id.buttonThoracic)

        val intentAudio = Intent(this, AudioActivity::class.java)

        val intentVideo = Intent(
            this, VideoActivity::class.java
        )

        sophronisation.setOnClickListener {
            val array = ArrayList<String>()
            array.add("Base vivantielle")
            array.add("Déplacement du négatif")

            intentAudio.putExtra("map", array)
            intentAudio.putExtra("Titre", "${sophronisation.text}")

            startActivity(intentAudio)
        }

        shoulder.setOnClickListener {
            videoLaunch(shoulder.text.toString(), "non", intentVideo, this)
        }

        mirror.setOnClickListener {
            videoLaunch(mirror.text.toString(), "non", intentVideo, this)
        }

        fans.setOnClickListener {
            videoLaunch(fans.text.toString(), "non", intentVideo, this)
        }

        thoracic.setOnClickListener {
            videoLaunch(thoracic.text.toString(), "non", intentVideo, this)
        }

    }
}