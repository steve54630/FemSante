package com.audreyRetournayDiet.femSante.features.toolbox

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.plant.Plant
import com.audreyRetournayDiet.femSante.repository.local.PlantContentRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Fiche détail native d'une plante du lexique : symptôme principal, symptômes ciblés, partie
 * utilisée, propriétés, formes galéniques, dose maximale par jour et contre-indications.
 *
 * Reçoit l'identifiant via [EXTRA_PLANT_ID]. `AppCompatActivity` simple (repo instancié
 * directement), même approche que [com.audreyRetournayDiet.femSante.features.alim.MicronutrientDetailActivity].
 */
class PlantDetailActivity : AppCompatActivity() {

    private val repository by lazy { PlantContentRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(EXTRA_PLANT_ID)
        val plant = id?.let { repository.getById(it) }
        if (plant == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_plant_detail)
        bindHeader(plant)
        bindChipSection(R.id.cardTargetSymptoms, R.id.chipGroupTargetSymptoms, plant.targetSymptoms)
        bindChipSection(R.id.cardPartsUsed, R.id.chipGroupPartsUsed, plant.partsUsed)
        bindChipSection(R.id.cardProperties, R.id.chipGroupProperties, plant.properties)
        bindChipSection(R.id.cardGalenicForms, R.id.chipGroupGalenicForms, plant.galenicForms)
        bindMaxDose(plant.maxDailyDose)
        bindContraindications(plant.contraindications)
    }

    private fun bindHeader(plant: Plant) {
        findViewById<TextView>(R.id.tvName).text = plant.name

        val latinName = findViewById<TextView>(R.id.tvLatinName)
        if (plant.latinName.isNullOrBlank()) {
            latinName.visibility = View.GONE
        } else {
            latinName.text = plant.latinName
        }

        findViewById<TextView>(R.id.tvMainSymptom).text = plant.mainSymptom
    }

    /** Section à puces (chips) — la carte entière est masquée si la liste est vide. */
    private fun bindChipSection(cardId: Int, chipGroupId: Int, values: List<String>) {
        val card = findViewById<View>(cardId)
        if (values.isEmpty()) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE

        val chipGroup = findViewById<ChipGroup>(chipGroupId)
        chipGroup.removeAllViews()
        values.forEach { addChip(chipGroup, it) }
    }

    private fun addChip(group: ChipGroup, label: String) {
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

    private fun bindMaxDose(maxDailyDose: String?) {
        val card = findViewById<View>(R.id.cardMaxDose)
        if (maxDailyDose.isNullOrBlank()) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvMaxDose).text = maxDailyDose
    }

    private fun bindContraindications(contraindications: List<String>) {
        val card = findViewById<View>(R.id.cardContraindications)
        val container = findViewById<LinearLayout>(R.id.llContraindications)
        container.removeAllViews()

        if (contraindications.isEmpty()) {
            card.visibility = View.GONE
            return
        }
        card.visibility = View.VISIBLE
        contraindications.forEach { contraindication ->
            val textView = TextView(this).apply {
                text = "• $contraindication"
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                setPadding(0, 4, 0, 4)
            }
            container.addView(textView)
        }
    }

    companion object {
        const val EXTRA_PLANT_ID = "EXTRA_PLANT_ID"
    }
}
