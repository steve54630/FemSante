package com.audreyRetournayDiet.femSante.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Règle JUnit qui remplace le dispatcher Main par un dispatcher de test, le temps d'un
 * test. Indispensable pour tester des ViewModels qui lancent des coroutines sur
 * `viewModelScope` (lié à Dispatchers.Main).
 *
 * On utilise [UnconfinedTestDispatcher] : les coroutines s'exécutent immédiatement, ce
 * qui simplifie les assertions (pas besoin d'avancer le temps virtuel manuellement).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: kotlinx.coroutines.test.TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
