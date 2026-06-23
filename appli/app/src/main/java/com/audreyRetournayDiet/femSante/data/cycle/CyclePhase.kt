package com.audreyRetournayDiet.femSante.data.cycle

/**
 * Phase du cycle menstruel estimée pour une date donnée.
 * [INDETERMINEE] couvre les cas où l'estimation n'est pas fiable (historique absent,
 * retard anormal) — on préfère « indéterminée » à une fausse information (zéro anxiété).
 */
enum class CyclePhase {
    MENSTRUELLE,
    FOLLICULAIRE,
    OVULATION,
    LUTEALE,
    INDETERMINEE
}
