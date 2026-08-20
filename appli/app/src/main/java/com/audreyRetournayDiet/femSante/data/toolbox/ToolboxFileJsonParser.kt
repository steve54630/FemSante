package com.audreyRetournayDiet.femSante.data.toolbox

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé de `toolbox.json` → liste de [ToolboxAdvice].
 *
 * Indépendant du `Context` (travaille sur une chaîne JSON) → testable en JVM sur un fixture, et
 * prêt pour une source serveur. Les entrées incomplètes ou au format invalide sont **ignorées**
 * plutôt que de faire planter l'écran.
 */
object ToolboxFileJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<NaturoAdviceDto>>() {}.type

    fun parse(json: String): List<ToolboxAdvice> {
        val dtos: List<NaturoAdviceDto> = runCatching {
            gson.fromJson<List<NaturoAdviceDto>>(json, listType)
        }.getOrNull() ?: emptyList()

        return dtos.mapNotNull { it.toAdviceOrNull() }
    }

    /** Représentation « à plat » d'une fiche (tolérante aux champs manquants). */
    private data class NaturoAdviceDto(
        val id: String? = null,
        val title: String? = null,
        val category: String? = null,
        val author: AuthorDto? = null,
        val summary: String? = null,
        val items: List<ItemDto>? = null,
        val protocols: List<ProtocolDto>? = null,
        val precautions: List<String>? = null
    ) {
        fun toAdviceOrNull(): ToolboxAdvice? {
            val validId = id?.takeIf { it.isNotBlank() } ?: return null
            val validTitle = title?.takeIf { it.isNotBlank() } ?: return null
            val parsedCategory = category?.let { runCatching { ToolboxCategory.valueOf(it) }.getOrNull() } ?: return null
            val validAuthor = author?.toAuthorOrNull() ?: return null
            val validSummary = summary?.takeIf { it.isNotBlank() } ?: ""

            return ToolboxAdvice(
                id = validId,
                title = validTitle,
                category = parsedCategory,
                author = validAuthor,
                summary = validSummary,
                items = items?.mapNotNull { it.toItemOrNull() } ?: emptyList(),
                protocols = protocols?.mapNotNull { it.toProtocolOrNull() } ?: emptyList(),
                precautions = precautions?.filter { it.isNotBlank() } ?: emptyList()
            )
        }
    }

    private data class AuthorDto(
        val name: String? = null,
        val role: String? = null
    ) {
        fun toAuthorOrNull(): Author? {
            val validName = name?.takeIf { it.isNotBlank() } ?: return null
            val validRole = role?.takeIf { it.isNotBlank() } ?: ""
            return Author(name = validName, role = validRole)
        }
    }

    private data class ItemDto(
        val name: String? = null,
        @SerializedName("latin_name") val latinName: String? = null,
        val properties: List<String>? = null,
        val contraindications: List<String>? = null
    ) {
        fun toItemOrNull(): ToolboxItem? {
            val validName = name?.takeIf { it.isNotBlank() } ?: return null
            return ToolboxItem(
                name = validName,
                latinName = latinName?.takeIf { it.isNotBlank() },
                properties = properties?.filter { it.isNotBlank() } ?: emptyList(),
                contraindications = contraindications?.filter { it.isNotBlank() } ?: emptyList()
            )
        }
    }

    private data class ProtocolDto(
        val indication: String? = null,
        @SerializedName("application_site") val applicationSite: String? = null,
        val steps: List<String>? = null
    ) {
        fun toProtocolOrNull(): ToolboxProtocol? {
            val validIndication = indication?.takeIf { it.isNotBlank() } ?: return null
            val validSite = applicationSite?.takeIf { it.isNotBlank() } ?: ""
            val validSteps = steps?.filter { it.isNotBlank() } ?: emptyList()

            return ToolboxProtocol(
                indication = validIndication,
                applicationSite = validSite,
                steps = validSteps
            )
        }
    }
}