package com.audreyRetournayDiet.femSante.features

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxCategory
import com.audreyRetournayDiet.femSante.features.toolbox.ToolboxFileDetailActivity
import com.audreyRetournayDiet.femSante.repository.local.ToolboxFileRepository
import timber.log.Timber

/**
 * Boîte à outils : fiches naturopathie présentées en **cartes regroupées par catégorie**
 * (massage, thermothérapie, aromathérapie, phytothérapie).
 *
 * Les fiches proviennent de `assets/toolbox.json` (via [ToolboxFileRepository]). Le clic sur une
 * carte ouvre la **fiche native** [ToolboxFileDetailActivity] (éléments, protocoles, précautions) —
 * plus de PDF.
 *
 * `AppCompatActivity` simple (pas de Hilt) : le repository ne dépend que du Context applicatif —
 * même approche que « Bien dans ta tête/ton corps » et Micronutriments.
 */
class ToolboxActivity : AppCompatActivity() {

    private val repository by lazy { ToolboxFileRepository(applicationContext) }
    private val adapter = ToolboxCardAdapter(::openAdvice)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbox)
        Timber.d("onCreate: Ouverture de la Boîte à Outils")

        val recycler = findViewById<RecyclerView>(R.id.recyclerToolbox)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        adapter.submitList(buildRows(repository.getAll()))
    }

    /** Regroupe les fiches par catégorie (ordre de déclaration de l'enum) en en-têtes + cartes. */
    private fun buildRows(advices: List<ToolboxAdvice>): List<ToolboxRow> = buildList {
        ToolboxCategory.entries.forEach { category ->
            val items = advices.filter { it.category == category }
            if (items.isNotEmpty()) {
                add(ToolboxRow.Header(category))
                items.forEach { add(ToolboxRow.Item(it)) }
            }
        }
    }

    private fun openAdvice(advice: ToolboxAdvice) {
        Timber.i("Boîte à outils : ouverture de la fiche %s", advice.id)
        startActivity(
            Intent(this, ToolboxFileDetailActivity::class.java)
                .putExtra(ToolboxFileDetailActivity.EXTRA_FILE_ID, advice.id)
        )
    }
}
