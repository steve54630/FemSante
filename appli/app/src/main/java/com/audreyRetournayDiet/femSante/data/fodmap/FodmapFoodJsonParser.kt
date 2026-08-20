package com.audreyRetournayDiet.femSante.data.fodmap

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé de `fodmap.json` → liste de [FodmapFood].
 *
 * Indépendant du `Context` (travaille sur une chaîne JSON) → testable en JVM sur un fixture, et
 * prêt pour une source serveur. Les entrées incomplètes ou de catégorie/statut inconnus sont
 * **ignorées** plutôt que de faire planter l'écran.
 */
object FodmapFoodJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<FodmapFoodDto>>() {}.type

    fun parse(json: String): List<FodmapFood> {
        val dtos: List<FodmapFoodDto> = gson.fromJson(json, listType) ?: emptyList()
        return dtos.mapNotNull { it.toFoodOrNull() }
    }

    private data class FodmapFoodDto(
        val id: String? = null,
        val name: String? = null,
        val category: String? = null,
        val status: String? = null,
        val portion: String? = null,
        val notes: String? = null
    ) {
        fun toFoodOrNull(): FodmapFood? {
            val validId = id?.takeIf { it.isNotBlank() } ?: return null
            val validName = name?.takeIf { it.isNotBlank() } ?: return null
            val parsedCategory = category?.let { runCatching { FodmapCategory.valueOf(it) }.getOrNull() } ?: return null
            val parsedStatus = status?.let { runCatching { FodmapStatus.valueOf(it) }.getOrNull() } ?: return null

            return FodmapFood(
                id = validId,
                name = validName,
                category = parsedCategory,
                status = parsedStatus,
                portion = portion?.takeIf { it.isNotBlank() } ?: "",
                notes = notes?.takeIf { it.isNotBlank() } ?: ""
            )
        }
    }
}
