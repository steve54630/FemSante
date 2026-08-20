package com.audreyRetournayDiet.femSante.features.alim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.fodmap.FodmapFood
import com.audreyRetournayDiet.femSante.data.fodmap.FodmapStatus

/**
 * Liste du guide FODMAP : une pastille couleur **+ le mot du statut en texte** (jamais la couleur
 * seule), nom, catégorie. Le clic déplie/replie la portion-limite et les remarques dans la carte
 * elle-même (pas d'écran détail séparé — usage de recherche répétée, pas de lecture longue).
 */
class FodmapCardAdapter(
    private val onClick: (FodmapFood) -> Unit
) : ListAdapter<FodmapRow, FodmapCardAdapter.FoodViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fodmap_card, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) = holder.bind(getItem(position))

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dot: View = itemView.findViewById(R.id.viewStatusDot)
        private val name: TextView = itemView.findViewById(R.id.textFoodName)
        private val meta: TextView = itemView.findViewById(R.id.textFoodMeta)
        private val expandedContainer: View = itemView.findViewById(R.id.containerExpanded)
        private val portion: TextView = itemView.findViewById(R.id.textFoodPortion)
        private val notes: TextView = itemView.findViewById(R.id.textFoodNotes)

        fun bind(row: FodmapRow) {
            val food = row.food
            dot.setBackgroundResource(food.status.dotRes())
            name.text = food.name
            meta.text = itemView.context.getString(R.string.fodmap_food_meta, food.category.label, food.status.label)

            expandedContainer.isVisible = row.expanded
            if (row.expanded) {
                portion.text = itemView.context.getString(R.string.fodmap_portion_value, food.portion)
                notes.isVisible = food.notes.isNotBlank()
                notes.text = food.notes
            }

            itemView.contentDescription = listOf(food.name, food.status.label, food.category.label)
                .joinToString(", ")
            itemView.setOnClickListener { onClick(food) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<FodmapRow>() {
            override fun areItemsTheSame(oldItem: FodmapRow, newItem: FodmapRow) = oldItem.food.id == newItem.food.id
            override fun areContentsTheSame(oldItem: FodmapRow, newItem: FodmapRow) = oldItem == newItem
        }
    }
}

/** Pastille associée à chaque statut — réutilise les tons pastel déjà validés (calendrier). */
private fun FodmapStatus.dotRes(): Int = when (this) {
    FodmapStatus.PAUVRE -> R.drawable.legend_dot_good
    FodmapStatus.MODERE -> R.drawable.legend_dot_medium
    FodmapStatus.RICHE -> R.drawable.legend_dot_bad
}
