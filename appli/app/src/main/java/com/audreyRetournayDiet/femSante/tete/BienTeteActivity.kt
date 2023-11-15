package com.audreyRetournayDiet.femSante.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.utilitaires.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.utilitaires.VideoActivity

class BienTeteActivity : AppCompatActivity() {

    private lateinit var artTherapie: Button
    private lateinit var emotion: Button
    private lateinit var sophro: Button
    private lateinit var medit: Button
    private lateinit var hypnosis: Button
    private lateinit var self: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_tete)

        val intentVideo = Intent(
            this, VideoActivity::class.java
        )

        artTherapie = findViewById(R.id.buttonArt)
        sophro = findViewById(R.id.buttonSophro)
        emotion = findViewById(R.id.buttonEmotion)
        medit = findViewById(R.id.buttonMedit)
        hypnosis = findViewById(R.id.buttonHypno)
        self = findViewById(R.id.buttonConfience)


        medit.setOnClickListener {
            videoLaunch(medit.text.toString(), "non", intentVideo, this)
        }

        sophro.setOnClickListener {
            startActivity(Intent(this, SophroActivity::class.java))
        }

        emotion.setOnClickListener {
            videoLaunch(emotion.text.toString(), "non", intentVideo, this)
        }

        hypnosis.setOnClickListener {
            videoLaunch(hypnosis.text.toString(),  "non", intentVideo, this)
        }

        artTherapie.setOnClickListener {
            startActivity(Intent(this, ArtTherapieActivity::class.java))
        }

        self.setOnClickListener{
            videoLaunch(self.text.toString(),  "non", intentVideo, this)
        }
    }
}