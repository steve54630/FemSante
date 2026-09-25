package com.audreyRetournayDiet.femSante.data.plant

/**
 * Fiche d'une plante du lexique de phytothérapie, dans la Boîte à outils.
 *
 * Données statiques issues de `assets/plants.json`, rédigées par Audrey Retournay : symptôme
 * principal, symptômes ciblés, partie utilisée, propriétés, formes galéniques disponibles, dose
 * maximale par jour et contre-indications.
 *
 * [maxDailyDose] reste une `String` : le format varie selon la plante (« 3g », « 8g/L »,
 * « 5g/L (bain) », « 3 à 5g (poudre) ») — on préserve la nuance plutôt que d'inventer une
 * précision numérique inexistante.
 *
 * [symptomCategories] : catégories de symptômes (tagging validé par Audrey, voir
 * [PlantSymptomCategory]), sert de filtre sur l'écran du lexique — distinct de [targetSymptoms]
 * qui reste le texte libre affiché sur la fiche détail.
 */
data class Plant(
    val id: String,
    val name: String,
    val latinName: String? = null,
    val mainSymptom: String,
    val targetSymptoms: List<String> = emptyList(),
    val partsUsed: List<String> = emptyList(),
    val properties: List<String> = emptyList(),
    val galenicForms: List<String> = emptyList(),
    val maxDailyDose: String? = null,
    val contraindications: List<String> = emptyList(),
    val symptomCategories: List<PlantSymptomCategory> = emptyList()
)

/**
 * Catégorie de symptôme (chips de filtre du lexique). Tagging manuel validé par Audrey
 * (2026-09-25) — liste fermée, pas de déduction automatique à partir du texte libre.
 * L'ordre de déclaration = ordre d'affichage des chips.
 */
enum class PlantSymptomCategory(val label: String) {
    DIGESTION_TRANSIT("Digestion & transit"),
    REGLES_CYCLE("Règles & cycle"),
    CIRCULATION_RETENTION("Circulation & rétention d'eau"),
    STRESS_SOMMEIL("Stress, anxiété & sommeil"),
    FATIGUE_VITALITE("Fatigue & vitalité"),
    FOIE_DETOX("Foie & détox"),
    DOULEURS_PELVIENNES("Douleurs pelviennes & inflammation"),
    DOULEURS_MUSCULAIRES("Douleurs musculaires & articulaires"),
    IMMUNITE_INFECTIONS("Immunité & infections"),
    RESPIRATOIRE("Respiratoire"),
    PEAU_MUQUEUSES("Peau & muqueuses")
}
