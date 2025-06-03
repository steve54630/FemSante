package com.audreyRetournayDiet.femSante.utilitaires

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R

@Suppress("DEPRECATION")
class VideoActivity : AppCompatActivity() {

    private var isPortraitVideo = false
    private var isFullScreen  = false
    private var titre: TextView? = null
    private var pdf: Button? = null
    private lateinit var fullScreen: ImageButton
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var map: HashMap<*, *>

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

        playerView = findViewById(R.id.videoView)
        player = Builder(this).build()
        titre = findViewById(R.id.textVid)
        pdf = findViewById(R.id.pdfButton)
        fullScreen = playerView.findViewById(R.id.exo_fullscreen)


        map = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                intent.getSerializableExtra("map", HashMap::class.java)!!

            else -> intent.getSerializableExtra("map")
                    as HashMap<*, *>
        }


        val videoUri = Uri.parse("asset:///${map["Title"].toString()}.mp4")
        val item = MediaItem.fromUri(videoUri)

        val retriever = MediaMetadataRetriever()
        val afd = assets.openFd(map["Title"].toString() + ".mp4")
        retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

        playerView.player = player
        player.setMediaItem(item)
        player.prepare()

        if (map["PDF"].toString() == "oui") {
                pdf?.visibility = View.VISIBLE
                pdf?.setOnClickListener {
                    val intentTarget = Intent(this, PdfActivity::class.java)
                    intentTarget.putExtra("PDF", map["Title"].toString() + ".pdf")
                    startActivity(intentTarget)
            }
        }

        fullScreen.setOnClickListener {
            toggleFullScreen()
        }

        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                window.decorView.windowInsetsController!!.hide(android.view.WindowInsets.Type.statusBars())
            else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        titre?.text = map["Title"].toString()

        val height =
            Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!)
        val width =
            Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!)

        isPortraitVideo = height > width

        if (isPortraitVideo) {
            fullScreen.visibility = View.INVISIBLE
            playerView.layoutParams =
                ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            titre!!.visibility = View.INVISIBLE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    supportActionBar?.show()
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                } else {
                    player.release()
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun toggleFullScreen() {
        if (isFullScreen) {
            exitFullScreen()
        } else {
            enterFullScreen()
        }
    }

    private fun enterFullScreen() {
        isFullScreen = true
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        playerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (!isPortraitVideo) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun exitFullScreen() {
        isFullScreen = false
        supportActionBar?.show()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        playerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun onResume() {
        player.play()
        super.onResume()
    }

    override fun onPause() {
        player.pause()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(LayoutParams.FLAG_SECURE)
    }


}