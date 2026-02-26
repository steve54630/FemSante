package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.repository.ApiResult

class RecipeRepository {

    fun getRecipesFromAssets(folderName: String, context : Context): ApiResult<List<String>> {
        return try {
            val fileList = context.assets.list(folderName)?.toList() ?: emptyList()

            if (fileList.isEmpty()) {
                ApiResult.Failure("Aucune recette trouvée dans le dossier $folderName")
            } else {
                val pdfFiles = fileList.filter { it.endsWith(".pdf") }
                ApiResult.Success(data = pdfFiles, message = "Success")
            }
        } catch (exception: Exception) {
            ApiResult.Failure("Erreur lors de l'accès aux fichiers : ${exception.message}")
        }
    }
}