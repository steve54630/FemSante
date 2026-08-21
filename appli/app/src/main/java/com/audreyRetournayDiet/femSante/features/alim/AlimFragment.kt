package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory
import com.audreyRetournayDiet.femSante.data.recipe.RecipeTags
import com.audreyRetournayDiet.femSante.viewmodels.alim.RecipeBrowseViewModel
import com.audreyRetournayDiet.femSante.viewmodels.alim.RecipeBrowseUiState
import com.audreyRetournayDiet.femSante.viewmodels.alim.RecipeDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Écran de navigation des recettes : une grille de cartes photo filtrable en direct par
 * catégorie, par « Ma phase » et par tags diététiques.
 */
@AndroidEntryPoint
class AlimFragment : Fragment() {

    private val viewModel: RecipeBrowseViewModel by viewModels()
    private val adapter = RecipeCardAdapter(::openRecipe)

    private lateinit var chipMaPhase: Chip
    private lateinit var buttonFilters: MaterialButton
    private lateinit var textEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_alim, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRecipes)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        chipMaPhase = view.findViewById(R.id.chipMaPhase)
        buttonFilters = view.findViewById(R.id.buttonFilters)
        textEmpty = view.findViewById(R.id.textEmpty)

        setupCategoryChips(view.findViewById(R.id.chipGroupCategory))
        chipMaPhase.setOnCheckedChangeListener { _, checked -> viewModel.setPhaseOnly(checked) }
        buttonFilters.setOnClickListener { showFiltersSheet() }

        observeState()
    }

    private fun setupCategoryChips(group: ChipGroup) {
        group.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                R.id.chipCatBreakfast -> RecipeCategory.BREAKFAST
                R.id.chipCatEntree -> RecipeCategory.ENTREE
                R.id.chipCatPlat -> RecipeCategory.PLAT
                R.id.chipCatDessert -> RecipeCategory.DESSERT
                else -> null // "Tout"
            }
            viewModel.setCategory(category)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: RecipeBrowseUiState) {
        adapter.submitList(state.recipes)
        textEmpty.isVisible = state.recipes.isEmpty()

        chipMaPhase.isVisible = state.phaseAvailable
        if (chipMaPhase.isChecked != state.phaseOnly) chipMaPhase.isChecked = state.phaseOnly

        buttonFilters.text = if (state.selectedTags.isEmpty()) {
            getString(R.string.recipe_browse_filters)
        } else {
            getString(R.string.recipe_browse_filters_count, state.selectedTags.size)
        }
    }

    /** Ouvre la fiche recette native. */
    private fun openRecipe(recipe: Recipe) {
        Timber.i("Ouverture de la recette : ${recipe.id}")
        val intent = Intent(requireContext(), RecetteDetailActivity::class.java)
            .putExtra(RecipeDetailViewModel.EXTRA_RECIPE_ID, recipe.id)
        startActivity(intent)
    }

    /** Feuille du bas : liste à cocher des tags diététiques, aperçu du nombre de résultats. */
    private fun showFiltersSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_recipe_filters, null)
        val container = sheet.findViewById<LinearLayout>(R.id.containerTags)
        val buttonApply = sheet.findViewById<MaterialButton>(R.id.buttonApplyFilters)
        val buttonReset = sheet.findViewById<MaterialButton>(R.id.buttonResetFilters)

        val pending = viewModel.uiState.value.selectedTags.toMutableSet()
        val checkBoxes = mutableListOf<CheckBox>()

        fun refreshApply() {
            buttonApply.text = getString(R.string.recipe_browse_apply, viewModel.previewCount(pending))
        }

        RecipeTags.ALL.forEach { tag ->
            val checkBox = CheckBox(requireContext()).apply {
                text = tag
                isChecked = tag in pending
                setPadding(0, dp(6), 0, dp(6))
                setOnCheckedChangeListener { _, checked ->
                    if (checked) pending.add(tag) else pending.remove(tag)
                    refreshApply()
                }
            }
            checkBoxes.add(checkBox)
            container.addView(checkBox)
        }
        refreshApply()

        buttonReset.setOnClickListener { checkBoxes.forEach { it.isChecked = false } }
        buttonApply.setOnClickListener {
            viewModel.setSelectedTags(pending.toSet())
            dialog.dismiss()
        }

        dialog.setContentView(sheet)
        dialog.show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
