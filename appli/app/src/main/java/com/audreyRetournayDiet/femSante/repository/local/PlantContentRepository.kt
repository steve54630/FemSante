package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.plant.Plant
import com.audreyRetournayDiet.femSante.data.plant.PlantJsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit le lexique des plantes (`assets/plants.json`), lu une seule fois depuis les assets.
 *
 * Injecté par constructeur et unique (`@Singleton`) : même patron que
 * [ToolboxFileRepository]/[MicronutrientContentRepository].
 */
@Singleton
class PlantContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val plantsCache: List<Plant> by lazy { load() }

    /** Toutes les fiches, triées par ordre alphabétique du nom (lexique = navigation par nom). */
    fun getAll(): List<Plant> = plantsCache.sortedBy { it.name.lowercase() }

    /** La fiche correspondant à l'identifiant, ou `null` si absente. */
    fun getById(id: String): Plant? = plantsCache.firstOrNull { it.id == id }

    private fun load(): List<Plant> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        PlantJsonParser.parse(json).also { Timber.i("%d plantes chargées depuis %s", it.size, ASSET_FILE) }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement du lexique des plantes (%s)", ASSET_FILE)
        emptyList()
    }

    private companion object {
        const val ASSET_FILE = "plants.json"
    }
}
