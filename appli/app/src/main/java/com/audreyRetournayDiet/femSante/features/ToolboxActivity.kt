package com.audreyRetournayDiet.femSante.features

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.PdfNavigationEvent
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.ToolboxViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activité "Boîte à Outils" regroupant divers calculateurs et ressources documentaires.
 *
 * Cette classe implémente un pattern de navigation réactif :
 * 1. L'utilisatrice clique sur un bouton d'outil.
 * 2. La vue informe le [ToolboxViewModel] via l'ID de l'outil.
 * 3. Le ViewModel traite la logique et émet un événement de navigation ([PdfNavigationEvent]).
 * 4. L'activité observe cet événement et lance la [PdfActivity].
 */
class ToolboxActivity : AppCompatActivity() {

    // Injection du ViewModel via le délégué de ktx
    private val viewModel: ToolboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbox)
        Timber.d("onCreate: Ouverture de la Boîte à Outils")

        setupButtons()
        observeNavigation()
    }

    /**
     * Initialise les listeners des boutons en utilisant un mapping ID de ressource / ID métier.
     * Cette approche factorisée permet d'ajouter de nouveaux outils très simplement.
     */
    private fun setupButtons() {
        // Map associant l'ID du bouton graphique à l'ID logique traité par le ViewModel
        val toolMap = mapOf(
            R.id.button1 to 1,
            R.id.buttonHistamine to 2, // Outil spécifique à l'histamine
            R.id.button3 to 3,
            R.id.button4 to 4,
            R.id.button5 to 5,
            R.id.button6 to 6,
            R.id.button7 to 7
        )

        toolMap.forEach { (resId, toolId) ->
            findViewById<Button>(resId).setOnClickListener {
                Timber.i("Action: Clic sur l'outil ID $toolId")
                viewModel.onToolClicked(toolId)
            }
        }
    }

    /**
     * Observe le flux d'événements de navigation provenant du ViewModel.
     * Utilise [repeatOnLifecycle] pour garantir que la collecte ne se fait
     * que lorsque l'activité est au premier plan (State.STARTED).
     */
    private fun observeNavigation() {
        lifecycleScope.launch {
            // Sécurité : évite de collecter des événements si l'app est en arrière-plan
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is PdfNavigationEvent.NavigateToPdf -> {
                            Timber.i("Navigation: Ouverture du PDF -> ${event.fileName}")

                            val intent = Intent(this@ToolboxActivity, PdfActivity::class.java).apply {
                                putExtra("PDF", event.fileName)
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}