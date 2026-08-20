package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.fodmap.FodmapFilter
import com.audreyRetournayDiet.femSante.data.fodmap.FodmapFood
import com.audreyRetournayDiet.femSante.data.fodmap.FodmapStatus
import com.audreyRetournayDiet.femSante.repository.local.FodmapFoodRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Guide FODMAP : recherche d'un aliment + filtre par statut (feu tricolore), 244 fiches issues de
 * `assets/fodmap.json`. Le clic sur une carte déplie la portion-limite et les remarques **dans la
 * carte** (pas d'écran séparé) — usage de recherche répétée plutôt que de lecture longue.
 *
 * `AppCompatActivity` simple (pas de Hilt) : le repository ne dépend que du Context applicatif —
 * même approche que Micronutriments / Ressources.
 */
class FodmapActivity : AppCompatActivity() {

    private val repository by lazy { FodmapFoodRepository(applicationContext) }
    private val adapter = FodmapCardAdapter(::onFoodClicked)

    private var query: String = ""
    private var status: FodmapStatus? = null
    private val expandedIds = mutableSetOf<String>()

    private lateinit var chipGroupStatus: ChipGroup
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fodmap)

        val recycler = findViewById<RecyclerView>(R.id.recyclerFodmap)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        textEmpty = findViewById(R.id.textEmpty)
        chipGroupStatus = findViewById(R.id.chipGroupStatus)
        buildStatusChips()

        findViewById<EditText>(R.id.editSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                query = s?.toString().orEmpty()
                render()
            }
        })

        render()
    }

    /** « Tout » (statut = null) puis un chip par statut, dans l'ordre de l'enum. */
    private fun buildStatusChips() {
        chipGroupStatus.addView(statusChip(getString(R.string.fodmap_status_all), status = null, checked = true))
        FodmapStatus.entries.forEach { s ->
            chipGroupStatus.addView(statusChip(s.label, status = s, checked = false))
        }
        chipGroupStatus.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            status = chip?.tag as? FodmapStatus
            render()
        }
    }

    private fun statusChip(label: String, status: FodmapStatus?, checked: Boolean): Chip {
        val chip = layoutInflater.inflate(R.layout.item_theme_chip, chipGroupStatus, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.tag = status
        chip.isChecked = checked
        return chip
    }

    private fun render() {
        val filtered = FodmapFilter.filter(repository.getAll(), query, status)
        adapter.submitList(filtered.map { FodmapRow(it, expanded = it.id in expandedIds) })
        textEmpty.isVisible = filtered.isEmpty()
    }

    /** Déplie/replie la portion-limite et les remarques de l'aliment cliqué. */
    private fun onFoodClicked(food: FodmapFood) {
        if (!expandedIds.add(food.id)) expandedIds.remove(food.id)
        render()
    }
}
