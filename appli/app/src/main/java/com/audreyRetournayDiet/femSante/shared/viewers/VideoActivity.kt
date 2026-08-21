package com.audreyRetournayDiet.femSante.shared.viewers

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.redirectToLoginAfterSessionExpiry
import com.audreyRetournayDiet.femSante.viewmodels.viewers.VideoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecteur vidéo (flux HLS sécurisé). Deux modes :
 * - **Fenêtré** (portrait) : titre + cadre vidéo à ratio réel + bouton PDF éventuel.
 * - **Plein écran** (paysage immersif) : la barre système disparaît, le cadre remplit l'écran.
 *
 * Le basculement se fait via un unique [applyFullscreen] (l'état vient du [VideoViewModel]), et le
 * mode immersif via [WindowInsetsControllerCompat] (API non dépréciée).
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    private val viewModel: VideoViewModel by viewModels()

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var videoFrame: FrameLayout
    private lateinit var fullScreenButton: ImageButton
    private lateinit var titleText: TextView
    private lateinit var pdfButton: Button

    private val loadingAlert by lazy { LoadingAlert(this) }
    private var isLoaderShowing = false
    private var lastError: String? = null

    /** Ratio réel de la vidéo courante (mis à jour au premier rendu), pour un cadre sans marge noire. */
    private var videoRatio: String = "16:9"

    /** Dernier mode appliqué : évite de re-layouter à chaque émission d'état. */
    private var appliedFullscreen: Boolean? = null

    private val insetsController by lazy {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        // Contenu protégé : pas de capture d'écran ni d'aperçu multitâche.
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        initViews()
        setupPlayer()
        observeState()
        setupBackHandler()
    }

    private fun initViews() {
        playerView = findViewById(R.id.videoView)
        videoFrame = findViewById(R.id.videoViewLayout)
        titleText = findViewById(R.id.textVid)
        pdfButton = findViewById(R.id.pdfButton)
        fullScreenButton = findViewById(R.id.fullscreen)

        fullScreenButton.setOnClickListener { viewModel.toggleFullScreen() }
        pdfButton.setOnClickListener {
            startActivity(
                Intent(this, PdfActivity::class.java)
                    .putExtra("PDF", viewModel.uiState.value.pdfFileName)
            )
        }
    }

    private fun setupPlayer() {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility ->
                fullScreenButton.isVisible = visibility == View.VISIBLE
            }
        )

        // Ajuste le cadre au format réel de la vidéo (évite les bandes noires).
        player.addListener(object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                if (videoSize.width <= 0 || videoSize.height <= 0) return
                videoRatio = "${videoSize.width}:${videoSize.height}"
                if (!viewModel.uiState.value.isFullScreen) applyVideoRatio(videoRatio)
            }
        })
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state.sessionExpired) {
                        redirectToLoginAfterSessionExpiry()
                        return@collect
                    }

                    titleText.text = state.title
                    pdfButton.isVisible = state.isPdfVisible && !state.isFullScreen
                    handleLoadingDialog(state.isLoading)
                    showErrorOnce(state.errorMessage)
                    applyFullscreen(state.isFullScreen)
                    prepareIfNeeded(state.videoUri)
                }
            }
        }
    }

    /** Prépare la source HLS une seule fois par URL. */
    private fun prepareIfNeeded(uri: android.net.Uri?) {
        uri ?: return
        if (player.currentMediaItem?.localConfiguration?.uri == uri) return
        val source = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
            .createMediaSource(MediaItem.fromUri(uri))
        player.setMediaSource(source)
        player.prepare()
        player.play()
        playerView.showController()
    }

    /**
     * Bascule fenêtré ⇄ plein écran en une seule passe : orientation, mode immersif, visibilité du
     * titre/PDF et dimensionnement du cadre (ratio réel ↔ plein écran).
     */
    private fun applyFullscreen(fullscreen: Boolean) {
        if (appliedFullscreen == fullscreen) return
        appliedFullscreen = fullscreen
        titleText.isVisible = !fullscreen
        applyImmersive(fullscreen)
        requestedOrientation =
            if (fullscreen) ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Le cadre est contraint haut ET bas au parent : sans ratio il remplit l'écran (plein
        // écran), avec le ratio réel il forme la vignette centrée verticalement.
        applyVideoRatio(if (fullscreen) null else videoRatio)
    }

    private fun applyVideoRatio(ratio: String?) {
        val lp = videoFrame.layoutParams as ConstraintLayout.LayoutParams
        lp.dimensionRatio = ratio
        videoFrame.layoutParams = lp
    }

    private fun applyImmersive(enabled: Boolean) {
        if (enabled) insetsController.hide(WindowInsetsCompat.Type.systemBars())
        else insetsController.show(WindowInsetsCompat.Type.systemBars())
    }

    private fun setupBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.uiState.value.isFullScreen) viewModel.toggleFullScreen() else finish()
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

    /** N'affiche un message d'erreur qu'une fois (le state peut ré-émettre). */
    private fun showErrorOnce(message: String?) {
        if (message == null || message == lastError) return
        lastError = message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        player.play()
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        player.release()
    }
}
