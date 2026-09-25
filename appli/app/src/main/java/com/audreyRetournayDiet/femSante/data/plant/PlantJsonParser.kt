package com.audreyRetournayDiet.femSante.data.plant

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

/**
 * Parseur isolé de `plants.json` → liste de [Plant].
 *
 * Indépendant du `Context` (travaille sur une chaîne JSON) → testable en JVM sur un fixture.
 * Même patron défensif que [com.audreyRetournayDiet.femSante.data.toolbox.ToolboxFileJsonParser] :
 * DTO à champs nullables + validation explicite, les entrées incomplètes sont **ignorées** plutôt
 * que de faire planter l'écran ou d'exposer un champ silencieusement faux (piège Gson/Kotlin :
 * un champ manquant du JSON reçoit `false`/`null`, jamais le défaut déclaré côté Kotlin).
 */
object PlantJsonParser {

    private val gson = Gson()
    private val listType = object : TypeToken<List<PlantDto>>() {}.type

    fun parse(json: String): List<Plant> {
        val dtos: List<PlantDto> = runCatching {
            gson.fromJson<List<PlantDto>>(json, listType)
        }.getOrNull() ?: emptyList()

        return dtos.mapNotNull { it.toPlantOrNull() }
    }

    /** Représentation « à plat » d'une fiche (tolérante aux champs manquants). */
    private data class PlantDto(
        val id: String? = null,
        val name: String? = null,
        @SerializedName("latin_name") val latinName: String? = null,
        @SerializedName("main_symptom") val mainSymptom: String? = null,
        @SerializedName("target_symptoms") val targetSymptoms: List<String>? = null,
        @SerializedName("parts_used") val partsUsed: List<String>? = null,
        val properties: List<String>? = null,
        @SerializedName("galenic_forms") val galenicForms: List<String>? = null,
        @SerializedName("max_daily_dose") val maxDailyDose: String? = null,
        val contraindications: List<String>? = null,
        @SerializedName("symptom_categories") val symptomCategories: List<String>? = null
    ) {
        fun toPlantOrNull(): Plant? {
            val validId = id?.takeIf { it.isNotBlank() } ?: return null
            val validName = name?.takeIf { it.isNotBlank() } ?: return null
            val validMainSymptom = mainSymptom?.takeIf { it.isNotBlank() } ?: ""

            return Plant(
                id = validId,
                name = validName,
                latinName = latinName?.takeIf { it.isNotBlank() },
                mainSymptom = validMainSymptom,
                targetSymptoms = targetSymptoms?.filter { it.isNotBlank() } ?: emptyList(),
                partsUsed = partsUsed?.filter { it.isNotBlank() } ?: emptyList(),
                properties = properties?.filter { it.isNotBlank() } ?: emptyList(),
                galenicForms = galenicForms?.filter { it.isNotBlank() } ?: emptyList(),
                maxDailyDose = maxDailyDose?.takeIf { it.isNotBlank() },
                contraindications = contraindications?.filter { it.isNotBlank() } ?: emptyList(),
                // Catégorie inconnue (typo, valeur retirée de l'enum...) ignorée plutôt que de
                // faire planter le parsing de toute la fiche.
                symptomCategories = symptomCategories.orEmpty()
                    .mapNotNull { runCatching { PlantSymptomCategory.valueOf(it) }.getOrNull() }
            )
        }
    }
}
