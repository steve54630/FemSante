package com.audreyRetournayDiet.femSante.shared.viewers

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.viewModels.viewers.VideoViewModel
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@OptIn(UnstableApi::class)
class VideoActivity : AppCompatActivity() {

    private val viewModel: VideoViewModel by viewModels {
        val map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("map") as HashMap<*, *>
        }
        VideoViewModel.Factory(
            VideoManager(this),
            data = map
        )
    }

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var layoutVideo: FrameLayout
    private lateinit var fullScreenButton: ImageButton
    private lateinit var titleText: TextView
    private val loadingAlert by lazy { LoadingAlert(this) }
    private var isLoaderShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        // Sécurité contre les captures d'écran
        window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

        initViews()
        setupPlayer()
        observeState()
        setupBackHandler()
    }

    private fun initViews() {
        playerView = findViewById(R.id.videoView)
        layoutVideo = findViewById(R.id.videoViewLayout)
        titleText = findViewById(R.id.textVid)

        // Le bouton est dans le controller de PlayerView ou le FrameLayout
        fullScreenButton = findViewById(R.id.fullscreen)

        fullScreenButton.setOnClickListener { viewModel.toggleFullScreen() }

        findViewById<Button>(R.id.pdfButton).setOnClickListener {
            val state = viewModel.uiState.value
            val intentTarget = Intent(this, PdfActivity::class.java).apply {
                putExtra("PDF", state.pdfFileName)
            }
            startActivity(intentTarget)
        }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
        playerView.player = player

        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            val state = viewModel.uiState.value
            fullScreenButton.visibility =
                if (visibility == View.VISIBLE && !state.isPortraitVideo) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        })

        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                viewModel.setPortraitMode(videoSize.height > videoSize.width)
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                titleText.text = state.title
                findViewById<Button>(R.id.pdfButton).visibility =
                    if (state.isPdfVisible) View.VISIBLE else View.GONE

                // --- AJOUT ICI ---
                handleLoadingDialog(state.isLoading)
                // -----------------

                when {
                    state.isFullScreen -> enterFullScreen()
                    state.isPortraitVideo -> applyPortraitStyle()
                    else -> exitFullScreen()
                }

                state.videoUri?.let { uri ->
                    if (player.currentMediaItem?.localConfiguration?.uri != uri) {
                        val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                            .createMediaSource(MediaItem.fromUri(uri))
                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.play()
                        playerView.showController()
                    }
                }
            }
        }
    }

    private fun enterFullScreen() {
        supportActionBar?.hide()
        layoutVideo.setBackgroundColor(Color.BLACK)
        titleText.visibility = View.GONE
        setImmersiveMode()

        val params = layoutVideo.layoutParams as ConstraintLayout.LayoutParams
        params.width = MATCH_PARENT
        params.height = MATCH_PARENT
        layoutVideo.layoutParams = params

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun exitFullScreen() {
        supportActionBar?.show()
        layoutVideo.setBackgroundColor(Color.TRANSPARENT)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Reset exact du XML (ConstraintLayout)
        val params = layoutVideo.layoutParams as ConstraintLayout.LayoutParams
        params.width = 0
        params.height = WRAP_CONTENT
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
        params.verticalBias = 0.4f
        layoutVideo.layoutParams = params

        titleText.visibility = View.VISIBLE
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun applyPortraitStyle() {
        setImmersiveMode()
        titleText.visibility = View.GONE

        val params = layoutVideo.layoutParams as ConstraintLayout.LayoutParams
        params.width = MATCH_PARENT
        params.height = WRAP_CONTENT
        layoutVideo.layoutParams = params
    }

    private fun setImmersiveMode() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.isFullScreen) {
                    viewModel.toggleFullScreen()
                } else {
                    finish()
                }
            }
        })
    }

    private fun handleLoadingDialog(isLoading: Boolean) {
        if (isLoading && !isLoaderShowing) {
            loadingAlert.start()
            isLoaderShowing = true
        } else if (!isLoading && isLoaderShowing) {
            loadingAlert.close()
            isLoaderShowing = false
        }
    }

    override fun onResume() {
        super.onResume(); player.play()
    }

    override fun onPause() {
        super.onPause(); player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(LayoutParams.FLAG_SECURE)
        player.release()
    }
}