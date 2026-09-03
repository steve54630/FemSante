package com.audreyRetournayDiet.femSante.features.alim

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.micronutrient.Micronutrient
import com.audreyRetournayDiet.femSante.data.micronutrient.NutrientIntake
import com.audreyRetournayDiet.femSante.data.micronutrient.NutrientSource
import com.audreyRetournayDiet.femSante.repository.local.MicronutrientContentRepository
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Fiche détail native d'un micronutriment : en-tête (groupe / nom / badges phase du cycle clé),
 * apports conseillés par profil, sources alimentaires (teneur pour 100 g), cofacteurs
 * d'assimilation et notes.
 *
 * Reçoit l'identifiant via [EXTRA_NUTRIENT_ID]. `AppCompatActivity` simple (repo instancié
 * directement), même approche que [MicronutrientsActivity]. Le gating premium est fait en amont
 * (au clic dans la liste) : cet écran n'est atteint que pour un contenu accessible.
 */
class MicronutrientDetailActivity : AppCompatActivity() {

    private val repository by lazy { MicronutrientContentRepository(applicationContext) }
    private val dinPro: Typeface? by lazy { ResourcesCompat.getFont(this, R.font.dinpro) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val id = intent.getStringExtra(EXTRA_NUTRIENT_ID)
        val nutrient = id?.let { repository.getById(it) }
        if (nutrient == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_micronutrient_detail)
        bindHeader(nutrient)
        bindPhase(nutrient.phase)
        bindIntake(nutrient.intake, nutrient.unit)
        bindSources(nutrient.sources)
        bindOptionalText(R.id.cardCofactors, R.id.tvCofactors, nutrient.cofactors)
        bindOptionalText(R.id.cardNotes, R.id.tvNotes, nutrient.notes)
    }

    private fun bindHeader(nutrient: Micronutrient) {
        findViewById<TextView>(R.id.tvGroup).text = nutrient.group.label
        findViewById<TextView>(R.id.tvName).text = nutrient.name

        val other = findViewById<TextView>(R.id.tvOtherName)
        if (nutrient.otherName.isNullOrBlank()) {
            other.visibility = View.GONE
        } else {
            other.text = nutrient.otherName
        }
    }

    /** Badges « phase du cycle clé » dans l'en-tête — masqués si aucune phase renseignée. */
    private fun bindPhase(phase: List<String>) {
        val chips = findViewById<ChipGroup>(R.id.chipGroupPhase)
        if (phase.isEmpty()) {
            chips.visibility = View.GONE
            return
        }
        phase.forEach { addPhaseChip(chips, it) }
    }

    private fun addPhaseChip(group: ChipGroup, label: String) {
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

    private fun bindIntake(intake: NutrientIntake, unit: String) {
        val container = findViewById<LinearLayout>(R.id.llIntake)
        listOf(
            getString(R.string.micronutrient_intake_women) to intake.women,
            getString(R.string.micronutrient_intake_men) to intake.men,
            getString(R.string.micronutrient_intake_pregnant) to intake.pregnant,
            getString(R.string.micronutrient_intake_breastfeeding) to intake.breastfeeding
        ).forEach { (label, value) ->
            container.addView(twoColumnRow(label, "$value $unit"))
        }
    }

    private fun bindSources(sources: List<NutrientSource>) {
        val container = findViewById<LinearLayout>(R.id.llSources)
        sources.forEach { source ->
            container.addView(twoColumnRow(source.name, source.amount, valueBold = true))
        }
    }

    private fun bindOptionalText(cardId: Int, textId: Int, value: String?) {
        if (value.isNullOrBlank()) return
        findViewById<MaterialCardView>(cardId).visibility = View.VISIBLE
        findViewById<TextView>(textId).text = value
    }

    /** Ligne à deux colonnes : libellé à gauche, valeur à droite. */
    private fun twoColumnRow(label: String, value: String, valueBold: Boolean = false): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(4), 0, dp(4))
        }
        val left = TextView(this).apply {
            text = label
            typeface = dinPro
            textSize = 15f
            setTextColor(0xFF333333.toInt())
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val right = TextView(this).apply {
            text = value
            setTypeface(dinPro, if (valueBold) Typeface.BOLD else Typeface.NORMAL)
            textSize = 15f
            setTextColor(0xFF333333.toInt())
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = dp(12) }
        }
        row.addView(left)
        row.addView(right)
        return row
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_NUTRIENT_ID = "nutrient_id"
    }
}
