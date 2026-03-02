package com.audreyRetournayDiet.femSante.features.corps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.BodyNavigationEvent
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import com.audreyRetournayDiet.femSante.viewModels.body.BodyViewModel
import kotlinx.coroutines.launch

class BienCorpsActivity : AppCompatActivity() {

    private val tag = "ACT_BIEN_CORPS"
    private val viewModel: BodyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_corps)
        Log.d(tag, "onCreate : Initialisation du menu Bien-être & Corps")

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.buttonYoga).setOnClickListener {
            Log.v(tag, "Clic : Sélection Yoga")
            viewModel.onYogaClicked()
        }
        findViewById<Button>(R.id.buttonPilates).setOnClickListener {
            Log.v(tag, "Clic : Sélection Pilates")
            viewModel.onPilatesClicked()
        }
        findViewById<Button>(R.id.buttonFitness).setOnClickListener {
            Log.v(tag, "Clic : Sélection Fitness")
            viewModel.onFitnessClicked()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    Log.i(tag, "Événement de navigation reçu : ${event::class.simpleName}")

                    when (event) {
                        is BodyNavigationEvent.NavigateToYoga -> {
                            Log.d(tag, "Navigation : Lancement de YogaActivity")
                            startActivity(Intent(this@BienCorpsActivity, YogaActivity::class.java))
                        }

                        is BodyNavigationEvent.LaunchVideo -> {
                            Log.d(tag, "Navigation : Lancement Vidéo (Catégorie: ${event.category}, Premium: ${event.isPremium})")
                            val intentVideo = Intent(this@BienCorpsActivity, VideoActivity::class.java)

                            try {
                                Utilitaires.videoLaunch(
                                    event.category,
                                    event.isPremium,
                                    intentVideo,
                                    this@BienCorpsActivity
                                )
                            } catch (e: Exception) {
                                Log.e(tag, "Erreur lors du lancement de la vidéo via Utilitaires", e)
                            }
                        }
                    }
                }
            }
        }
    }
}