package com.audreyRetournayDiet.femSante.room.type

/**
 * Représente l'évaluation qualitative globale d'une journée par l'utilisatrice.
 *
 * ### Usage :
 * Cette énumération est utilisée dans [com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity] pour offrir
 * un indicateur rapide du moral et du bien-être général.
 *
 * ### Mapping Room :
 * Converti en String via [com.audreyRetournayDiet.femSante.room.converter.QualityConverter] ("BONNE", "MOYENNE", "MAUVAISE").
 */
enum class DayQuality {
    /** Journée positive, moral stable, peu ou pas de symptômes gênants. */
    BONNE,

    /** Journée mitigée, présence de fatigue ou de stress modéré. */
    MOYENNE,

    /** Journée difficile, crise de douleur, stress intense ou moral bas. */
    MAUVAISE
}