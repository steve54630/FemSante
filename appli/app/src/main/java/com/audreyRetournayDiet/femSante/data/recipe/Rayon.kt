package com.audreyRetournayDiet.femSante.data.recipe

/**
 * Rayon de magasin, pour regrouper la liste de courses.
 *
 * L'ordre de déclaration suit un **parcours de magasin** classique (frais → conserves → sec →
 * épices) et sert d'ordre d'affichage des sections de la liste.
 */
enum class Rayon(val label: String) {
    FRUITS_LEGUMES("Fruits et légumes"),
    BOULANGERIE("Boulangerie"),
    CREMERIE_OEUFS("Crèmerie et œufs"),
    POISSONNERIE("Poissonnerie"),
    CONSERVES("Conserves et bocaux"),
    EPICERIE_SECHE("Épicerie sèche"),
    HUILES_CONDIMENTS("Huiles et condiments"),
    EPICES("Épices et aromates"),
    BOISSONS_VEGETALES("Boissons végétales"),
    AUTRE("Autre")
}
