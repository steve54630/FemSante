package com.audreyRetournayDiet.femSante.utilitaires

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.*
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R

class AudioActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var map: ArrayList<*>
    private lateinit var title: TextView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        spinner = findViewById(R.id.spinnerExercice)
        playerView = findViewById(R.id.audioPlayer)
        player = Builder(this).build()
        title = findViewById(R.id.textTitle)

        title.text = intent.getStringExtra("Titre")

        playerView.player = player

        map = intent.getSerializableExtra("map", ArrayList::class.java)!!

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, map)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.prompt = "Exercices audios"
        spinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_exo, this)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinner.selectedItemId != "-1".toLong()) {
                    playerView.visibility = View.VISIBLE

                    val videoUri = Uri.parse("asset:///${spinner.selectedItem}.mp4")

                    val item = MediaItem.fromUri(videoUri)
                    val retriever = MediaMetadataRetriever()
                    val afd = assets.openFd( "${spinner.selectedItem}.mp4")
                    retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

                    player.setMediaItem(item)
                    player.prepare()
                    player.play()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

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