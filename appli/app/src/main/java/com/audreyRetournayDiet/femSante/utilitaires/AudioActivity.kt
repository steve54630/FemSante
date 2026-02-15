package com.audreyRetournayDiet.femSante.utilitaires

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.PlayerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.ApiResult
import com.audreyRetournayDiet.femSante.repository.remote.VideoManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class AudioActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var playerView: PlayerView
    private lateinit var player: ExoPlayer
    private lateinit var map: ArrayList<*>
    private lateinit var title: TextView
    private lateinit var api : VideoManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        api = VideoManager(this)
        spinner = findViewById(R.id.spinnerExercice)
        playerView = findViewById(R.id.audioPlayer)
        player = Builder(this).setMediaSourceFactory(DefaultMediaSourceFactory(this)).build()
        title = findViewById(R.id.textTitle)

        title.text = intent.getStringExtra("Titre")

        playerView.player = player

        map = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                intent.getSerializableExtra("map", ArrayList::class.java)!!

            else -> @Suppress("DEPRECATION") intent.getSerializableExtra("map")
                    as ArrayList<*>
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, map)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.prompt = "Exercices audios"
        spinner.adapter =
            NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_exo, this)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @OptIn(UnstableApi::class) override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinner.selectedItemId != "-1".toLong()) {
                    playerView.visibility = View.VISIBLE

                    lifecycleScope.launch {

                        try {
                        val uri = fetchData(spinner.selectedItem.toString())
                        val dataSourceFactory = DefaultHttpDataSource.Factory()
                        val mediaSource: MediaSource = HlsMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(MediaItem.fromUri(uri!!))

                        player.setMediaSource(mediaSource)
                        player.prepare()
                        player.play()}
                        catch (e : Exception) {
                            Toast.makeText(p0!!.context, "${e.message}", Toast.LENGTH_LONG).show()
                        }

                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                playerView.visibility=View.GONE
                player.stop()
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

    private suspend fun fetchData(title: String): String? =
        suspendCancellableCoroutine { cont ->
            api.getVideoUrl(title) { result ->
                when (result) {
                    is ApiResult.Failure -> {
                        if (cont.isActive) {
                            cont.resumeWithException(Exception(result.message))
                        }
                    }

                    is ApiResult.Success -> {
                        if (cont.isActive) {
                            val url = result.data?.getString("url")
                            cont.resumeWith(Result.success(url))
                        }
                    }
                }
            }
        }
}