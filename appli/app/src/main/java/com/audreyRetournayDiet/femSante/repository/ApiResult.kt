package com.audreyRetournayDiet.femSante.repository

/**
 * Encapsule le résultat d'une opération asynchrone (Réseau ou Base de données).
 * * L'utilisation d'une `sealed class` permet de définir une hiérarchie stricte :
 * un résultat est SOIT un succès, SOIT un échec, évitant ainsi les états incohérents
 * (comme avoir des données ET une erreur en même temps).
 * * @param T Le type de donnée attendu en cas de succès (ex: User, List<Recipe>, JSONObject).
 */
sealed class ApiResult<out T> {

    /**
     * Représente une opération réussie.
     * * @property data Les données récupérées (peut être null pour certaines opérations comme une suppression).
     * @property message Un message de confirmation ou d'information provenant de la source.
     */
    data class Success<T>(val data : T?, val message: String) : ApiResult<T>()

    /**
     * Représente une opération ayant échoué.
     * * @property message Le message d'erreur explicite à afficher à l'utilisatrice ou à logger.
     */
    data class Failure(val message: String) : ApiResult<Nothing>()
}