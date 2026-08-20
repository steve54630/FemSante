package com.audreyRetournayDiet.femSante.features.tete

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
import com.audreyRetournayDiet.femSante.shared.MediaCardAdapter
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.shared.Utilitaires
import com.audreyRetournayDiet.femSante.shared.viewers.AudioActivity
import com.audreyRetournayDiet.femSante.shared.viewers.VideoActivity
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import timber.log.Timber

/**
 * Écran « Bien dans ta tête » unifié : un sélecteur **Vidéos / Audios**, des chips de thème (qui
 * dépendent du type choisi), et une **grille de cartes** que l'on lance en un tap. Remplace
 * l'ancien menu de boutons + les sous-écrans Art-thérapie / Sophrologie, et supprime le spinner
 * audio.
 */
class BienTeteActivity : AppCompatActivity() {

    private val catalog by lazy { MediaContentRepository(applicationContext).forModule(MediaModule.TETE) }
    private var type = MediaType.VIDEO
    private var category: MediaCategory? = null

    private lateinit var adapter: MediaCardAdapter
    private lateinit var chipGroupTheme: ChipGroup
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bien_tete)

        // Le cadenas premium ne s'affiche qu'aux utilisatrices sans accès. Vérification
        // centralisée (voir UserStore.hasContentAccess) → prête pour le futur statut freemium.
        val hasAccess = UserStore(this).hasContentAccess()
        adapter = MediaCardAdapter(hasAccess, ::onMediaClick)

        findViewById<RecyclerView>(R.id.recyclerMedia).apply {
            layoutManager = GridLayoutManager(this@BienTeteActivity, 2)
            adapter = this@BienTeteActivity.adapter
        }
        chipGroupTheme = findViewById(R.id.chipGroupTheme)
        textEmpty = findViewById(R.id.textEmpty)

        val toggle = findViewById<MaterialButtonToggleGroup>(R.id.toggleType)
        toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                type = if (checkedId == R.id.btnAudios) MediaType.AUDIO else MediaType.VIDEO
                category = null
                buildThemeChips()
                refresh()
            }
        }
        // Sélection initiale : Vidéos (déclenche le premier affichage via le listener).
        toggle.check(R.id.btnVideos)
    }

    /** (Re)construit les chips de thème selon le type courant ; « Tout » sélectionné par défaut. */
    private fun buildThemeChips() {
        chipGroupTheme.setOnCheckedStateChangeListener(null)
        chipGroupTheme.removeAllViews()

        chipGroupTheme.addView(themeChip(getString(R.string.recipe_browse_all), category = null, checked = true))
        MediaFilter.categoriesFor(catalog, type).forEach { cat ->
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
        val items = MediaFilter.filter(catalog, type, category)
        adapter.submitList(items)
        textEmpty.isVisible = items.isEmpty()
    }

    private fun onMediaClick(item: MediaItem) {
        when (item.type) {
            MediaType.VIDEO -> {
                Timber.i("Lecture vidéo : ${item.title}")
                val pdfFlag = if (item.pdf) "oui" else "non"
                Utilitaires.videoLaunch(item.title, pdfFlag, Intent(this, VideoActivity::class.java), this)
            }
            MediaType.AUDIO -> {
                Timber.i("Lecture audio : ${item.title}")
                startActivity(
                    Intent(this, AudioActivity::class.java)
                        .putExtra(AudioActivity.EXTRA_TITLE, item.title)
                        .putExtra(AudioActivity.EXTRA_TRACK, item.title)
                )
            }
        }
    }
}
