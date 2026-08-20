package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxFileJsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolboxFileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val adviceCache: List<ToolboxAdvice> by lazy { load() }

    /** Toutes les fiches naturopathie, dans l'ordre du JSON. */
    fun getAll(): List<ToolboxAdvice> = adviceCache

    private fun load(): List<ToolboxAdvice> = try {
        val json = context.assets.open(ASSET_FILE).bufferedReader().use { it.readText() }
        ToolboxFileJsonParser.parse(json).also { Timber.i("%d fiches naturopathie chargées depuis %s", it.size, ASSET_FILE) }
    } catch (exception: Exception) {
        Timber.e(exception, "Échec du chargement des fiches naturopathie (%s)", ASSET_FILE)
        emptyList()
    }

    private companion object {
        const val ASSET_FILE = "toolbox.json"
    }
}