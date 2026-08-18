package com.audreyRetournayDiet.femSante.repository.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.shoppingDataStore by preferencesDataStore(name = "shopping_list")

/**
 * Persiste la liste de courses **localement** (DataStore) : les recettes retenues avec leur
 * nombre de réalisations (`recette → nombre`) et les ingrédients déjà cochés en magasin.
 *
 * Expose des [Flow] réactifs → le badge de l'onglet et l'écran se mettent à jour tout seuls.
 * Rien n'est envoyé à un serveur.
 */
@Singleton
class ShoppingListStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val gson = Gson()
    private val selectionsKey = stringPreferencesKey("selections_json")
    private val checkedKey = stringSetPreferencesKey("checked_names")
    private val listType = object : TypeToken<List<StoredSelection>>() {}.type

    /** Recettes retenues (id → nombre de réalisations), ordre d'ajout conservé. */
    val selections: Flow<Map<String, Int>> =
        context.shoppingDataStore.data.map { prefs -> decode(prefs[selectionsKey]) }

    /** Noms d'ingrédients cochés (« déjà pris »). */
    val checkedNames: Flow<Set<String>> =
        context.shoppingDataStore.data.map { prefs -> prefs[checkedKey] ?: emptySet() }

    /** Ajoute la recette (×1) si elle n'y est pas déjà. */
    suspend fun add(id: String) = update { current ->
        if (current.containsKey(id)) current else current + (id to 1)
    }

    /** Fixe le nombre de réalisations ; un nombre ≤ 0 retire la recette. */
    suspend fun setCount(id: String, count: Int) = update { current ->
        if (count <= 0) current - id else current + (id to count)
    }

    suspend fun remove(id: String) = update { current -> current - id }

    /** Vide entièrement la liste (recettes + cochés). */
    suspend fun clear() {
        context.shoppingDataStore.edit { prefs ->
            prefs.remove(selectionsKey)
            prefs.remove(checkedKey)
        }
    }

    suspend fun setChecked(name: String, checked: Boolean) {
        context.shoppingDataStore.edit { prefs ->
            val set = (prefs[checkedKey] ?: emptySet()).toMutableSet()
            if (checked) set.add(name) else set.remove(name)
            prefs[checkedKey] = set
        }
    }

    private suspend fun update(transform: (Map<String, Int>) -> Map<String, Int>) {
        context.shoppingDataStore.edit { prefs ->
            prefs[selectionsKey] = encode(transform(decode(prefs[selectionsKey])))
        }
    }

    private fun decode(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        val list: List<StoredSelection> = gson.fromJson(json, listType) ?: emptyList()
        return list.associate { it.id to it.count } // LinkedHashMap : ordre conservé
    }

    private fun encode(map: Map<String, Int>): String =
        gson.toJson(map.map { StoredSelection(it.key, it.value) })

    private data class StoredSelection(val id: String, val count: Int)
}
