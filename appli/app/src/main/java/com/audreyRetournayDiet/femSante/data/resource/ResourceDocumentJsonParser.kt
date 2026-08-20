package com.audreyRetournayDiet.femSante.data.resource

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé de `resources.json` → liste de [ResourceDocument].
 *
 * Indépendant du `Context` (travaille sur une chaîne JSON) → testable en JVM sur un fixture, et
 * prêt pour une source serveur. Les entrées incomplètes (pas de `pdf`/`title`) sont **ignorées**
 * plutôt que de faire planter l'écran.
 */
object ResourceDocumentJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<ResourceDocumentDto>>() {}.type

    fun parse(json: String): List<ResourceDocument> {
        val dtos: List<ResourceDocumentDto> = gson.fromJson(json, listType) ?: emptyList()
        return dtos.mapNotNull { it.toDocumentOrNull() }
    }

    private data class ResourceDocumentDto(
        val id: String? = null,
        val pdf: String? = null,
        val title: String? = null
    ) {
        fun toDocumentOrNull(): ResourceDocument? {
            val pdfName = pdf?.takeIf { it.isNotBlank() } ?: return null
            val label = title?.takeIf { it.isNotBlank() } ?: return null
            val stableId = id?.takeIf { it.isNotBlank() } ?: pdfName.substringBeforeLast('.')
            return ResourceDocument(id = stableId, pdf = pdfName, title = label)
        }
    }
}
