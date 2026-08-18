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
import com.audreyRetournayDiet.femSante.data.micronutrient.Micronutrient

/**
 * Liste des fiches micronutriments : nom, méta (autre nom · unité), et **cadenas si la fiche est
 * premium ET que l'utilisatrice n'a pas l'accès** ([userHasAccess]).
 */
class MicronutrientCardAdapter(
    private val userHasAccess: Boolean,
    private val onClick: (Micronutrient) -> Unit
) : ListAdapter<Micronutrient, MicronutrientCardAdapter.NutrientViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_micronutrient_card, parent, false)
        return NutrientViewHolder(view)
    }

    override fun onBindViewHolder(holder: NutrientViewHolder, position: Int) = holder.bind(getItem(position))

    inner class NutrientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textName)
        private val meta: TextView = itemView.findViewById(R.id.textMeta)
        private val lock: ImageView = itemView.findViewById(R.id.imageLock)

        fun bind(item: Micronutrient) {
            name.text = item.name
            meta.text = listOfNotNull(item.otherName, item.unit).joinToString(" · ")
            lock.visibility = if (item.premium && !userHasAccess) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onClick(item) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Micronutrient>() {
            override fun areItemsTheSame(oldItem: Micronutrient, newItem: Micronutrient) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Micronutrient, newItem: Micronutrient) = oldItem == newItem
        }
    }
}
