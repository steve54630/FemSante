package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.view.View
import androidx.core.view.isEmpty
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import androidx.core.view.isVisible
import timber.log.Timber

/**
 * Fragment gérant l'état psychologique et émotionnel de l'utilisatrice.
 *
 * Ce composant permet de :
 * - Évaluer la qualité globale de la journée ([DayQuality]).
 * - Identifier les causes potentielles de difficultés ([DifficultyCause]) via une sélection multiple.
 * - Saisir des précisions textuelles si la cause "AUTRE" est sélectionnée.
 *
 * Les options sont générées dynamiquement à partir des Enums pour garantir la cohérence
 * avec la base de données Room.
 */
@Suppress("UNCHECKED_CAST")
class PsychologicalFragment : Fragment(R.layout.fragment_psychological_state) {

    private val viewModel: EntryViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated : Initialisation de l'état psychologique")

        val chipGroupQuality = view.findViewById<ChipGroup>(R.id.chipGroupDayQuality)
        val chipGroupCauses = view.findViewById<ChipGroup>(R.id.chipGroupDifficultyCauses)
        val layoutAutres = view.findViewById<View>(R.id.layoutAutres)
        val etAutres = view.findViewById<TextInputEditText>(R.id.etAutresPsychological)

        // Génération dynamique des options pour éviter la maintenance manuelle du XML
        if (chipGroupQuality.isEmpty()) {
            Timber.d("Génération des Chips pour Qualité et Causes")
            setupChips(chipGroupQuality, DayQuality.entries)
            setupChips(chipGroupCauses, DifficultyCause.entries)
        }

        observePsychologicalState(chipGroupQuality, chipGroupCauses, layoutAutres, etAutres)
        setupInteractionListeners(chipGroupQuality, chipGroupCauses, etAutres)
    }

    /**
     * Observe le StateFlow du ViewModel pour synchroniser l'interface utilisateur.
     * Gère la sélection des chips, la visibilité du champ "Autre" et le contenu textuel.
     */
    private fun observePsychologicalState(
        groupQuality: ChipGroup,
        groupCauses: ChipGroup,
        layoutAutres: View,
        etAutres: TextInputEditText
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.psychologicalState.collect { state ->
                    Timber.v("Sync UI : Qualité=${state.dayQuality}, Nb Causes=${state.difficultyCauses.size}")

                    // Synchronisation des sélections (VM -> UI)
                    selectChipByTag(groupQuality, state.dayQuality)
                    selectChipsByTags(groupCauses, state.difficultyCauses)

                    // Gestion de la visibilité du champ "Autre"
                    val hasOther = state.difficultyCauses.contains(DifficultyCause.AUTRE)
                    if (layoutAutres.isVisible != hasOther) {
                        layoutAutres.isVisible = hasOther
                    }

                    // Mise à jour du texte (Anti-boucle pour préserver le curseur)
                    if (etAutres.text?.toString() != state.autres) {
                        etAutres.setText(state.autres)
                    }
                }
            }
        }
    }

    /**
     * Configure les listeners de changement d'état pour les composants de saisie.
     * Transmet les modifications au ViewModel via une fonction de mise à jour locale.
     */
    private fun setupInteractionListeners(
        groupQuality: ChipGroup,
        groupCauses: ChipGroup,
        etAutres: TextInputEditText
    ) {
        /** Envoie l'état actuel des vues vers le ViewModel */
        fun pushUpdate() {
            val quality = getSelectedTag<DayQuality>(groupQuality) ?: DayQuality.MOYENNE
            val causes = getSelectedTags<DifficultyCause>(groupCauses)
            val notes = etAutres.text?.toString()

            Timber.d("Action : pushUpdate -> Qualité: $quality, Causes: $causes")
            viewModel.updatePsychologicalState(quality, causes, notes)
        }

        groupQuality.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }
        groupCauses.setOnCheckedStateChangeListener { _, _ -> pushUpdate() }

        // On n'écoute la saisie que si l'utilisateur a le focus (évite les rebonds VM->UI)
        etAutres.addTextChangedListener {
            if (etAutres.hasFocus()) {
                pushUpdate()
            }
        }
    }

    // --- HELPERS UTILITAIRES ---

    /**
     * Génère dynamiquement des [Chip] à partir d'une liste d'Enum.
     *
     * Chaque Chip reçoit l'objet Enum dans sa propriété `tag` pour faciliter
     * la récupération de la valeur sélectionnée sans conversion de String.
     *
     * @param T Le type de l'Enum (ex: [DayQuality] ou [DifficultyCause]).
     * @param group Le [ChipGroup] où injecter les vues.
     * @param entries La liste des valeurs possibles.
     */
    private fun <T : Enum<T>> setupChips(group: ChipGroup, entries: List<T>) {
        entries.forEach { entry ->
            val chip = Chip(requireContext()).apply {
                text = entry.name
                isCheckable = true
                this.tag = entry
                id = View.generateViewId()
            }
            group.addView(chip)
        }
    }

    /**
     * Coche un [Chip] unique au sein d'un groupe en fonction de son Tag (Enum).
     *
     * @param group Le groupe contenant les Chips.
     * @param tagToSelect L'objet (Enum) correspondant à la sélection actuelle.
     */
    private fun selectChipByTag(group: ChipGroup, tagToSelect: Any?) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            if (chip.tag == tagToSelect) {
                if (!chip.isChecked) {
                    chip.isChecked = true
                }
                return
            }
        }
    }

    /**
     * Met à jour l'état de sélection multiple des Chips pour les causes de difficulté.
     *
     * @param group Le groupe de sélection multiple.
     * @param tagsToSelect La liste des causes actuellement enregistrées dans le ViewModel.
     */
    private fun selectChipsByTags(group: ChipGroup, tagsToSelect: List<DifficultyCause>) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as Chip
            val shouldBeChecked = tagsToSelect.contains(chip.tag)
            // On ne change l'état que si nécessaire pour éviter des triggers inutiles
            if (chip.isChecked != shouldBeChecked) {
                chip.isChecked = shouldBeChecked
            }
        }
    }

    /**
     * Récupère la valeur Enum du Chip actuellement sélectionné (Sélection Unique).
     *
     * @param group Le [ChipGroup] concerné.
     * @return La valeur de l'Enum castée au type [T], ou null si rien n'est coché.
     */
    private fun <T> getSelectedTag(group: ChipGroup): T? {
        val selectedId = group.checkedChipId
        return group.findViewById<Chip>(selectedId)?.tag as? T
    }

    /**
     * Récupère la liste des valeurs Enum des Chips cochés (Sélection Multiple).
     *
     * @param group Le [ChipGroup] concerné.
     * @return Une liste d'objets [T] correspondant aux tags des Chips sélectionnés.
     */
    private fun <T> getSelectedTags(group: ChipGroup): List<T> {
        return group.checkedChipIds.mapNotNull { id ->
            group.findViewById<Chip>(id)?.tag as? T
        }
    }
}