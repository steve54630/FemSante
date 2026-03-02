package com.audreyRetournayDiet.femSante.room.type

import com.audreyRetournayDiet.femSante.room.converter.CausesConverters

/**
 * Catalogue des causes principales impactant négativement le bien-être psychologique.
 *
 * ### Rôle fonctionnel :
 * Utilisé dans [com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity] pour qualifier les sources de difficulté.
 * L'utilisatrice peut en sélectionner plusieurs pour une même journée.
 *
 * ### Persistance :
 * Converti en une chaîne JSON ou délimitée par [CausesConverters] pour
 * être stocké dans une seule colonne SQLite.
 */
enum class DifficultyCause {

    /** Sentiment d'agacement, d'irritabilité ou de frustration. */
    COLERE,

    /** Tension nerveuse, surcharge mentale ou anxiété liée au quotidien. */
    STRESS,

    /** Baisse de moral, mélancolie ou sentiment de déprime. */
    TRISTESSE,

    /** Cause non répertoriée (sera généralement précisée dans le champ 'autres'). */
    AUTRE
}