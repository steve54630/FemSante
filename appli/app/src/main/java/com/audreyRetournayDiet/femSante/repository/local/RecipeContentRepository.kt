package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.recipe.Recipe
import com.audreyRetournayDiet.femSante.data.recipe.RecipeCategory
import com.audreyRetournayDiet.femSante.data.recipe.RecipeJsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit le contenu structuré des recettes (fiches natives), lu une seule fois depuis
 * `assets/recipes.json`.
 *
 * À la différence de [RecipeRepository] (qui liste les fichiers PDF d'un dossier), ce
 * repository expose des objets [Recipe] exploitables par l'UI. L'[Recipe.id] correspond au
 * nom de fichier PDF sans extension : c'est la clé de jointure avec le flux existant.
 *
 * Injecté par constructeur (`@Inject`) et unique (`@Singleton`) : le JSON n'est parsé qu'à la
 * première utilisation grâce au chargement paresseux.
 */
@Singleton
class RecipeContentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val recipesById: Map<String, Recipe> by lazy {
        loadRecipes().associateBy { it.id }
    }

    /** Retourne la recette correspondant à l'identifiant, ou `null` si elle n'existe pas. */
    fun getById(id: String): Recipe? = recipesById[id]

    /** Retourne toutes les recettes d'une catégorie donnée. */
    fun getByCategory(category: RecipeCategory): List<Recipe> =
        recipesById.values.filter { it.category == category }

    /** Retourne l'ensemble des recettes chargées. */
    fun getAll(): List<Recipe> = recipesById.values.toList()

    private fun loadRecipes(): List<Recipe> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        RecipeJsonParser.parse(json).also {
            Timber.i("%d recettes chargées depuis %s", it.size, ASSET_FILE)
        }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement des recettes (%s)", ASSET_FILE)
        emptyList()
    }

    private companion object {
        const val ASSET_FILE = "recipes.json"
    }
}
