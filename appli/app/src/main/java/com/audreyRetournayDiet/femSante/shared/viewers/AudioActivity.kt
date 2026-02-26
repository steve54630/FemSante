package com.audreyRetournayDiet.femSante.shared.viewers

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.viewModels.viewers.AudioViewModel
import kotlinx.coroutines.launch

class AudioActivity : AppCompatActivity() {

    private val viewModel: AudioViewModel by viewModels {
        val title = intent.getStringExtra("Titre") ?: ""
        val map = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", ArrayList::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("map") as ArrayList<*>
        }
        AudioViewModel.Factory(VideoManager(this), title, map)
    }

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var spinner: Spinner
    private lateinit var titleTextView: TextView

    private val loadingAlert by lazy { LoadingAlert(this) }
    private var isLoaderShowing = false

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        initViews()
        setupPlayer()
        setupSpinner()
        observeState()
    }

    private fun initViews() {
        spinner = findViewById(R.id.spinnerExercice)
        playerView = findViewById(R.id.audioPlayer)
        titleTextView = findViewById(R.id.textTitle)
    }

    private fun setupPlayer() {
        player = Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
        playerView.player = player
    }

    private fun setupSpinner() {
        val exercises = viewModel.uiState.value.exercises
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, exercises)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_exo, this)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // position 0 est "nothing selected" dans le décorateur
                if (p2 > 0) {
                    viewModel.onExerciseSelected(spinner.selectedItem.toString())
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
                viewModel.onNothingSelected()
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // 1. UI Simple
                titleTextView.text = state.mainTitle
                playerView.visibility = if (state.isPlayerVisible) View.VISIBLE else View.GONE

                // 2. Gestion du Loader
                handleLoadingDialog(state.isLoading)

                // 3. Gestion d'erreur
                state.errorMessage?.let {
                    Toast.makeText(this@AudioActivity, it, Toast.LENGTH_LONG).show()
                }

                // 4. Gestion du Player
                state.currentAudioUri?.let { uri ->
                    if (player.currentMediaItem?.localConfiguration?.uri != uri) {
                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                        val mediaSource = HlsMediaSource.Factory(dataSourceFactory)
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

    override fun onStart() { super.onStart(); player.play() }
    override fun onStop() { super.onStop(); player.pause() }
    override fun onDestroy() { super.onDestroy(); player.release() }
}