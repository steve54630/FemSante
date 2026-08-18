package com.audreyRetournayDiet.femSante.features.alim

import android.graphics.Paint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recipe.Rayon
import com.audreyRetournayDiet.femSante.data.recipe.ShoppingItem
import com.audreyRetournayDiet.femSante.viewModels.alim.ShoppingListUiState
import com.audreyRetournayDiet.femSante.viewModels.alim.ShoppingListViewModel
import com.audreyRetournayDiet.femSante.viewModels.alim.ShoppingRecipe
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Écran « Ma liste de courses » : les recettes retenues (avec un compteur `− N +`) et les
 * ingrédients agrégés, groupés par rayon et cochables. Tout est reconstruit à chaque changement
 * d'état (petite liste → construction directe des vues, sans RecyclerView).
 */
@AndroidEntryPoint
class ShoppingListFragment : Fragment() {

    private val viewModel: ShoppingListViewModel by viewModels()

    private lateinit var llRecipes: LinearLayout
    private lateinit var llSections: LinearLayout
    private lateinit var tvRecipesCount: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var btnClear: MaterialButton

    private val accent by lazy { requireContext().getColor(R.color.orange_app) }
    private val onSurface = 0xFF222222.toInt()
    private val muted = 0xFF9A9A9A.toInt()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_shopping_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        llRecipes = view.findViewById(R.id.llRecipes)
        llSections = view.findViewById(R.id.llSections)
        tvRecipesCount = view.findViewById(R.id.tvRecipesCount)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        btnClear = view.findViewById(R.id.btnClear)

        btnClear.setOnClickListener { confirmClear() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun render(state: ShoppingListUiState) {
        tvEmpty.isVisible = state.isEmpty
        btnClear.isVisible = !state.isEmpty
        tvRecipesCount.isVisible = !state.isEmpty
        tvRecipesCount.text = getString(R.string.shopping_list_recipes_count, state.recipes.size)

        llRecipes.removeAllViews()
        state.recipes.forEach { llRecipes.addView(recipeRow(it)) }

        // Compteur de progression (articles déjà pris / total).
        val allItems = state.sections.flatMap { it.items }
        val taken = allItems.count { it.name in state.checked }
        tvProgress.isVisible = allItems.isNotEmpty()
        tvProgress.text = getString(R.string.shopping_list_progress, taken, allItems.size)

        llSections.removeAllViews()
        state.sections.forEach { section ->
            llSections.addView(sectionHeader(section.rayon))
            // Les articles cochés descendent en bas de leur rayon (tri stable : l'ordre
            // alphabétique est conservé au sein de chaque groupe).
            section.items
                .sortedBy { it.name in state.checked }
                .forEach { llSections.addView(itemRow(it, it.name in state.checked)) }
        }
    }

    /** Ligne recette : titre + compteur `− N +` (retirée quand on descend à 0). */
    private fun recipeRow(shoppingRecipe: ShoppingRecipe): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val title = TextView(requireContext()).apply {
            text = shoppingRecipe.recipe.title
            textSize = 15f
            setTextColor(onSurface)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val count = TextView(requireContext()).apply {
            text = shoppingRecipe.count.toString()
            textSize = 15f
            gravity = Gravity.CENTER
            minWidth = dp(28)
            setTextColor(onSurface)
        }
        row.addView(title)
        row.addView(stepButton("–") { viewModel.decrement(shoppingRecipe.recipe.id, shoppingRecipe.count) })
        row.addView(count)
        row.addView(stepButton("+") { viewModel.increment(shoppingRecipe.recipe.id, shoppingRecipe.count) })
        return row
    }

    private fun stepButton(label: String, onClick: () -> Unit): TextView = TextView(requireContext()).apply {
        text = label
        textSize = 20f
        gravity = Gravity.CENTER
        setTextColor(accent)
        minWidth = dp(36)
        minHeight = dp(36)
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

    private fun sectionHeader(rayon: Rayon): TextView = TextView(requireContext()).apply {
        text = rayon.label
        textSize = 12f
        isAllCaps = true
        setTextColor(accent)
        setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, dp(14), 0, dp(4))
    }

    /** Ligne ingrédient : case à cocher (barrée si prise) + quantités agrégées. */
    private fun itemRow(item: ShoppingItem, checked: Boolean): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(3), 0, dp(3))
        }
        val checkBox = CheckBox(requireContext()).apply {
            text = item.name
            textSize = 14f
            isChecked = checked
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            applyStrike(this, checked)
            setOnCheckedChangeListener { _, isChecked ->
                applyStrike(this, isChecked)
                viewModel.toggleChecked(item.name, isChecked)
            }
        }
        val quantities = TextView(requireContext()).apply {
            text = item.quantities.joinToString(" · ")
            textSize = 13f
            setTextColor(muted)
        }
        row.addView(checkBox)
        row.addView(quantities)
        return row
    }

    private fun applyStrike(view: TextView, checked: Boolean) {
        view.paintFlags = if (checked) view.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else view.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        view.setTextColor(if (checked) muted else onSurface)
    }

    private fun confirmClear() {
        AlertDialog.Builder(requireContext())
            .setMessage(R.string.shopping_list_clear)
            .setPositiveButton(R.string.shopping_list_clear) { _, _ -> viewModel.clear() }
            .setNegativeButton(R.string.report_cancel, null)
            .show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
