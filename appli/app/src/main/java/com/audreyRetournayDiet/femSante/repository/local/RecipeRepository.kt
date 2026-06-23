package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.repository.ApiResult
import timber.log.Timber

/**
 * Repository gérant l'accès aux fiches de recettes et protocoles alimentaires.
 *
 * Contrairement aux données utilisateurs, les recettes sont des ressources statiques
 * packagées directement dans le dossier `assets` de l'application.
 *
 * Ce repository permet de lister dynamiquement les fichiers PDF présents dans
 * des sous-dossiers spécifiques (ex : "Petit-déjeuner", "Plats IG Bas").
 */
class RecipeRepository {

    /**
     * Scanne un dossier spécifique dans les assets pour en extraire la liste des fichiers PDF.
     *
     * @param folderName Nom du sous-dossier à explorer (ex : "Recettes/Saison").
     * @param context Context nécessaire pour accéder au [android.content.res.AssetManager] du système.
     * @return Un [ApiResult] contenant la liste des noms de fichiers trouvés.
     */
    fun getRecipesFromAssets(folderName: String, context : Context): ApiResult<List<String>> {
        return try {
            Timber.d("Tentative de lecture du dossier assets : $folderName")

            // Récupération de la liste brute des fichiers dans le dossier
            val fileList = context.assets.list(folderName)?.toList() ?: emptyList()

            if (fileList.isEmpty()) {
                Timber.w("Dossier vide ou inexistant dans les assets : $folderName")
                ApiResult.Failure("Aucune recette trouvée dans le dossier $folderName")
            } else {
                // Filtrage pour ne garder que les documents PDF (ignore les dossiers ou fichiers cachés)
                val pdfFiles = fileList.filter { it.endsWith(".pdf") }
                Timber.i("${pdfFiles.size} fichiers PDF trouvés dans le dossier '$folderName'")

                ApiResult.Success(data = pdfFiles, message = "Success")
            }
        } catch (exception: Exception) {
            // Gestion des erreurs d'IO (entrée/sortie) ou d'accès aux ressources
            Timber.e(exception, "Erreur fatale lors de l'accès aux assets (Dossier: $folderName)")
            ApiResult.Failure("Erreur lors de l'accès aux fichiers : ${exception.message}")
        }
    }
}