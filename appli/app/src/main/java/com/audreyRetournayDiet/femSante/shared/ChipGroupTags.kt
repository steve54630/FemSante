package com.audreyRetournayDiet.femSante.shared

import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Helpers génériques **ChipGroup ↔ Enum** : chaque [Chip] porte sa valeur dans son `tag`, ce qui
 * évite de jongler avec des `String`. Factorisé depuis les fragments de saisie du journal
 * (`PsychologicalFragment`, `ContextFragment`…), où la même logique était dupliquée.
 */

/** Ajoute un [Chip] cochable par valeur d'enum (libellé = `name`, valeur portée par `tag`). */
fun <T : Enum<T>> ChipGroup.addTagChips(entries: List<T>) {
    entries.forEach { entry ->
        addView(
            Chip(context).apply {
                text = entry.name
                isCheckable = true
                tag = entry
                id = View.generateViewId()
            }
        )
    }
}

/** Coche l'unique Chip dont le `tag` correspond (sélection simple). No-op si absent. */
fun ChipGroup.checkChipByTag(tag: Any?) {
    for (i in 0 until childCount) {
        val chip = getChildAt(i) as? Chip ?: continue
        if (chip.tag == tag) {
            if (!chip.isChecked) chip.isChecked = true
            return
        }
    }
}

/** Aligne les Chips cochés sur l'ensemble de `tags` donné (sélection multiple). */
fun ChipGroup.checkChipsByTags(tags: Collection<*>) {
    for (i in 0 until childCount) {
        val chip = getChildAt(i) as? Chip ?: continue
        val shouldCheck = chip.tag in tags
        if (chip.isChecked != shouldCheck) chip.isChecked = shouldCheck
    }
}

/** Valeur (`tag`) du Chip coché en sélection simple, ou `null`. */
@Suppress("UNCHECKED_CAST")
fun <T> ChipGroup.selectedTag(): T? = findViewById<Chip>(checkedChipId)?.tag as? T

/** Valeurs (`tag`) des Chips cochés en sélection multiple. */
@Suppress("UNCHECKED_CAST")
fun <T> ChipGroup.selectedTags(): List<T> =
    checkedChipIds.mapNotNull { findViewById<Chip>(it)?.tag as? T }
