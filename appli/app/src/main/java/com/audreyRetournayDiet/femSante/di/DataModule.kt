package com.audreyRetournayDiet.femSante.di

import android.content.Context
import com.audreyRetournayDiet.femSante.repository.local.CycleRepository
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.repository.local.RecipeRepository
import com.audreyRetournayDiet.femSante.room.dao.CycleDayDao
import com.audreyRetournayDiet.femSante.room.dao.DailyEntryDao
import com.audreyRetournayDiet.femSante.room.database.AppDatabase
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.shared.UserStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module Hilt déclarant comment fabriquer les dépendances de la couche données.
 *
 * `@InstallIn(SingletonComponent::class)` : ces objets vivent à l'échelle de
 * l'application (un seul exemplaire partagé).
 *
 * Chaque `@Provides` est une « recette » : quand un composant a besoin du type
 * retourné, Hilt appelle cette fonction (et injecte automatiquement ses paramètres).
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * On délègue à [DatabaseProvider] existant pour garantir une instance unique et
     * chiffrée (SQLCipher), partagée avec le code pas encore migré vers Hilt.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        DatabaseProvider.getDatabase(context)

    @Provides
    fun provideDailyDao(db: AppDatabase): DailyEntryDao = db.dailyDao()

    @Provides
    fun provideCycleDao(db: AppDatabase): CycleDayDao = db.cycleDao()

    @Provides
    fun provideDailyRepository(dao: DailyEntryDao): DailyRepository = DailyRepository(dao)

    @Provides
    fun provideCycleRepository(dao: CycleDayDao): CycleRepository = CycleRepository(dao)

    @Provides
    fun provideRecipeRepository(): RecipeRepository = RecipeRepository()

    @Provides
    @Singleton
    fun provideUserStore(@ApplicationContext context: Context): UserStore = UserStore(context)
}
