package com.audreyRetournayDiet.femSante.features.alim

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory
import com.audreyRetournayDiet.femSante.data.recipe.RecipeIngredient
import com.audreyRetournayDiet.femSante.viewmodels.alim.RecipeDetailViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fiche recette **native**, à la charte de l'app (par opposition au PDF figé).
 *
 * Reçoit l'identifiant de la recette via [RecipeDetailViewModel.EXTRA_RECIPE_ID]. Le contenu est
 * affiché directement dans l'app — plus de lien vers le PDF d'origine.
 */
@AndroidEntryPoint
class RecetteDetailActivity : AppCompatActivity() {

    private val viewModel: RecipeDetailViewModel by viewModels()

    private val dinPro: Typeface? by lazy { ResourcesCompat.getFont(this, R.font.dinpro) }
    private val accentColor: Int by lazy { ContextCompat.getColor(this, R.color.orange_app) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recipe = viewModel.recipe
        if (recipe == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_recette_detail)
        bindHeader(recipe)
        bindAddButton()
        bindIngredients(recipe.ingredients)
        bindSteps(recipe.steps)
        bindTip(recipe.nutritionTip)
    }

    /** Bouton « Ajouter à ma liste » : reflète l'appartenance à la liste et la bascule. */
    private fun bindAddButton() {
        val button = findViewById<MaterialButton>(R.id.btnAddToList)
        button.setOnClickListener {
            viewModel.toggleInShoppingList { added ->
                if (added) Toast.makeText(this, R.string.recipe_added_to_list, Toast.LENGTH_SHORT).show()
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.inShoppingList.collect { inList ->
                    button.setText(if (inList) R.string.recipe_remove_from_list else R.string.recipe_add_to_list)
                    button.setIconResource(
                        if (inList) android.R.drawable.ic_menu_close_clear_cancel else android.R.drawable.ic_input_add
                    )
                }
            }
        }
    }

    private fun bindHeader(recipe: Recipe) {
        findViewById<TextView>(R.id.tvCategory).text = categoryLabel(recipe.category)
        findViewById<TextView>(R.id.tvTitle).text = recipe.title

        val chips = findViewById<ChipGroup>(R.id.chipGroupMeta)
        recipe.prepMinutes?.let { addMetaChip(chips, getString(R.string.recipe_meta_prep, it)) }
        recipe.cookMinutes?.let {
            val label = if (it <= 0) getString(R.string.recipe_meta_cook_none)
            else getString(R.string.recipe_meta_cook, it)
            addMetaChip(chips, label)
        }
        recipe.servings?.takeIf { it.isNotBlank() }?.let { addMetaChip(chips, it) }
    }

    private fun bindIngredients(ingredients: List<RecipeIngredient>) {
        val container = findViewById<LinearLayout>(R.id.llIngredients)
        var currentSection: String? = null
        ingredients.forEach { ingredient ->
            if (ingredient.section != null && ingredient.section != currentSection) {
                currentSection = ingredient.section
                container.addView(sectionHeader(ingredient.section))
            }
            container.addView(ingredientRow(ingredient))
        }
    }

    private fun bindSteps(steps: List<String>) {
        val container = findViewById<LinearLayout>(R.id.llSteps)
        steps.forEachIndexed { index, step ->
            container.addView(stepRow(index + 1, step))
        }
    }

    private fun bindTip(tip: String?) {
        if (tip.isNullOrBlank()) return
        findViewById<MaterialCardView>(R.id.cardTip).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvTip).text = tip
    }

    // --- Construction des vues dynamiques -----------------------------------------------------

    private fun sectionHeader(title: String): TextView = TextView(this).apply {
        text = title
        setTypeface(dinPro, Typeface.BOLD)
        setTextColor(accentColor)
        textSize = 13f
        setPadding(0, dp(10), 0, dp(2))
    }

    private fun ingredientRow(ingredient: RecipeIngredient): TextView {
        val quantityUnit = listOfNotNull(
            ingredient.quantity?.takeIf { it.isNotBlank() },
            ingredient.unit?.takeIf { it.isNotBlank() }
        ).joinToString(" ")

        val builder = SpannableStringBuilder("•  ")
        val mainStart = builder.length
        if (quantityUnit.isNotBlank()) {
            builder.append(quantityUnit)
            builder.setSpan(StyleSpan(Typeface.BOLD), mainStart, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(" ")
        }
        builder.append(ingredient.name)

        ingredient.note?.takeIf { it.isNotBlank() }?.let { note ->
            val noteStart = builder.length
            builder.append("  ").append(note)
            builder.setSpan(ForegroundColorSpan(0xFF8A8A8A.toInt()), noteStart, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(StyleSpan(Typeface.ITALIC), noteStart, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(RelativeSizeSpan(0.9f), noteStart, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return TextView(this).apply {
            text = builder
            typeface = dinPro
            textSize = 15f
            setTextColor(0xFF333333.toInt())
            setLineSpacing(dp(2).toFloat(), 1f)
            setPadding(0, dp(3), 0, dp(3))
        }
    }

    private fun stepRow(number: Int, step: String): TextView {
        val builder = SpannableStringBuilder("$number.  ")
        builder.setSpan(StyleSpan(Typeface.BOLD), 0, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.setSpan(ForegroundColorSpan(accentColor), 0, builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(step)

        return TextView(this).apply {
            text = builder
            typeface = dinPro
            textSize = 15f
            setTextColor(0xFF333333.toInt())
            setLineSpacing(dp(3).toFloat(), 1f)
            setPadding(0, dp(5), 0, dp(5))
        }
    }

    private fun addMetaChip(group: ChipGroup, label: String) {
        val chip = Chip(this).apply {
            text = label
            isClickable = false
            isCheckable = false
            isCloseIconVisible = false
            setEnsureMinTouchTargetSize(false)
            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            setTextColor(0xFF333333.toInt())
        }
        group.addView(chip)
    }

    private fun categoryLabel(category: RecipeCategory): String = when (category) {
        RecipeCategory.BREAKFAST -> "Petit-déjeuner"
        RecipeCategory.ENTREE -> "Entrée"
        RecipeCategory.PLAT -> "Plat"
        RecipeCategory.DESSERT -> "Dessert"
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
