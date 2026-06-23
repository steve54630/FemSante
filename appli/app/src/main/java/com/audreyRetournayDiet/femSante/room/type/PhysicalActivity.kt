package com.audreyRetournayDiet.femSante.room.type

import com.audreyRetournayDiet.femSante.room.converter.ActivityConverter

/**
 * Représente l'intensité de l'effort physique fourni durant la journée.
 *
 * ### Rôle fonctionnel :
 * Utilisé dans [com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity] pour suivre l'hygiène de vie.
 * Cet indicateur permet de voir, par exemple, si le mouvement améliore
 * ou exacerbe certains symptômes (douleurs pelviennes, transit).
 *
 * ### Persistance :
 * Converti en String par [ActivityConverter] pour le stockage SQLite.
 */
enum class PhysicalActivity {
    /** * Activité minimale ou sédentaire.
     * Idéal pour identifier les phases de récupération nécessaire.
     */
    REPOS,

    /** * Activité modérée (marche active, trajet quotidien, ménage).
     * Représente le mouvement doux, souvent recommandé en santé féminine.
     */
    MARCHE,

    /** * Séance d'entraînement structurée (Yoga intense, Cardio, Musculation).
     */
    SPORT
}