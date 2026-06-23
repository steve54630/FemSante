package com.audreyRetournayDiet.femSante.room.type

/**
 * Échelle de Bristol, standard médical de classification du transit intestinal (7 types).
 *
 * ### Rôle fonctionnel :
 * Permet de qualifier le transit digestif du jour, utilisé pour recommander du
 * contenu adapté (ex: fiches/infusions digestion) indépendamment de la douleur
 * générale ou de la zone douloureuse.
 */
enum class BristolType {
    /** Type 1 : Selles dures, séparées (constipation sévère). */
    TYPE_1,

    /** Type 2 : En forme de saucisse mais grumeleuse (constipation légère). */
    TYPE_2,

    /** Type 3 : Comme une saucisse avec des craquelures en surface (transit normal). */
    TYPE_3,

    /** Type 4 : Comme une saucisse ou un serpent, lisse et molle (transit idéal). */
    TYPE_4,

    /** Type 5 : Morceaux mous avec des bords nets (manque de fibres). */
    TYPE_5,

    /** Type 6 : Morceaux pâteux aux bords déchiquetés (tendance diarrhéique). */
    TYPE_6,

    /** Type 7 : Liquide, sans morceaux solides (diarrhée). */
    TYPE_7,

    /** Aucune observation renseignée pour la journée. */
    NON_RENSEIGNE
}
