package com.audreyRetournayDiet.femSante.features.alim

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.micronutrient.DrugInteraction

/**
 * Liste des interactions médicament ↔ nutriment : le médicament (ou la classe) et les nutriments
 * qu'il appauvrit. Contenu informatif non cliquable.
 */
class InteractionAdapter : ListAdapter<DrugInteraction, InteractionAdapter.InteractionViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InteractionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_interaction, parent, false)
        return InteractionViewHolder(view)
    }

    override fun onBindViewHolder(holder: InteractionViewHolder, position: Int) = holder.bind(getItem(position))

    class InteractionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val drug: TextView = itemView.findViewById(R.id.textDrug)
        private val nutrients: TextView = itemView.findViewById(R.id.textNutrients)

        fun bind(item: DrugInteraction) {
            drug.text = item.drug
            nutrients.text = item.nutrients
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<DrugInteraction>() {
            override fun areItemsTheSame(oldItem: DrugInteraction, newItem: DrugInteraction) = oldItem.drug == newItem.drug
            override fun areContentsTheSame(oldItem: DrugInteraction, newItem: DrugInteraction) = oldItem == newItem
        }
    }
}
