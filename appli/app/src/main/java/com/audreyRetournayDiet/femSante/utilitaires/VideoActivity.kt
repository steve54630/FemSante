package com.audreyRetournayDiet.femSante.utilitaires

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R

@Suppress("DEPRECATION")
class VideoActivity : AppCompatActivity() {

    private var isPortrait = true
    private var isFullScreen = false
    private var titre: TextView? = null
    private var pdf: Button? = null
    private lateinit var fullScreen: ImageButton
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var layoutVideo: FrameLayout
    private lateinit var map: HashMap<*, *>

    @UnstableApi
    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

        map = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                intent.getSerializableExtra("map", HashMap::class.java)!!

            else -> intent.getSerializableExtra("map")
                    as HashMap<*, *>
        }

        playerView = findViewById(R.id.videoView)
        player = Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(this)).build()

        titre = findViewById(R.id.textVid)
        titre?.text = map["Title"].toString()

        pdf = findViewById(R.id.pdfButton)
        layoutVideo = findViewById(R.id.videoViewLayout)

        fullScreen = layoutVideo.findViewById(R.id.fullscreen)

        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            if (visibility == View.VISIBLE && !isPortrait) {
                fullScreen.visibility = View.VISIBLE
            } else {
                fullScreen.visibility = View.GONE
            }
        })

        playerView.player = player

        val uri = map["URL"].toString().toUri()

        val dataSourceFactory = DefaultHttpDataSource.Factory()
        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(uri))

        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                val width = videoSize.width
                val height = videoSize.height
                if (height > width) {
                    setPortrait()
                } else {
                    isPortrait = false
                }
            }
        })


        player.setMediaSource(mediaSource)
        player.prepare()
        player.play()

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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    supportActionBar?.show()
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
                player.release()
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    private fun setPortrait() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        layoutVideo.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        titre!!.visibility = View.GONE
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
        layoutVideo.setBackgroundColor(Color.BLACK)
        titre?.visibility = View.GONE
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        layoutVideo.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun exitFullScreen() {
        isFullScreen = false
        supportActionBar?.show()
        layoutVideo.setBackgroundColor(Color.TRANSPARENT)
        titre?.visibility = View.VISIBLE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        layoutVideo.layoutParams = ViewGroup.LayoutParams(
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