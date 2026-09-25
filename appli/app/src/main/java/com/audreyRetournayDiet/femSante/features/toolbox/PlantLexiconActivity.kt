package com.audreyRetournayDiet.femSante.features.toolbox

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.plant.Plant
import com.audreyRetournayDiet.femSante.data.plant.PlantFilter
import com.audreyRetournayDiet.femSante.data.plant.PlantSymptomCategory
import com.audreyRetournayDiet.femSante.repository.local.PlantContentRepository
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import timber.log.Timber

/**
 * Lexique des plantes (Boîte à outils / Phytothérapie) : liste alphabétique des fiches,
 * filtrable par recherche de nom et par catégories de symptôme (feuille du bas, même patron que
 * [com.audreyRetournayDiet.femSante.features.alim.AlimFragment] pour les recettes — sélection
 * multiple, application différée). Chaque fiche mène à sa fiche détail native
 * [PlantDetailActivity].
 *
 * `AppCompatActivity` simple (pas de Hilt) : le repository ne dépend que du Context applicatif —
 * même approche que [com.audreyRetournayDiet.femSante.features.ToolboxActivity] et Micronutriments.
 */
class PlantLexiconActivity : AppCompatActivity() {

    private val repository by lazy { PlantContentRepository(applicationContext) }
    private val allPlants by lazy { repository.getAll() }
    private val adapter = PlantCardAdapter(::openPlant)

    private var selectedCategories: Set<PlantSymptomCategory> = emptySet()
    private var searchQuery: String = ""

    private lateinit var buttonFilters: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_lexicon)

        val recycler = findViewById<RecyclerView>(R.id.recyclerPlants)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        val etSearch = findViewById<TextInputEditText>(R.id.etSearch)
        buttonFilters = findViewById(R.id.buttonFilters)

        etSearch.addTextChangedListener {
            searchQuery = it?.toString().orEmpty()
            refreshPlants()
        }
        buttonFilters.setOnClickListener { showFiltersSheet() }

        refreshPlants()
    }

    private fun refreshPlants() {
        adapter.submitList(PlantFilter.filter(allPlants, selectedCategories, searchQuery))
        buttonFilters.text = if (selectedCategories.isEmpty()) {
            getString(R.string.recipe_browse_filters)
        } else {
            getString(R.string.recipe_browse_filters_count, selectedCategories.size)
        }
    }

    /** Feuille du bas : cases à cocher des catégories, aperçu du nombre de résultats. */
    private fun showFiltersSheet() {
        val dialog = BottomSheetDialog(this)
        val sheet = layoutInflater.inflate(R.layout.bottom_sheet_plant_filters, null)
        val container = sheet.findViewById<LinearLayout>(R.id.containerCategories)
        val buttonApply = sheet.findViewById<MaterialButton>(R.id.buttonApplyFilters)
        val buttonReset = sheet.findViewById<MaterialButton>(R.id.buttonResetFilters)

        val pending = selectedCategories.toMutableSet()
        val checkBoxes = mutableListOf<CheckBox>()

        fun refreshApply() {
            val count = PlantFilter.filter(allPlants, pending, searchQuery).size
            buttonApply.text = getString(R.string.recipe_browse_apply, count)
        }

        PlantFilter.categoriesFor(allPlants).forEach { category ->
            val checkBox = CheckBox(this).apply {
                text = category.label
                isChecked = category in pending
                setPadding(0, dp(6), 0, dp(6))
                setOnCheckedChangeListener { _, checked ->
                    if (checked) pending.add(category) else pending.remove(category)
                    refreshApply()
                }
            }
            checkBoxes.add(checkBox)
            container.addView(checkBox)
        }
        refreshApply()

        buttonReset.setOnClickListener { checkBoxes.forEach { it.isChecked = false } }
        buttonApply.setOnClickListener {
            selectedCategories = pending.toSet()
            refreshPlants()
            dialog.dismiss()
        }

        dialog.setContentView(sheet)
        dialog.show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun openPlant(plant: Plant) {
        Timber.i("Lexique des plantes : ouverture de la fiche %s", plant.id)
        startActivity(
            Intent(this, PlantDetailActivity::class.java)
                .putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plant.id)
        )
    }
}
