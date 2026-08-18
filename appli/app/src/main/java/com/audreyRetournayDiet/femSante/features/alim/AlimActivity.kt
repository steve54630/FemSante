package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.viewModels.alim.ShoppingBadgeViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activité du module « Bien dans son assiette ».
 *
 * Navigation basse entre les recettes ([AlimFragment]), les ressources PDF ([RessourceFragment])
 * et la liste de courses ([ShoppingListFragment]), dont l'onglet porte un badge du nombre de
 * recettes retenues.
 */
@AndroidEntryPoint
class AlimActivity : AppCompatActivity() {

    private val badgeViewModel: ShoppingBadgeViewModel by viewModels()

    private lateinit var menu: BottomNavigationView

    private val alimFragment = AlimFragment()
    private val docFragment = RessourceFragment()
    private val shoppingListFragment = ShoppingListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        menu = findViewById(R.id.bottom_navigation_menu)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, alimFragment)
                .commit()
        }

        setupNavigation()
        observeShoppingBadge()
    }

    private fun setupNavigation() {
        menu.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.alim -> alimFragment
                R.id.pdf -> docFragment
                R.id.shopping_list -> shoppingListFragment
                else -> {
                    Timber.w("Navigation : ID inconnu -> ${item.itemId}")
                    return@setOnItemSelectedListener false
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            true
        }
    }

    /** Badge de l'onglet « Ma liste » : nombre de recettes retenues (masqué si vide). */
    private fun observeShoppingBadge() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                badgeViewModel.recipeCount.collect { count ->
                    if (count > 0) {
                        menu.getOrCreateBadge(R.id.shopping_list).apply {
                            number = count
                            isVisible = true
                        }
                    } else {
                        menu.removeBadge(R.id.shopping_list)
                    }
                }
            }
        }
    }
}
