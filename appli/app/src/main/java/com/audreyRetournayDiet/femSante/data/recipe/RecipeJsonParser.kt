package com.audreyRetournayDiet.femSante.data.recipe

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé du contenu `recipes.json`.
 *
 * Volontairement indépendant du `Context`/`AssetManager` : il travaille sur une simple chaîne
 * JSON. Cela permet au repository de lui passer le flux des assets côté application, et aux
 * tests unitaires (JVM) de valider directement le fichier livré sans émulateur.
 */
object RecipeJsonParser {

    private val gson = Gson()
    private val recipeListType = object : TypeToken<List<Recipe>>() {}.type

    /** Parse le JSON en liste de recettes (liste vide si le contenu est nul). */
    fun parse(json: String): List<Recipe> = gson.fromJson(json, recipeListType) ?: emptyList()
}
