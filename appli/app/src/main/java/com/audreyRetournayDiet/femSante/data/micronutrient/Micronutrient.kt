package com.audreyRetournayDiet.femSante.data.micronutrient

/**
 * Fiche d'un micronutriment affichée nativement (vitamine, minéral, oligo-élément).
 *
 * Données statiques issues de `assets/micronutrients.json`, validées par la diététicienne
 * (apports par sexe et état physiologique, unités confirmées, sources en teneur pour 100 g,
 * cofacteurs d'assimilation, notes/précautions).
 *
 * Les apports [intake] et les teneurs [NutrientSource.amount] sont volontairement des `String` :
 * la donnée métier n'est pas toujours un nombre simple (« 16 (11 post-ménopause) », « 7,5 à 11 »,
 * « 100-300 /g »). On préserve la nuance plutôt que d'inventer une précision inexistante.
 *
 * [premium] : contenu réservé aux abonnées (gating via `UserStore.hasContentAccess`).
 *
 * [phase] : phases du cycle pour lesquelles la diététicienne recommande ce micronutriment (même
 * vocabulaire que [com.audreyRetournayDiet.femSante.data.recipe.Recipe.phase] — « Menstruelle »,
 * « Folliculaire », « Ovulatoire », « Lutéale », ou « Toutes »). Vide tant que le tagging n'est
 * pas fait : la liste "Pour toi" reste alors invisible plutôt que d'afficher une sélection non
 * validée par la diététicienne.
 */
data class Micronutrient(
    val id: String,
    val name: String,
    val otherName: String? = null,
    val group: NutrientGroup,
    val unit: String,
    val premium: Boolean = true,
    val intake: NutrientIntake,
    val sources: List<NutrientSource> = emptyList(),
    val cofactors: String? = null,
    val notes: String? = null,
    val phase: List<String> = emptyList()
)

/** Apports nutritionnels conseillés selon le profil (valeurs en [Micronutrient.unit]). */
data class NutrientIntake(
    val women: String,
    val men: String,
    val pregnant: String,
    val breastfeeding: String
)

/** Une source alimentaire et sa teneur (généralement pour 100 g, sauf mention dans [amount]). */
data class NutrientSource(
    val name: String,
    val amount: String
)

/** Grand groupe de classement (sert de chips de filtre). L'ordre de déclaration = ordre d'affichage. */
enum class NutrientGroup(val label: String) {
    LIPOSOLUBLE("Vitamines liposolubles"),
    HYDROSOLUBLE("Vitamines hydrosolubles"),
    MINERAL("Minéraux majeurs"),
    OLIGO("Oligo-éléments")
}

/**
 * Interaction médicament ↔ nutriment : un médicament (ou une classe) et les nutriments qu'il
 * appauvrit. Présenté comme une section à part de l'écran micronutriments.
 */
data class DrugInteraction(
    val drug: String,
    val nutrients: String
)
