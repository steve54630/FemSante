package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class AlimFragment : Fragment() {

    private val tag = "FRAG_ALIM"

    private val viewModel: AlimViewModel by viewModels {
        AlimViewModel.AlimViewModelFactory(RecipeRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(tag, "onCreateView : Création de la vue du fragment")
        return inflater.inflate(R.layout.fragment_alim, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(tag, "onViewCreated : Initialisation des composants")

        setupButton(view, R.id.buttonBreakfirst, "breakfast", "Petit-déjeuner")
        setupButton(view, R.id.buttonEntry, "entries", "Entrées")
        setupButton(view, R.id.buttonPlat, "main_courses", "Plats")
        setupButton(view, R.id.buttonEBook, "desserts", "Desserts")

        observeViewModel()
    }

    private fun setupButton(view: View, buttonId: Int, folder: String, title: String) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            Log.d(tag, "Bouton cliqué : $title (Dossier: $folder)")
            viewModel.onCategorySelected(folder, title, requireContext())
        }
    }

    private fun observeViewModel() {
        // Utilisation de repeatOnLifecycle pour une collecte sécurisée des Flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Collecte de la navigation
                launch {
                    viewModel.navigationEvent.collect { event ->
                        Log.i(tag, "Navigation reçue vers RecetteActivity : ${event.title}")
                        val intent = Intent(activity, RecetteActivity::class.java).apply {
                            putExtra("Title", event.title)
                            putExtra("map", event.recipeMap)
                            putExtra("FOLDER_PATH", event.folderPath)
                        }
                        startActivity(intent)
                    }
                }

                // Collecte des erreurs
                launch {
                    viewModel.errorEvent.collect { errorMessage ->
                        Log.e(tag, "Erreur reçue du ViewModel : $errorMessage")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}