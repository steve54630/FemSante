package com.audreyRetournayDiet.femSante.utilitaires

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.*
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R

class VideoActivity : AppCompatActivity() {

    private lateinit var titre: TextView
    private var pdf: Button? = null
    private var fullScreen: Button? = null
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    @RequiresApi(Build.VERSION_CODES.R)
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        playerView = findViewById(R.id.videoView)
        player = Builder(this).build()
        titre = findViewById(R.id.textVid)
        pdf = findViewById(R.id.pdfButton)
        fullScreen = findViewById(R.id.buttonFS)

        val map: HashMap<*, *>? =
            intent.getSerializableExtra("map") as HashMap<*, *>?


        val videoUri = Uri.parse("asset:///" + map!!["Title"].toString() + ".mp4")
        val item = MediaItem.fromUri(videoUri)
        val retriever = MediaMetadataRetriever()
        val afd = assets.openFd(map["Title"].toString() + ".mp4")
        retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        playerView.player = player

        player.setMediaItem(item)
        player.prepare()

        if (map["PDF"].toString() == "oui") {
            if (pdf != null) {
                pdf!!.visibility = View.VISIBLE
                pdf!!.setOnClickListener {
                    val intentTarget = Intent(this, PdfActivity::class.java)
                    intentTarget.putExtra("PDF", map["Title"].toString() + ".pdf")
                    startActivity(intentTarget)
                }
            }
        }

        if (fullScreen != null) {
            fullScreen!!.setOnClickListener {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                player.release()
            }
        }

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                window.decorView.windowInsetsController!!.hide(android.view.WindowInsets.Type.statusBars())
            else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        if (findViewById<TextView>(R.id.textVid) != null)
            titre.text = map["Title"].toString()

        val height =
            Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!)
        val width =
            Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!)

        if (height > width) {
            fullScreen!!.visibility = View.INVISIBLE
        }

    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onBackPressed() {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            player.release()
            super.onBackPressed()
        }
    }

    override fun onResume() {
        player.play()
        super.onResume()
    }

    override fun onPause() {
        player.pause()
        super.onPause()
    }


}