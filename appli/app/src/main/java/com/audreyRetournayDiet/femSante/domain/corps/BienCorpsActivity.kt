package com.audreyRetournayDiet.femSante.domain.corps

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
import com.audreyRetournayDiet.femSante.viewModels.body.BodyViewModel
import kotlinx.coroutines.launch

class BienCorpsActivity : AppCompatActivity() {

    private val viewModel: BodyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_corps)

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.buttonYoga).setOnClickListener { viewModel.onYogaClicked() }
        findViewById<Button>(R.id.buttonPilates).setOnClickListener { viewModel.onPilatesClicked() }
        findViewById<Button>(R.id.buttonFitness).setOnClickListener { viewModel.onFitnessClicked() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                // L'UI génère sa propre navigation ici, c'est elle qui a le Context
                when (event) {
                    is BodyNavigationEvent.NavigateToYoga -> {
                        startActivity(Intent(this@BienCorpsActivity, YogaActivity::class.java))
                    }

                    is BodyNavigationEvent.LaunchVideo -> {
                        val intentVideo = Intent(this@BienCorpsActivity, VideoActivity::class.java)
                        Utilitaires.videoLaunch(
                            event.category,
                            event.isPremium,
                            intentVideo,
                            this@BienCorpsActivity
                        )
                    }
                }
            }
        }
    }
}