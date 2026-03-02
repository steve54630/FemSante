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
import com.audreyRetournayDiet.femSante.viewModels.body.YogaViewModel
import kotlinx.coroutines.launch

class YogaActivity : AppCompatActivity() {

    private val tag = "ACT_YOGA"
    private val viewModel: YogaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)
        Log.d(tag, "onCreate : Initialisation de l'écran Yoga")

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.buttonFlow).setOnClickListener {
            Log.v(tag, "Clic : Yoga Flow")
            viewModel.onFlowClicked()
        }
        findViewById<Button>(R.id.buttonCalm).setOnClickListener {
            Log.v(tag, "Clic : Yoga Calme")
            viewModel.onCalmClicked()
        }
        findViewById<Button>(R.id.buttonBeginner).setOnClickListener {
            Log.v(tag, "Clic : Yoga Débutant")
            viewModel.onBeginnerClicked()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Sécurisation de la collecte selon le cycle de vie
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    Log.i(tag, "Événement reçu : ${event::class.simpleName}")

                    when (event) {
                        is BodyNavigationEvent.LaunchVideo -> {
                            Log.d(tag, "Navigation -> Lancement Vidéo Yoga [Catégorie: ${event.category}, Premium: ${event.isPremium}]")

                            val intentVideo = Intent(this@YogaActivity, VideoActivity::class.java)
                            try {
                                Utilitaires.videoLaunch(
                                    event.category,
                                    event.isPremium,
                                    intentVideo,
                                    this@YogaActivity
                                )
                            } catch (e: Exception) {
                                Log.e(tag, "Erreur lors de l'appel à Utilitaires.videoLaunch", e)
                            }
                        }
                        else -> {
                            Log.w(tag, "Événement de navigation non géré dans cette activité : $event")
                        }
                    }
                }
            }
        }
    }
}