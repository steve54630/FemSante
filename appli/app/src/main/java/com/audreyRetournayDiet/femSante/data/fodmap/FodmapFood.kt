package com.audreyRetournayDiet.femSante.data.fodmap

/**
 * Entrée du guide FODMAP : classement « feu tricolore » d'un aliment (Riche/Modéré/Pauvre en
 * FODMAPs), sa portion-limite et des remarques pratiques (substituts, précautions).
 *
 * Données issues de `assets/fodmap.json`. [portion] est volontairement une `String` : la donnée
 * n'est pas toujours un nombre simple (« Libre », « 10-15 grains », « 1/8e »…) — on préserve la
 * nuance plutôt que d'inventer une précision inexistante (même choix que [NutrientIntake]).
 */
data class FodmapFood(
    val id: String,
    val name: String,
    val category: FodmapCategory,
    val status: FodmapStatus,
    val portion: String,
    val notes: String
)

/** Grande famille alimentaire (métadonnée affichée, pas un filtre — cf. décision produit). */
enum class FodmapCategory(val label: String) {
    FRUITS("Fruits"),
    FRUITS_A_COQUE("Fruits à coque"),
    LEGUMES("Légumes"),
    FECULENTS("Féculents"),
    LEGUMINEUSES("Légumineuses"),
    PRODUITS_LAITIERS("Produits laitiers"),
    SUCRES("Sucres"),
    EDULCORANTS("Édulcorants"),
    CONDIMENTS("Condiments"),
    BOISSONS("Boissons"),
    PROTEINES("Protéines"),
    DIVERS("Divers"),
    MATIERES_GRASSES("Matières grasses")
}

/**
 * Statut FODMAP (feu tricolore). **L'ordre de déclaration = ordre des chips de filtre.**
 * Le [label] est toujours affiché à côté de la pastille colorée (jamais la couleur seule).
 */
enum class FodmapStatus(val label: String) {
    PAUVRE("Pauvre"),
    MODERE("Modéré"),
    RICHE("Riche")
}
