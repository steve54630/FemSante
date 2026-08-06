package com.audreyRetournayDiet.femSante.room.type

import com.audreyRetournayDiet.femSante.room.converter.PainZoneConverter

/**
 * Catalogue des zones anatomiques sujettes aux douleurs ou à l'inconfort.
 *
 * ### Rôle fonctionnel :
 * Utilisé par [com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity] sous forme de liste pour permettre
 * une multi-sélection (ex : douleurs au bassin ET aux lombaires simultanément).
 *
 * ### Signification clinique :
 * - **BASSIN** : Douleurs pelviennes, crampes utérines, ovaires.
 * - **LOMBAIRES** : Douleurs dans le bas du dos, souvent liées au cycle.
 * - **SEINS** : Tension mammaire (mastodynie).
 * - **TETE** : Migraines cataméniales ou céphalées de tension.
 */
enum class PainZone {
    /** Douleurs pelviennes basses. */
    BASSIN,

    /** Irradiation dans le bas du dos ou les reins. */
    LOMBAIRES,

    /** Sensibilité ou gonflement des glandes mammaires. */
    SEINS,

    /** Maux de tête ou migraines. */
    TETE,

    /** Douleurs ou ballonnements abdominaux (distincts du bassin). */
    ABDOMEN,

    /** Bras (épaules, coudes, avant-bras). */
    BRAS,

    /** Cuisses (face avant des jambes). */
    CUISSES,

    /** Haut du dos (entre les omoplates, trapèzes). */
    HAUT_DOS,

    /** Jambes (mollets, bas des jambes). */
    JAMBES,

    /** Toute autre zone (articulations, etc.). */
    AUTRE
}