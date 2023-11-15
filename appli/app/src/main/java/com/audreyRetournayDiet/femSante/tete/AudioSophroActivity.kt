package com.audreyRetournayDiet.femSante.tete

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R

class AudioSophroActivity : AppCompatActivity() {

    private lateinit var sdn: Button
    private lateinit var sbv: Button
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_sophro)

        sdn = findViewById(R.id.buttonSDN)
        sbv = findViewById(R.id.buttonSBV)
        playerView = findViewById(R.id.audioPlayer)
        player = ExoPlayer.Builder(this).build()

        playerView.player = player

        sdn.setOnClickListener {
            playerView.visibility = View.VISIBLE
            val videoUri = Uri.parse("asset:///sdn.mp4")
            val item = MediaItem.fromUri(videoUri)

            player.setMediaItem(item)
            player.prepare()
            player.play()
        }

        sbv.setOnClickListener {
            playerView.visibility = View.VISIBLE
            val videoUri = Uri.parse("asset:///sbv.mp4")
            val item = MediaItem.fromUri(videoUri)

            player.setMediaItem(item)
            player.prepare()
            player.play()
        }
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }

    override fun onStart() {
        player.play()
        super.onStart()
    }

    override fun onStop() {
        player.pause()
        super.onStop()
    }


}