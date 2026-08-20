package com.audreyRetournayDiet.femSante.shared

import org.json.JSONObject

/**
 * Coercition **tolérante** d'une valeur JSON hétérogène en booléen.
 *
 * `JSONObject.optBoolean` ne reconnaît qu'un vrai booléen JSON ou les chaînes `"true"`/`"false"` ;
 * un backend qui sérialise un `tinyint` (`1`/`0`) ou une chaîne `"1"` retomberait **silencieusement**
 * sur la valeur par défaut. Pour le flag freemium `acces`, cela bloquerait une abonnée payante à
 * cause d'une simple différence de sérialisation. On accepte donc ici les formes courantes.
 *
 * Logique **pure** (aucune dépendance à `org.json`) → testable en JVM. L'extension [optBooleanFlexible]
 * n'est qu'un mince adaptateur au-dessus.
 */
object FlexibleBoolean {

    private val TRUE_TOKENS = setOf("true", "1", "yes", "oui")

    /**
     * Interprète [value] en booléen : `Boolean` tel quel, `Number` vrai si ≠ 0, `String` vraie si
     * elle vaut (insensible à la casse) `true`/`1`/`yes`/`oui`. Toute autre valeur (dont `null`)
     * retourne [default].
     */
    fun of(value: Any?, default: Boolean = false): Boolean = when (value) {
        is Boolean -> value
        is Number -> value.toInt() != 0
        is String -> value.trim().lowercase() in TRUE_TOKENS
        else -> default
    }
}

/**
 * Lit une clé booléenne en tolérant le type réellement renvoyé par l'API (booléen, `1`/`0`, `"1"`…).
 * Voir [FlexibleBoolean] pour le pourquoi.
 */
fun JSONObject.optBooleanFlexible(key: String, default: Boolean = false): Boolean =
    if (has(key) && !isNull(key)) FlexibleBoolean.of(get(key), default) else default
