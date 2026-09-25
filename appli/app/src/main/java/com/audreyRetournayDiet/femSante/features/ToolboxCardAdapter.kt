package com.audreyRetournayDiet.femSante.features

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxCategory

/**
 * Liste de la Boîte à outils : des **en-têtes de section** (par catégorie), des **cartes de
 * fiche** (titre + résumé) et un **lien vers le lexique des plantes** (section Phytothérapie), au
 * sein d'un même RecyclerView. Le clic sur une carte remonte la [ToolboxAdvice] au parent pour
 * ouvrir la fiche native ; le lien lexique remonte [onLexiconClick].
 */
class ToolboxCardAdapter(
    private val onClick: (ToolboxAdvice) -> Unit,
    private val onLexiconClick: () -> Unit
) : ListAdapter<ToolboxRow, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is ToolboxRow.Header -> TYPE_HEADER
        is ToolboxRow.Item -> TYPE_ITEM
        is ToolboxRow.PlantLexiconLink -> TYPE_LEXICON_LINK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(inflater.inflate(R.layout.item_toolbox_header, parent, false))
            TYPE_LEXICON_LINK -> LexiconLinkViewHolder(inflater.inflate(R.layout.item_toolbox_tool, parent, false))
            else -> ToolViewHolder(inflater.inflate(R.layout.item_toolbox_tool, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is ToolboxRow.Header -> (holder as HeaderViewHolder).bind(row.category)
            is ToolboxRow.Item -> (holder as ToolViewHolder).bind(row.advice)
            is ToolboxRow.PlantLexiconLink -> (holder as LexiconLinkViewHolder).bind()
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.imageThemeIcon)
        private val label: TextView = view.findViewById(R.id.textThemeLabel)

        fun bind(category: ToolboxCategory) {
            label.text = category.label
            icon.setImageResource(category.iconRes())
        }
    }

    inner class ToolViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.textToolTitle)
        private val description: TextView = view.findViewById(R.id.textToolDescription)

        fun bind(advice: ToolboxAdvice) {
            title.text = advice.title
            description.text = advice.summary
            description.isVisible = advice.summary.isNotBlank()
            // Lecteur d'écran : titre + résumé en une phrase (la carte entière est cliquable).
            itemView.contentDescription = listOf(advice.title, advice.summary)
                .filter { it.isNotBlank() }
                .joinToString(". ")
            itemView.setOnClickListener { onClick(advice) }
        }
    }

    /** Carte fixe menant au lexique des plantes — réutilise le layout de carte fiche. */
    inner class LexiconLinkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.textToolTitle)
        private val description: TextView = view.findViewById(R.id.textToolDescription)

        fun bind() {
            title.setText(R.string.plant_lexicon_link_title)
            description.setText(R.string.plant_lexicon_link_description)
            description.isVisible = true
            itemView.setOnClickListener { onLexiconClick() }
        }
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        const val TYPE_LEXICON_LINK = 2

        val DIFF = object : DiffUtil.ItemCallback<ToolboxRow>() {
            override fun areItemsTheSame(oldItem: ToolboxRow, newItem: ToolboxRow): Boolean = when {
                oldItem is ToolboxRow.Header && newItem is ToolboxRow.Header -> oldItem.category == newItem.category
                oldItem is ToolboxRow.Item && newItem is ToolboxRow.Item -> oldItem.advice.id == newItem.advice.id
                oldItem is ToolboxRow.PlantLexiconLink && newItem is ToolboxRow.PlantLexiconLink -> true
                else -> false
            }

            override fun areContentsTheSame(oldItem: ToolboxRow, newItem: ToolboxRow): Boolean = oldItem == newItem
        }
    }
}

/** Icône associée à chaque catégorie (affichée dans l'en-tête de section). */
private fun ToolboxCategory.iconRes(): Int = when (this) {
    ToolboxCategory.MASSAGE -> R.drawable.ic_toolbox_massage
    ToolboxCategory.THERMOTHERAPY -> R.drawable.ic_toolbox_thermo
    ToolboxCategory.AROMATHERAPY -> R.drawable.ic_toolbox_aroma
    ToolboxCategory.PHYTOTHERAPY -> R.drawable.ic_toolbox_phyto
}
