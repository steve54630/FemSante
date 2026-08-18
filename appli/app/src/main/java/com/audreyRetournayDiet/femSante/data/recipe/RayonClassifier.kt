package com.audreyRetournayDiet.femSante.data.recipe

/**
 * Associe un **aliment** (nom d'ingrédient) à son [Rayon] de magasin.
 *
 * Table définie une seule fois par aliment (et non dupliquée sur chaque ligne d'ingrédient des
 * recettes) : couvre les 102 aliments du catalogue actuel. Tout aliment inconnu retombe sur
 * [Rayon.AUTRE]. Réservé à la construction de la liste de courses.
 */
object RayonClassifier {

    private val byName: Map<String, Rayon> = buildMap {
        putAll(Rayon.FRUITS_LEGUMES, listOf(
            "Ail", "Avocat", "Banane", "Blettes", "Carottes", "Champignons", "Citron", "Crudités",
            "Épinards", "Feuilles de salade", "Jus de citron", "Melon", "Myrtilles", "Oignon",
            "Oignon jaune", "Panais", "Pommes", "Pousses d'épinards",
            "Pousses d'épinards (hiver/printemps) ou tomates cerises (été)",
            "Pousses d'épinards / roquette / petits pois / betterave"
        ))
        putAll(Rayon.BOULANGERIE, listOf("Pain sans gluten", "Pâte à tarte"))
        putAll(Rayon.CREMERIE_OEUFS, listOf(
            "Beurre", "Fromage frais de chèvre ou brebis", "Lait", "Œuf", "Œufs", "Tofu",
            "Tofu fumé", "Yaourt de cacao", "Yaourt végétal"
        ))
        putAll(Rayon.POISSONNERIE, listOf("Protéine", "Saumon fumé"))
        putAll(Rayon.CONSERVES, listOf(
            "Betterave cuite", "Betteraves cuites", "Châtaignes cuites", "Haricots rouges",
            "Pois chiches", "Thon"
        ))
        putAll(Rayon.EPICERIE_SECHE, listOf(
            "Amandes et noisettes", "Bicarbonate", "Bouillon cube de légumes", "Cacao",
            "Cacao en poudre", "Chocolat noir", "Chocolat noir 70 %", "Compote de pommes",
            "Compote sans sucre", "Dattes Medjool", "Farine de blé T110", "Farine de maïs",
            "Farine de pois chiches", "Farine de riz complet", "Farine de sarrasin",
            "Farine sans gluten", "Flocons d'avoine", "Graines de courge", "Graines de lin",
            "Graines de sésame", "Lentilles corail", "Levure boulangère", "Levure chimique",
            "Noix de coco râpée", "Pépites de chocolat noir", "Poudre à lever", "Poudre d'amande",
            "Poudre d'amandes", "Psyllium", "Semoule de maïs", "Sucre complet"
        ))
        putAll(Rayon.HUILES_CONDIMENTS, listOf(
            "Beurre de cacahuètes", "Huile d'olive", "Huile de coco", "Huile de colza", "Moutarde",
            "Moutarde douce", "Purée d'amandes", "Sauce chili", "Sauce soja", "Sauce soja sucrée",
            "Tahini", "Vinaigre balsamique"
        ))
        putAll(Rayon.EPICES, listOf(
            "Cannelle", "Cumin", "Cumin, sel, poivre", "Curcuma", "Curry", "Origan", "Paprika",
            "Poivre", "Poivre, sel", "Sel", "Sel, poivre", "Sel, poivre, cumin", "Thym"
        ))
        putAll(Rayon.BOISSONS_VEGETALES, listOf(
            "Boisson végétale", "Crème de coco", "Lait de coco", "Lait végétal",
            "Lait végétal ou crème végétale"
        ))
        putAll(Rayon.AUTRE, listOf("Eau", "Eau tiède"))
    }

    /** Rayon de l'aliment, ou [Rayon.AUTRE] s'il n'est pas répertorié. */
    fun rayonFor(name: String): Rayon = byName[name] ?: Rayon.AUTRE

    private fun MutableMap<String, Rayon>.putAll(rayon: Rayon, names: List<String>) {
        names.forEach { put(it, rayon) }
    }
}
