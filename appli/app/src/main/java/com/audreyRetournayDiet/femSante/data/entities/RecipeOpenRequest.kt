package com.audreyRetournayDiet.femSante.data.entities

/**
 * Demande d'ouverture d'une recette sélectionnée.
 *
 * @param recipeId identifiant de la recette (nom de fichier sans extension), clé du catalogue natif.
 * @param pdfPath chemin du PDF d'origine, utilisé pour le bouton « Voir la fiche PDF » et comme
 *   repli si la recette n'existe pas encore dans le catalogue natif.
 */
data class RecipeOpenRequest(
    val recipeId: String,
    val pdfPath: String
)
