package com.audreyRetournayDiet.femSante.data.entities

data class RecipeNavigationEvent(
    val title: String,
    val recipeMap: HashMap<String, String>,
    val folderPath: String
)