package com.audreyRetournayDiet.femSante.room.converter

import androidx.room.TypeConverter
import com.audreyRetournayDiet.femSante.room.type.FlowLevel

/**
 * Convertisseur Room pour l'abondance du flux menstruel.
 * Nullable : null signifie « pas de flux renseigné » (jour sans règles).
 */
class FlowConverter {

    @TypeConverter
    fun fromFlowLevel(value: FlowLevel?): String? = value?.name

    @TypeConverter
    fun toFlowLevel(value: String?): FlowLevel? = value?.let { FlowLevel.valueOf(it) }
}
