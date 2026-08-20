package com.audreyRetournayDiet.femSante.data.resource

/**
 * Fiche documentaire PDF de l'écran « Ressources » (nutrition) — ex. intolérances, sensibilités
 * alimentaires. Données issues de `assets/resources.json`, ouvertes dans `PdfActivity`.
 *
 * [id] identifie la fiche (nom de fichier sans extension) ; [pdf] est le fichier réellement
 * ouvert.
 */
data class ResourceDocument(
    val id: String,
    val pdf: String,
    val title: String
)
