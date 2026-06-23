package com.audreyRetournayDiet.femSante.features.corps

import android.content.Intent
import android.os.Bundle
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
import timber.log.Timber

/**
 * Activité principale du module "Bien-être & Corps".
 * * Ce menu permet à l'utilisatrice de naviguer vers les différentes disciplines
 * physiques (Yoga, Pilates, Fitness). La logique de navigation est pilotée par
 * le [BodyViewModel] via un flux d'événements unique ([BodyNavigationEvent]).
 * * ### Fonctionnement :
 * 1. L'utilisatrice clique sur une discipline.
 * 2. Le ViewModel traite la demande (vérification des droits, type de contenu).
 * 3. L'activité reçoit un événement et lance soit une activité spécifique,
 * soit le lecteur vidéo via [Utilitaires].
 */
class BienCorpsActivity : AppCompatActivity() {

    private val viewModel: BodyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_corps)
        Timber.d("onCreate : Initialisation du menu Bien-être & Corps")

        setupButtons()
        observeNavigation()
    }

    /**
     * Initialise les listeners des boutons de l'interface.
     * Les clics sont immédiatement transmis au ViewModel pour traitement.
     */
    private fun setupButtons() {
        findViewById<Button>(R.id.buttonYoga).setOnClickListener {
            Timber.v("Clic : Sélection Yoga")
            viewModel.onYogaClicked()
        }
        findViewById<Button>(R.id.buttonPilates).setOnClickListener {
            Timber.v("Clic : Sélection Pilates")
            viewModel.onPilatesClicked()
        }
        findViewById<Button>(R.id.buttonFitness).setOnClickListener {
            Timber.v("Clic : Sélection Fitness")
            viewModel.onFitnessClicked()
        }
    }

    /**
     * Observe le SharedFlow de navigation du ViewModel.
     * Utilise [repeatOnLifecycle] pour garantir une consommation sécurisée des événements
     * uniquement lorsque l'UI est au premier plan.
     */
    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }

    /**
     * Traite les différents types d'événements de navigation.
     * * @param event L'événement émis par le ViewModel (Yoga ou Lancement Vidéo).
     */
    private fun handleNavigationEvent(event: BodyNavigationEvent) {
        Timber.i("Événement de navigation reçu : ${event::class.simpleName}")

        when (event) {
            is BodyNavigationEvent.NavigateToYoga -> {
                Timber.d("Navigation : Lancement de YogaActivity")
                startActivity(Intent(this, YogaActivity::class.java))
            }

            is BodyNavigationEvent.LaunchVideo -> {
                Timber.d("Navigation : Lancement Vidéo (Catégorie: ${event.category}, Premium: ${event.isPremium})")
                val intentVideo = Intent(this, VideoActivity::class.java)

                try {
                    // Utilisation de la classe utilitaire centralisée pour configurer l'Intent vidéo
                    Utilitaires.videoLaunch(
                        event.category,
                        event.isPremium,
                        intentVideo,
                        this
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Erreur lors du lancement de la vidéo via Utilitaires")
                }
            }
        }
    }
}