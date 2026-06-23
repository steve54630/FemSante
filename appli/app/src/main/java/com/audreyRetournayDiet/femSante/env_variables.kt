package com.audreyRetournayDiet.femSante

/**
 * Façade d'accès à la configuration runtime.
 *
 * Les valeurs ne sont plus codees en dur ici : elles proviennent de `local.properties`
 * (ou des variables d'environnement), injectees au build via `buildConfigField` puis
 * exposees par [BuildConfig]. Ce fichier ne contient donc aucun secret et peut etre
 * versionne ; les valeurs reelles, elles, restent hors du depot.
 *
 * Voir `local.properties.example` pour la liste des cles attendues.
 */

val PAYPAL_CLIENT_ID: String = BuildConfig.PAYPAL_CLIENT_ID

val API_URL: String = BuildConfig.API_URL

val RETURN_URL_CARD: String = BuildConfig.RETURN_URL_CARD

val RETURN_URL_PAYPAL: String = BuildConfig.RETURN_URL_PAYPAL

val AUTHORIZATION_HEADERS: String = BuildConfig.AUTHORIZATION_HEADERS
