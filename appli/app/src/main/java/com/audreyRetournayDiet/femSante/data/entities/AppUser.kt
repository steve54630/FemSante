package com.audreyRetournayDiet.femSante.data.entities

/**
 * Compte utilisateur en session.
 *
 * @param lifetimeAccess abonnement **à vie** (champ API « A vie ») — information d'affichage
 *   (« Mon compte »), pas le critère d'accès au contenu.
 * @param hasAccess **a-t-elle accès aux contenus premium ?** — vrai pour une abonnée active
 *   (à vie, mensuelle ou en période d'essai), faux en mode gratuit (freemium). C'est le champ
 *   à utiliser pour le gating (voir `UserStore.hasContentAccess`).
 */
data class AppUser(
    val id: String,
    val lifetimeAccess: Boolean,
    val email: String,
    val password: String,
    val hasAccess: Boolean = false,
)
