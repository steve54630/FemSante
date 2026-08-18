package com.audreyRetournayDiet.femSante.data.micronutrient

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé des contenus micronutriments (`micronutrients.json` et
 * `nutrient_interactions.json`).
 *
 * Indépendant du `Context`/`AssetManager` (travaille sur une simple chaîne JSON) : le repository
 * lui passe le flux des assets, et les tests unitaires (JVM) valident directement les fichiers
 * livrés sans émulateur — comme pour les recettes.
 */
object MicronutrientJsonParser {

    private val gson = Gson()
    private val nutrientListType = object : TypeToken<List<Micronutrient>>() {}.type
    private val interactionListType = object : TypeToken<List<DrugInteraction>>() {}.type

    /** Parse la liste des fiches (liste vide si le contenu est nul). */
    fun parseNutrients(json: String): List<Micronutrient> =
        gson.fromJson(json, nutrientListType) ?: emptyList()

    /** Parse la liste des interactions médicament ↔ nutriment (liste vide si le contenu est nul). */
    fun parseInteractions(json: String): List<DrugInteraction> =
        gson.fromJson(json, interactionListType) ?: emptyList()
}
