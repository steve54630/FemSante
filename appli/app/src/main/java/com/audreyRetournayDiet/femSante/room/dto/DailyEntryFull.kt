package com.audreyRetournayDiet.femSante.room.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity

data class DailyEntryFull(
    @Embedded
    val dailyEntry: DailyEntryEntity,

    @Relation(
        parentColumn = "id",      // L'ID de DailyEntryEntity
        entityColumn = "entry_id" // Le champ entry_id dans GeneralStateEntity
    )
    val generalState: GeneralStateEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val psychologicalState: PsychologicalStateEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val symptomsState: SymptomStateEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "entry_id"
    )
    val contextState: ContextStateEntity?
)