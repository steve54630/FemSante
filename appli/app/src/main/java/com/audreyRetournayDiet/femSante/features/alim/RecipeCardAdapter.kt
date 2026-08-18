package com.audreyRetournayDiet.femSante.features.alim

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory

/**
 * Adapter de la grille de recettes (cartes photo). Utilise [ListAdapter] + DiffUtil pour animer
 * proprement les changements de filtre sans reconstruire toute la liste.
 */
class RecipeCardAdapter(
    private val onClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeCardAdapter.RecipeViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_card, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        private val image: ImageView = itemView.findViewById(R.id.imageRecipe)
        private val title: TextView = itemView.findViewById(R.id.textRecipeTitle)
        private val meta: TextView = itemView.findViewById(R.id.textRecipeMeta)

        fun bind(recipe: Recipe) {
            title.text = recipe.title
            meta.text = metaLine(recipe)

            // L'image est un drawable nommé comme l'id de la recette (ex. veloute_epinards_amandes).
            val context = itemView.context
            val resId = context.resources.getIdentifier(recipe.id, "drawable", context.packageName)
            if (resId != 0) image.setImageResource(resId) else image.setImageDrawable(null)

            itemView.setOnClickListener { onClick(recipe) }
        }

        private fun metaLine(recipe: Recipe): String {
            val minutes = (recipe.prepMinutes ?: 0) + (recipe.cookMinutes ?: 0)
            val category = categoryLabel(recipe.category)
            return if (minutes > 0) "$minutes min · $category" else category
        }

        private fun categoryLabel(category: RecipeCategory): String = when (category) {
            RecipeCategory.BREAKFAST -> "Petit-déjeuner"
            RecipeCategory.ENTREE -> "Entrée"
            RecipeCategory.PLAT -> "Plat"
            RecipeCategory.DESSERT -> "Dessert"
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean = oldItem == newItem
        }
    }
}
