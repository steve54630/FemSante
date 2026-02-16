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
        parentColumn = "general_state_id",
        entityColumn = "id"
    )
    val generalState: GeneralStateEntity,

    @Relation(
        parentColumn = "psychological_state_id",
        entityColumn = "id"
    )
    val psychologicalState: PsychologicalStateEntity,

    @Relation(
        parentColumn = "symptoms_state_id",
        entityColumn = "id"
    )
    val symptomsState: SymptomStateEntity,

    @Relation(
        parentColumn = "context_state_id",
        entityColumn = "id"
    )
    val contextState: ContextStateEntity
)