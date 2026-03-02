package com.audreyRetournayDiet.femSante.room.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity

/**
 * Objet de transfert de données (DTO) représentant une journée complète de suivi.
 * * Cette classe agrège l'entrée principale et tous ses sous-états associés.
 * Elle évite de multiplier les requêtes manuelles dans le Repository.
 */
data class DailyEntryFull(
    /**
     * L'entrée parente (ID, Date, UserID).
     * @Embedded permet de considérer les champs de cette entité comme faisant
     * partie directement de cet objet pour la requête SQL.
     */
    @Embedded
    val dailyEntry: DailyEntryEntity,

    /**
     * État physique général (Sommeil, Énergie, Douleur).
     * Room fait le lien entre `dailyEntry.id` et `generalState.entry_id`.
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val generalState: GeneralStateEntity?,

    /**
     * État psychologique (Stress, Humeur).
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val psychologicalState: PsychologicalStateEntity?,

    /**
     * Liste des symptômes et zones de douleur.
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val symptomsState: SymptomStateEntity?,

    /**
     * Contexte de vie (Hydratation, Cycle, Repas).
     */
    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val contextState: ContextStateEntity?
)