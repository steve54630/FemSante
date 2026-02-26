package com.audreyRetournayDiet.femSante.domain.tete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.Utilitaires.videoLaunch
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity

class BienTeteActivity : AppCompatActivity() {

    private lateinit var artTherapie: Button
    private lateinit var emotion: Button
    private lateinit var sophro: Button
    private lateinit var medit: Button
    private lateinit var hypnosis: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_tete)

        val intentVideo = Intent(
            this, VideoActivity::class.java
        )

        val intentAudio = Intent(this, AudioActivity::class.java)

        artTherapie = findViewById(R.id.buttonArt)
        sophro = findViewById(R.id.buttonSophro)
        emotion = findViewById(R.id.buttonEmotion)
        medit = findViewById(R.id.buttonMedit)
        hypnosis = findViewById(R.id.buttonHypno)

        hypnosis.setOnClickListener {
            val array = ArrayList<String>()
            array.add("Auto hypnose pour le stress")
            array.add("Auto-hypnose pour l'apaisement")

            intentAudio.putExtra("map", array)
            intentAudio.putExtra("Titre", "${hypnosis.text}")

            startActivity(intentAudio)
        }

        medit.setOnClickListener {
            val array = ArrayList<String>()

            array.add("Calmer la colère")
            array.add("Calmer la douleur")
            array.add("Confiance en soi")
            array.add("Relaxation")

            intentAudio.putExtra("map", array)
            intentAudio.putExtra("Titre", "${medit.text}")

            startActivity(intentAudio)
        }

        sophro.setOnClickListener {
            startActivity(Intent(this, SophroActivity::class.java))
        }

        emotion.setOnClickListener {
            videoLaunch(emotion.text.toString(), "non", intentVideo, this)
        }


        artTherapie.setOnClickListener {
            startActivity(Intent(this, ArtTherapieActivity::class.java))
        }

    }
}