package com.audreyRetournayDiet.femSante.data.entities

data class RecipeUiState(
    val title: String = "",
    val recipeNames: List<String> = emptyList(),
    val imageResourceId: Int = 0, // Nouvel état pour l'image
    val isRecipeSelected: Boolean = false
)