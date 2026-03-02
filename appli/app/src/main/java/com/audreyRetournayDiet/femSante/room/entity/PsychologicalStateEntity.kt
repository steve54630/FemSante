package com.audreyRetournayDiet.femSante.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.audreyRetournayDiet.femSante.room.converter.CausesConverters
import com.audreyRetournayDiet.femSante.room.type.DayQuality
import com.audreyRetournayDiet.femSante.room.type.DifficultyCause

/**
 * Entité représentant l'état psychologique et émotionnel d'une journée.
 *
 * ### Architecture de données :
 * - **Lien Parent** : Connecté à [DailyEntryEntity]. La suppression de la journée
 * entraîne la suppression de cet état ([ForeignKey.CASCADE]).
 * - **Conversion complexe** : Utilise [CausesConverters] pour stocker une liste
 * de causes dans une seule colonne SQL.
 */
@Entity(
    tableName = "psychological_state",
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["entry_id"])]
)
data class PsychologicalStateEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    /** Évaluation globale du moral (ex: BONNE, MOYENNE, DIFFICILE) */
    @ColumnInfo(name = "day_quality")
    val dayQuality : DayQuality = DayQuality.BONNE,

    /** Référence obligatoire vers la journée parente */
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    /** * Liste des raisons expliquant une éventuelle difficulté (ex: Stress, Fatigue, Cycle).
     * @TypeConverters permet de transformer la List en String pour SQLite.
     */
    @ColumnInfo(name = "causes")
    @param:TypeConverters(CausesConverters::class)
    val difficultyCauses : List<DifficultyCause> = emptyList(),

    /** Champ libre pour des notes personnelles ou observations spécifiques */
    @ColumnInfo(name = "autres")
    val autres : String? = null
)