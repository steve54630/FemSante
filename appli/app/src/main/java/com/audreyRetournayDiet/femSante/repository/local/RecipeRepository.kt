package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import android.util.Log
import com.audreyRetournayDiet.femSante.repository.ApiResult

class RecipeRepository {

    private val tag = "REPO_RECIPES"

    fun getRecipesFromAssets(folderName: String, context : Context): ApiResult<List<String>> {
        return try {
            Log.d(tag, "Tentative de lecture du dossier assets : $folderName")

            val fileList = context.assets.list(folderName)?.toList() ?: emptyList()

            if (fileList.isEmpty()) {
                Log.w(tag, "Dossier vide ou inexistant dans les assets : $folderName")
                ApiResult.Failure("Aucune recette trouvée dans le dossier $folderName")
            } else {
                val pdfFiles = fileList.filter { it.endsWith(".pdf") }
                Log.i(tag, "${pdfFiles.size} fichiers PDF trouvés dans le dossier '$folderName'")

                ApiResult.Success(data = pdfFiles, message = "Success")
            }
        } catch (exception: Exception) {
            Log.e(tag, "Erreur fatale lors de l'accès aux assets (Dossier: $folderName)", exception)
            ApiResult.Failure("Erreur lors de l'accès aux fichiers : ${exception.message}")
        }
    }
}