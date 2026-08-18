package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewmodels.alim.RessourceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment gérant la bibliothèque de ressources documentaires (PDF).
 * * Il permet d'accéder à des guides spécifiques (Histamine, Gluten, E-book) via des boutons.
 * La logique de sélection du fichier est gérée par le [RessourceViewModel], et l'affichage
 * est délégué à la [PdfActivity].
 */
@AndroidEntryPoint
class RessourceFragment : Fragment() {

    private val viewModel: RessourceViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ressource, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRessourceButtons(view)
        collectNavigationEvents()
    }

    /**
     * Initialise les listeners des boutons de ressources.
     * @param view La vue racine du fragment.
     */
    private fun setupRessourceButtons(view: View) {
        val buttons = mapOf(
            R.id.buttonHistamine to "histamine",
            R.id.buttonGluten to "gluten"
        )

        buttons.forEach { (resId, key) ->
            view.findViewById<Button>(resId).setOnClickListener {
                Timber.d("Action : Sélection de la ressource '$key'")
                viewModel.onRessourceClicked(key)
            }
        }

        // L'ancien e-book PDF « Les secrets des micronutriments » ouvre désormais le module natif.
        view.findViewById<Button>(R.id.buttonEBook).setOnClickListener {
            Timber.d("Action : Ouverture du module Micronutriments")
            startActivity(Intent(requireContext(), MicronutrientsActivity::class.java))
        }
    }

    /**
     * Écoute le flux de navigation du ViewModel pour ouvrir les documents PDF.
     * Utilise [repeatOnLifecycle] pour garantir que la collecte ne se produit
     * que lorsque l'UI est visible.
     */
    private fun collectNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { pdfName ->
                    Timber.i("Navigation : Lancement du lecteur PDF pour $pdfName")

                    val intentTarget = Intent(requireActivity(), PdfActivity::class.java).apply {
                        putExtra("PDF", pdfName)
                    }

                    try {
                        startActivity(intentTarget)
                    } catch (e: Exception) {
                        Timber.e(e, "Erreur critique : Impossible d'ouvrir le PDF $pdfName")
                    }
                }
            }
        }
    }
}