package com.audreyRetournayDiet.femSante.shared.viewers

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerControlView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.redirectToLoginAfterSessionExpiry
import com.audreyRetournayDiet.femSante.viewmodels.viewers.AudioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Lecteur **audio** d'une séance (HLS/m3u8, streaming), pensé pour l'écoute : gros contrôles
 * de lecture toujours visibles, titre lisible, artwork. Reçoit **une seule piste** via
 * [EXTRA_TRACK] (plus de spinner) et démarre directement sa lecture.
 */
@AndroidEntryPoint
class AudioActivity : AppCompatActivity() {

    // Hilt injecte le ViewModel ; l'extra "Titre" est lu via SavedStateHandle pour l'en-tête.
    private val viewModel: AudioViewModel by viewModels()

    private lateinit var player: ExoPlayer
    private lateinit var controls: PlayerControlView
    private lateinit var titleTextView: TextView

    private val loadingAlert by lazy { LoadingAlert(this) }
    private var isLoaderShowing = false

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        controls = findViewById(R.id.audioControls)
        titleTextView = findViewById(R.id.textTitle)

        player = ExoPlayer.Builder(this).build()
        controls.player = player

        observeState()

        // Une piste unique fournie par l'appelant : on lance directement sa lecture.
        intent.getStringExtra(EXTRA_TRACK)?.takeIf { it.isNotBlank() }?.let {
            viewModel.onExerciseSelected(it)
        }
    }

    @OptIn(UnstableApi::class)
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (state.sessionExpired) {
                    redirectToLoginAfterSessionExpiry()
                    return@collect
                }

                titleTextView.text = state.mainTitle
                handleLoadingDialog(state.isLoading)

                state.errorMessage?.let {
                    Toast.makeText(this@AudioActivity, it, Toast.LENGTH_LONG).show()
                }

                state.currentAudioUri?.let { uri ->
                    if (player.currentMediaItem?.localConfiguration?.uri != uri) {
                        val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                            .createMediaSource(MediaItem.fromUri(uri))
                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.play()
                    }
                }
            }
        }
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

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    companion object {
        const val EXTRA_TITLE = "Titre"
        const val EXTRA_TRACK = "Track"
    }
}
