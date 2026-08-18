package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.micronutrient.DrugInteraction
import com.audreyRetournayDiet.femSante.data.micronutrient.Micronutrient
import com.audreyRetournayDiet.femSante.data.micronutrient.MicronutrientJsonParser
import com.audreyRetournayDiet.femSante.data.micronutrient.NutrientGroup
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit le contenu structuré des micronutriments (fiches natives + interactions médicaments),
 * lu une seule fois depuis les assets.
 *
 * Injecté par constructeur et unique (`@Singleton`) : les JSON ne sont parsés qu'à la première
 * utilisation grâce au chargement paresseux. Même patron que [RecipeContentRepository].
 */
@Singleton
class MicronutrientContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val nutrientsCache: List<Micronutrient> by lazy { load(NUTRIENTS_FILE, MicronutrientJsonParser::parseNutrients) }
    private val interactionsCache: List<DrugInteraction> by lazy { load(INTERACTIONS_FILE, MicronutrientJsonParser::parseInteractions) }

    /** Toutes les fiches. */
    fun getAll(): List<Micronutrient> = nutrientsCache

    /** Les fiches d'un groupe donné (liposolubles, minéraux…). */
    fun getByGroup(group: NutrientGroup): List<Micronutrient> = nutrientsCache.filter { it.group == group }

    /** La fiche correspondant à l'identifiant, ou `null` si absente. */
    fun getById(id: String): Micronutrient? = nutrientsCache.firstOrNull { it.id == id }

    /** Les interactions médicament ↔ nutriment. */
    fun getInteractions(): List<DrugInteraction> = interactionsCache

    private fun <T> load(assetFile: String, parse: (String) -> List<T>): List<T> = try {
        val json = context.assets.open(assetFile).bufferedReader().use { it.readText() }
        parse(json).also { Timber.i("%d éléments chargés depuis %s", it.size, assetFile) }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement (%s)", assetFile)
        emptyList()
    }

    private companion object {
        const val NUTRIENTS_FILE = "micronutrients.json"
        const val INTERACTIONS_FILE = "nutrient_interactions.json"
    }
}
