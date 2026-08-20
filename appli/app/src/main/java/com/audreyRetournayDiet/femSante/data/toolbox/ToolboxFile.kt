package com.audreyRetournayDiet.femSante.data.toolbox

/** Catégorie de fiche. **L'ordre de déclaration = ordre d'affichage des sections** dans la liste. */
enum class ToolboxCategory(val label: String) {
    MASSAGE("Massage"),
    THERMOTHERAPY("Thermothérapie"),
    AROMATHERAPY("Aromathérapie"),
    PHYTOTHERAPY("Phytothérapie")
}

data class Author(
    val name: String,
    val role: String
)

data class ToolboxItem(
    val name: String,
    val latinName: String? = null,
    val properties: List<String> = emptyList(),
    val contraindications: List<String> = emptyList()
)

data class ToolboxProtocol(
    val indication: String,
    val applicationSite: String,
    val steps: List<String>
)

data class ToolboxAdvice(
    val id: String,
    val title: String,
    val category: ToolboxCategory,
    val author: Author,
    val summary: String,
    val items: List<ToolboxItem>,
    val protocols: List<ToolboxProtocol>,
    val precautions: List<String>
)