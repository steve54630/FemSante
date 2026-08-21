package com.audreyRetournayDiet.femSante.data.media

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé du catalogue média (`assets/media.json`).
 *
 * Indépendant du `Context`/`AssetManager` (travaille sur une simple chaîne JSON) : le repository
 * lui passe le flux des assets, et les tests unitaires (JVM) valident directement le fichier livré
 * sans émulateur. Même patron que `RecipeJsonParser` / `MicronutrientJsonParser`.
 *
 * Objectif : permettre un contenu piloté par le serveur (il suffira de changer la source dans le repo).
 */
object MediaJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<MediaItem>>() {}.type

    /** Parse le catalogue en liste d'items (liste vide si le contenu est nul). */
    fun parse(json: String): List<MediaItem> = gson.fromJson(json, listType) ?: emptyList()
}
