package com.audreyRetournayDiet.femSante.features.toolbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.plant.Plant

/** Liste du lexique des plantes : nom, nom latin, symptôme principal. */
class PlantCardAdapter(
    private val onClick: (Plant) -> Unit
) : ListAdapter<Plant, PlantCardAdapter.PlantViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant_card, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) = holder.bind(getItem(position))

    inner class PlantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.textPlantName)
        private val latinName: TextView = itemView.findViewById(R.id.textPlantLatinName)
        private val symptom: TextView = itemView.findViewById(R.id.textPlantSymptom)

        fun bind(plant: Plant) {
            name.text = plant.name
            latinName.isVisible = !plant.latinName.isNullOrBlank()
            latinName.text = plant.latinName
            symptom.text = plant.mainSymptom
            itemView.setOnClickListener { onClick(plant) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Plant>() {
            override fun areItemsTheSame(oldItem: Plant, newItem: Plant) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Plant, newItem: Plant) = oldItem == newItem
        }
    }
}
