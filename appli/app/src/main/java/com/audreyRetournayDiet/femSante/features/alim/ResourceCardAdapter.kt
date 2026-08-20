package com.audreyRetournayDiet.femSante.features.alim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R

/**
 * Liste des ressources nutrition : fiches PDF (issues du JSON) + le raccourci fixe vers
 * Micronutriments, en cartes uniformes. Le clic remonte la [ResourceRow] au parent, qui décide
 * de la destination (PDF ou module natif).
 */
class ResourceCardAdapter(
    private val onClick: (ResourceRow) -> Unit
) : ListAdapter<ResourceRow, ResourceCardAdapter.ResourceViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resource_card, parent, false)
        return ResourceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ResourceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.imageResourceIcon)
        private val title: TextView = itemView.findViewById(R.id.textResourceTitle)

        fun bind(row: ResourceRow) {
            when (row) {
                is ResourceRow.Document -> {
                    icon.setImageResource(R.drawable.baseline_pdf)
                    title.text = row.document.title
                }
                ResourceRow.MicronutrientsShortcut -> {
                    icon.setImageResource(R.drawable.baseline_medication_24)
                    title.text = itemView.context.getString(R.string.ebook)
                }
                ResourceRow.FodmapShortcut -> {
                    icon.setImageResource(R.drawable.ic_resource_fodmap)
                    title.text = itemView.context.getString(R.string.fodmap_title)
                }
            }
            itemView.setOnClickListener { onClick(row) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ResourceRow>() {
            override fun areItemsTheSame(oldItem: ResourceRow, newItem: ResourceRow): Boolean = when {
                oldItem is ResourceRow.Document && newItem is ResourceRow.Document -> oldItem.document.id == newItem.document.id
                oldItem is ResourceRow.MicronutrientsShortcut && newItem is ResourceRow.MicronutrientsShortcut -> true
                oldItem is ResourceRow.FodmapShortcut && newItem is ResourceRow.FodmapShortcut -> true
                else -> false
            }

            override fun areContentsTheSame(oldItem: ResourceRow, newItem: ResourceRow): Boolean = oldItem == newItem
        }
    }
}
