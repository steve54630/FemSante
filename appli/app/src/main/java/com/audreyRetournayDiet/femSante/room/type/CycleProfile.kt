package com.audreyRetournayDiet.femSante.room.type

/**
 * Profil de cycle déclaré par l'utilisatrice. Conditionne tout l'affichage du suivi de
 * cycle pour respecter la règle « zéro anxiété » (cf. décisions diététicienne) :
 *
 * - [REGULIER] : phases + prédictions classiques (incréments 2 et 3).
 * - [IRREGULIER] : phase estimée si possible, mais **jamais** de compte à rebours.
 * - [ABSENT_OU_PILULE] : pas de cadran de cycle, suivi purement déclaratif.
 *
 * Tant que l'utilisatrice n'a pas répondu, on retient [IRREGULIER] par défaut : c'est le
 * mode le plus prudent (aucune prédiction, aucune promesse fausse).
 */
enum class CycleProfile {
    REGULIER,
    IRREGULIER,
    ABSENT_OU_PILULE
}
