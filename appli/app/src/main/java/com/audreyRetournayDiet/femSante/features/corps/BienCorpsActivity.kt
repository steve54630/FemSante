package com.audreyRetournayDiet.femSante.features.corps

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.media.MediaModule
import com.audreyRetournayDiet.femSante.repository.local.MediaContentRepository
import com.audreyRetournayDiet.femSante.data.media.MediaCategory
import com.audreyRetournayDiet.femSante.data.media.MediaFilter
import com.audreyRetournayDiet.femSante.data.media.MediaItem
import com.audreyRetournayDiet.femSante.data.media.MediaType
import com.audreyRetournayDiet.femSante.features.login.PremiumUpsellActivity
import com.audreyRetournayDiet.femSante.shared.MediaCardAdapter
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import timber.log.Timber

/**
 * Écran « Bien dans ton corps » unifié : des chips de thème et une grille de cartes que l'on
 * lance en un tap, pour rester sur du 2 clics max.
 *
 * Le module ne contient que des vidéos : pas de sélecteur Vidéos/Audios (contrairement à
 * « Bien dans ta tête »).
 */
class BienCorpsActivity : AppCompatActivity() {

    private val catalog by lazy { MediaContentRepository(applicationContext).forModule(MediaModule.CORPS) }
    private var category: MediaCategory? = null
    private var hasAccess = false

    private lateinit var adapter: MediaCardAdapter
    private lateinit var chipGroupTheme: ChipGroup
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_corps)

        // Le cadenas premium ne s'affiche qu'aux utilisatrices sans accès. Vérification
        // centralisée (voir UserStore.hasContentAccess) → prête pour le futur statut freemium.
        hasAccess = UserStore(this).hasContentAccess()
        adapter = MediaCardAdapter(hasAccess, ::onMediaClick)

        findViewById<RecyclerView>(R.id.recyclerMedia).apply {
            layoutManager = GridLayoutManager(this@BienCorpsActivity, 2)
            adapter = this@BienCorpsActivity.adapter
        }
        chipGroupTheme = findViewById(R.id.chipGroupTheme)
        textEmpty = findViewById(R.id.textEmpty)

        buildThemeChips()
        refresh()
    }

    /** Construit les chips de thème du corps ; « Tout » sélectionné par défaut. */
    private fun buildThemeChips() {
        chipGroupTheme.setOnCheckedStateChangeListener(null)
        chipGroupTheme.removeAllViews()

        chipGroupTheme.addView(themeChip(getString(R.string.recipe_browse_all), category = null, checked = true))
        MediaFilter.categoriesFor(catalog, MediaType.VIDEO).forEach { cat ->
            chipGroupTheme.addView(themeChip(cat.label, category = cat, checked = false))
        }

        chipGroupTheme.setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) }
            category = chip?.tag as? MediaCategory
            refresh()
        }
    }

    private fun themeChip(label: String, category: MediaCategory?, checked: Boolean): Chip {
        val chip = layoutInflater.inflate(R.layout.item_theme_chip, chipGroupTheme, false) as Chip
        chip.id = View.generateViewId()
        chip.text = label
        chip.tag = category
        chip.isChecked = checked
        return chip
    }

    private fun refresh() {
        val items = MediaFilter.filter(catalog, MediaType.VIDEO, category)
        adapter.submitList(items)
        textEmpty.isVisible = items.isEmpty()
    }

    private fun onMediaClick(item: MediaItem) {
        if (item.premium && !hasAccess) {
            startActivity(Intent(this, PremiumUpsellActivity::class.java))
            return
        }
        Timber.i("Lecture vidéo : ${item.title}")
        val pdfFlag = if (item.pdf) "oui" else "non"
        Utilitaires.videoLaunch(item.title, pdfFlag, Intent(this, VideoActivity::class.java), this)
    }
}
