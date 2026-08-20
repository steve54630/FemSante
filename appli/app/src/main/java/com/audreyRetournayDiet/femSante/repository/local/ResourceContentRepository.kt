package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.resource.ResourceDocument
import com.audreyRetournayDiet.femSante.data.resource.ResourceDocumentJsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit les fiches documentaires PDF de l'écran « Ressources » (nutrition), lues une seule fois
 * depuis `assets/resources.json`.
 *
 * Injecté par constructeur et unique (`@Singleton`) : le JSON n'est parsé qu'à la première
 * utilisation (chargement paresseux). Même patron que [RecipeContentRepository] /
 * [ToolboxFileRepository]. Sur échec de lecture → liste vide (l'écran reste ouvrable).
 */
@Singleton
class ResourceContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val documentsCache: List<ResourceDocument> by lazy { load() }

    /** Toutes les fiches, dans l'ordre du JSON. */
    fun getAll(): List<ResourceDocument> = documentsCache

    private fun load(): List<ResourceDocument> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        ResourceDocumentJsonParser.parse(json).also { Timber.i("%d fiches ressources chargées depuis %s", it.size, ASSET_FILE) }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement des ressources (%s)", ASSET_FILE)
        emptyList()
    }

    private companion object {
        const val ASSET_FILE = "resources.json"
    }
}
