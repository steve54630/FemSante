package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.repository.local.RecipeRepository
import com.audreyRetournayDiet.femSante.viewModels.alim.AlimViewModel
import kotlinx.coroutines.launch

class AlimFragment : Fragment() {

    private val viewModel: AlimViewModel by viewModels {
        AlimViewModel.AlimViewModelFactory(RecipeRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_alim, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButton(view, R.id.buttonBreakfirst, "breakfast", "Petit-déjeuner")
        setupButton(view, R.id.buttonEntry, "starters", "Entrées")
        setupButton(view, R.id.buttonPlat, "main_courses", "Plats")
        setupButton(view, R.id.buttonEBook, "desserts", "Desserts")

        observeNavigation()
    }

    private fun setupButton(view: View, buttonId: Int, folder: String, title: String) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            viewModel.onCategorySelected(folder, title, requireContext())
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                val intent = Intent(activity, RecetteActivity::class.java).apply {
                    putExtra("Title", event.title)
                    putExtra("map", event.recipeMap)
                    putExtra("FOLDER_PATH", event.folderPath)
                }
                startActivity(intent)
            }
        }
    }
}