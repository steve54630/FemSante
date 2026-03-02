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
import com.audreyRetournayDiet.femSante.viewModels.body.YogaViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activité dédiée à la sélection des séances de Yoga.
 *
 * Ce composant permet de choisir entre différents styles de Yoga (Flow, Calme, Débutant).
 * La logique métier (vérification des accès, sélection de la catégorie de vidéo) est
 * déportée dans le [YogaViewModel].
 *
 * L'activité réagit aux [BodyNavigationEvent] pour déclencher l'ouverture du lecteur vidéo
 * via la classe utilitaire [Utilitaires].
 */
class YogaActivity : AppCompatActivity() {

    private val viewModel: YogaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yoga)
        Timber.d("onCreate : Initialisation de l'écran Yoga")

        setupButtons()
        observeNavigation()
    }

    /**
     * Initialise les boutons de sélection de type de Yoga.
     * Chaque clic notifie le ViewModel de l'intention de l'utilisatrice.
     */
    private fun setupButtons() {
        findViewById<Button>(R.id.buttonFlow).setOnClickListener {
            Timber.v("Clic : Yoga Flow")
            viewModel.onFlowClicked()
        }
        findViewById<Button>(R.id.buttonCalm).setOnClickListener {
            Timber.v("Clic : Yoga Calme")
            viewModel.onCalmClicked()
        }
        findViewById<Button>(R.id.buttonBeginner).setOnClickListener {
            Timber.v("Clic : Yoga Débutant")
            viewModel.onBeginnerClicked()
        }
    }

    /**
     * Souscrit aux événements de navigation émis par le ViewModel.
     * Utilise [repeatOnLifecycle] pour éviter la consommation d'événements
     * lorsque l'activité n'est pas visible (économie de ressources et sécurité).
     */
    private fun observeNavigation() {
        lifecycleScope.launch {
            // Sécurisation de la collecte selon le cycle de vie de l'UI
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    handleNavigationEvent(event)
                }
            }
        }
    }

    /**
     * Traite l'événement de navigation reçu.
     * Dans cette activité, seul l'événement [BodyNavigationEvent.LaunchVideo] est attendu.
     *
     * @param event L'événement à traiter.
     */
    private fun handleNavigationEvent(event: BodyNavigationEvent) {
        Timber.i("Événement reçu : ${event::class.simpleName}")

        when (event) {
            is BodyNavigationEvent.LaunchVideo -> {
                Timber.d("Navigation -> Lancement Vidéo Yoga [Catégorie: ${event.category}, Premium: ${event.isPremium}]")

                val intentVideo = Intent(this@YogaActivity, VideoActivity::class.java)
                try {
                    // Délégation de la configuration de l'Intent et du démarrage à Utilitaires
                    Utilitaires.videoLaunch(
                        event.category,
                        event.isPremium,
                        intentVideo,
                        this@YogaActivity
                    )
                } catch (e: Exception) {
                    Timber.e(e, "Erreur lors de l'appel à Utilitaires.videoLaunch")
                }
            }
            else -> {
                // Log de sécurité si un événement non pertinent pour cet écran est reçu
                Timber.w("Événement de navigation non géré dans cette activité : $event")
            }
        }
    }
}