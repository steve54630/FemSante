package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entry",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GeneralStateEntity::class,
            parentColumns = ["id"],
            childColumns = ["general_state_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PsychologicalStateEntity::class,
            parentColumns = ["id"],
            childColumns = ["psychological_state_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SymptomStateEntity::class,
            parentColumns = ["id"],
            childColumns = ["symptoms_state_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContextStateEntity::class,
            parentColumns = ["id"],
            childColumns = ["context_state_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id", "date"], unique = true),
        Index(value = ["general_state_id"]),
        Index(value = ["psychological_state_id"]),
        Index(value = ["symptoms_state_id"]),
        Index(value = ["context_state_id"])
    ]
)
data class DailyEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "general_state_id")
    val generalStateId: Long,

    @ColumnInfo(name = "psychological_state_id")
    val psychologicalStateId: Long,

    @ColumnInfo(name = "symptoms_state_id")
    val symptomsStateId: Long,

    @ColumnInfo(name = "context_state_id")
    val contextStateId: Long
)