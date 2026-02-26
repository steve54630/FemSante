package com.audreyRetournayDiet.femSante.features.corps

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import com.audreyRetournayDiet.femSante.viewModels.body.YogaViewModel
import kotlinx.coroutines.launch

class YogaActivity : AppCompatActivity() {

    private val viewModel: YogaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.buttonFlow).setOnClickListener { viewModel.onFlowClicked() }
        findViewById<Button>(R.id.buttonCalm).setOnClickListener { viewModel.onCalmClicked() }
        findViewById<Button>(R.id.buttonBeginner).setOnClickListener { viewModel.onBeginnerClicked() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is BodyNavigationEvent.LaunchVideo -> {
                        val intentVideo = Intent(this@YogaActivity, VideoActivity::class.java)
                        Utilitaires.videoLaunch(
                            event.category,
                            event.isPremium,
                            intentVideo,
                            this@YogaActivity
                        )
                    }
                    // On gère les autres cas de BodyNavigationEvent si nécessaire
                    else -> Unit
                }
            }
        }
    }
}