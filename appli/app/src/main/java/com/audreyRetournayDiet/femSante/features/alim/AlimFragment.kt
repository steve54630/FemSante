package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.local.RecipeRepository
import com.audreyRetournayDiet.femSante.viewModels.alim.AlimViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment gérant la sélection des catégories de recettes (Petit-déjeuner, Entrées, etc.).
 * * Ce fragment délègue la logique de chargement des données au [AlimViewModel]
 * et observe les événements de navigation pour lancer la [RecetteActivity].
 */
class AlimFragment : Fragment() {

    private val viewModel: AlimViewModel by viewModels {
        AlimViewModel.AlimViewModelFactory(RecipeRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alim, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialisation des catégories de recettes
        setupButton(view, R.id.buttonBreakfirst, "breakfast", "Petit-déjeuner")
        setupButton(view, R.id.buttonEntry, "entries", "Entrées")
        setupButton(view, R.id.buttonPlat, "main_courses", "Plats")
        setupButton(view, R.id.buttonEBook, "desserts", "Desserts")

        observeViewModel()
    }

    /**
     * Configure un bouton de catégorie et lie son clic au ViewModel.
     * * @param view Vue parente contenant le bouton.
     * @param buttonId ID de la ressource du bouton.
     * @param folder Nom du dossier technique contenant les recettes.
     * @param title Titre affiché à l'utilisateur dans l'écran suivant.
     */
    private fun setupButton(view: View, buttonId: Int, folder: String, title: String) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            Timber.d("Sélection catégorie : $title")
            viewModel.onCategorySelected(folder, title, requireContext())
        }
    }

    /**
     * Observe les flux (Flows) du ViewModel pour la navigation et la gestion des erreurs.
     * Utilise [repeatOnLifecycle] pour garantir une collecte sécurisée durant le cycle de vie.
     */
    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Collecte des événements de navigation vers le détail des recettes
                launch {
                    viewModel.navigationEvent.collect { event ->
                        Timber.i("Navigation : Ouverture de la catégorie ${event.title}")
                        val intent = Intent(activity, RecetteActivity::class.java).apply {
                            putExtra("Title", event.title)
                            putExtra("map", event.recipeMap)
                            putExtra("FOLDER_PATH", event.folderPath)
                        }
                        startActivity(intent)
                    }
                }

                // Collecte et affichage des messages d'erreur via Toast
                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        Timber.e("Erreur ViewModel : $errorMessage")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}