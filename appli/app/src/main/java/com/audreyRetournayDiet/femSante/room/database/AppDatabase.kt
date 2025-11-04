package com.audreyRetournayDiet.femSante.room.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.audreyRetournayDiet.femSante.room.converter.ActivityConverter
import com.audreyRetournayDiet.femSante.room.converter.CausesConverters
import com.audreyRetournayDiet.femSante.room.converter.ListConverter
import com.audreyRetournayDiet.femSante.room.converter.QualityConverter
import com.audreyRetournayDiet.femSante.room.dao.ContextStateDao
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.room.dao.GeneralStateDao
import com.audreyRetournayDiet.femSante.room.dao.PsychologicalStateDao
import com.audreyRetournayDiet.femSante.room.dao.SymptomStateDao
import com.audreyRetournayDiet.femSante.room.dao.UserDao
import com.audreyRetournayDiet.femSante.room.entity.ContextStateEntity
import com.audreyRetournayDiet.femSante.room.entity.DailyEntryEntity
import com.audreyRetournayDiet.femSante.room.entity.GeneralStateEntity
import com.audreyRetournayDiet.femSante.room.entity.PsychologicalStateEntity
import com.audreyRetournayDiet.femSante.room.entity.SymptomStateEntity
import com.audreyRetournayDiet.femSante.room.entity.UserEntity

@Database(
    version = 1,
    exportSchema = false,
    entities = [
        UserEntity::class,
        GeneralStateEntity::class,
        PsychologicalStateEntity::class,
        SymptomStateEntity::class,
        ContextStateEntity::class,
        DailyEntryEntity::class
    ]
)
@TypeConverters(value = [
    ActivityConverter::class,
    CausesConverters::class,
    ListConverter::class,
    QualityConverter::class
])
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao() : UserDao
    abstract fun generalDao() : GeneralStateDao
    abstract fun psychologicalDao() : PsychologicalStateDao
    abstract fun symptomsDao() : SymptomStateDao
    abstract fun contextDao(): ContextStateDao
    abstract fun dailyDao() : DailyEntryDao

}