package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.micronutrient.Micronutrient
import com.audreyRetournayDiet.femSante.data.micronutrient.MicronutrientFilter
import com.audreyRetournayDiet.femSante.data.micronutrient.NutrientGroup
import com.audreyRetournayDiet.femSante.repository.local.MicronutrientContentRepository
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import timber.log.Timber

/**
 * Module « Micronutriments » (remplace l'ancien e-book PDF « Les secrets des micronutriments »).
 *
 * Un sélecteur **Fiches / Interactions** : les fiches sont filtrables par groupe (chips) et
 * mènent à une fiche détail native ; les interactions listent les couples médicament ↔ nutriments
 * appauvris. Contenu **premium** (cadenas via [UserStore.hasContentAccess]).
 *
 * `AppCompatActivity` simple (pas de Hilt) : le repository ne dépend que du Context applicatif,
 * on l'instancie directement — même approche que les écrans « Bien dans ta tête/ton corps ».
 */
class MicronutrientsActivity : AppCompatActivity() {

    private val repository by lazy { MicronutrientContentRepository(applicationContext) }
    private val catalog by lazy { repository.getAll() }

    private var group: NutrientGroup? = null
    private var hasAccess = false

    private lateinit var nutrientAdapter: MicronutrientCardAdapter
    private lateinit var interactionAdapter: InteractionAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var groupChipsScroll: View
    private lateinit var chipGroupNutrient: ChipGroup
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_micronutrients)

        hasAccess = UserStore(this).hasContentAccess()
        nutrientAdapter = MicronutrientCardAdapter(hasAccess, ::onNutrientClick)
        interactionAdapter = InteractionAdapter()

        recycler = findViewById(R.id.recyclerMicro)
        recycler.layoutManager = LinearLayoutManager(this)
        groupChipsScroll = findViewById(R.id.groupChipsScroll)
        chipGroupNutrient = findViewById(R.id.chipGroupNutrient)
        textEmpty = findViewById(R.id.textEmpty)

        val toggle = findViewById<MaterialButtonToggleGroup>(R.id.toggleMode)
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnInteractions) showInteractions() else showFiches()
            }
        }
        // Sélection initiale : Fiches (déclenche le premier affichage via le listener).
        toggle.check(R.id.btnFiches)
    }

    private fun showFiches() {
        groupChipsScroll.isVisible = true
        recycler.adapter = nutrientAdapter
        buildGroupChips()
        refreshNutrients()
    }

    private fun showInteractions() {
        groupChipsScroll.isVisible = false
        recycler.adapter = interactionAdapter
        interactionAdapter.submitList(repository.getInteractions())
        textEmpty.isVisible = false
    }

    /** (Re)construit les chips de groupe ; « Tout » sélectionné par défaut. */
    private fun buildGroupChips() {
        chipGroupNutrient.setOnCheckedStateChangeListener(null)
        chipGroupNutrient.removeAllViews()

        chipGroupNutrient.addView(groupChip(getString(R.string.recipe_browse_all), group = null, checked = true))
        MicronutrientFilter.groupsFor(catalog).forEach { g ->
            chipGroupNutrient.addView(groupChip(g.label, group = g, checked = false))
        }

        chipGroupNutrient.setOnCheckedStateChangeListener { chipGroup, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { chipGroup.findViewById<Chip>(it) }
            group = chip?.tag as? NutrientGroup
            refreshNutrients()
        }
    }

    private fun groupChip(label: String, group: NutrientGroup?, checked: Boolean): Chip {
        val chip = layoutInflater.inflate(R.layout.item_theme_chip, chipGroupNutrient, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.tag = group
        chip.isChecked = checked
        return chip
    }

    private fun refreshNutrients() {
        val items = MicronutrientFilter.filter(catalog, group)
        nutrientAdapter.submitList(items)
        textEmpty.isVisible = items.isEmpty()
    }

    private fun onNutrientClick(item: Micronutrient) {
        // Contenu local : le gating premium se fait ici (au clic), avant d'ouvrir la fiche.
        if (item.premium && !hasAccess) {
            Toast.makeText(this, R.string.micronutrient_locked, Toast.LENGTH_SHORT).show()
            return
        }
        Timber.i("Ouverture de la fiche micronutriment : ${item.id}")
        startActivity(
            Intent(this, MicronutrientDetailActivity::class.java)
                .putExtra(MicronutrientDetailActivity.EXTRA_NUTRIENT_ID, item.id)
        )
    }
}
