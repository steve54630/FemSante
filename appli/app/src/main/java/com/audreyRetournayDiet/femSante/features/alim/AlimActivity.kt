package com.audreyRetournayDiet.femSante.features.alim

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.viewmodels.alim.ShoppingBadgeViewModel
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
 *
 * Navigation en **add/hide/show** (et non `replace()` sur instances réutilisées) : l'état est
 * conservé et, surtout, chaque onglet reste `STARTED` en arrière-plan — sa collecte de flux
 * continue de tourner, donc la liste de courses reste **réactive** au store même cachée. Le
 * `replace()` sur instance réutilisée ne rafraîchissait pas la vue de façon fiable (cf. le même
 * correctif dans `HomeActivity`).
 */
@AndroidEntryPoint
class AlimActivity : AppCompatActivity() {

    private val badgeViewModel: ShoppingBadgeViewModel by viewModels()

    private lateinit var menu: BottomNavigationView

    private var alimFragment = AlimFragment()
    private var docFragment = RessourceFragment()
    private var shoppingListFragment = ShoppingListFragment()
    private lateinit var activeFragment: Fragment

    private companion object {
        const val TAG_ALIM = "alim"
        const val TAG_PDF = "pdf"
        const val TAG_SHOPPING = "shopping"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alim)

        menu = findViewById(R.id.bottom_navigation_menu)

        initFragments(savedInstanceState)
        setupNavigation()
        observeShoppingBadge()
    }

    private fun initFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, alimFragment, TAG_ALIM)
                .commit()
            activeFragment = alimFragment
        } else {
            // Restauration : récupérer les instances recréées par le FragmentManager.
            (supportFragmentManager.findFragmentByTag(TAG_ALIM) as? AlimFragment)?.let { alimFragment = it }
            (supportFragmentManager.findFragmentByTag(TAG_PDF) as? RessourceFragment)?.let { docFragment = it }
            (supportFragmentManager.findFragmentByTag(TAG_SHOPPING) as? ShoppingListFragment)?.let { shoppingListFragment = it }
            activeFragment = supportFragmentManager.fragments.lastOrNull { !it.isHidden } ?: alimFragment
        }
    }

    private fun setupNavigation() {
        menu.setOnItemSelectedListener { item ->
            val (target, tag) = when (item.itemId) {
                R.id.alim -> alimFragment to TAG_ALIM
                R.id.pdf -> docFragment to TAG_PDF
                R.id.shopping_list -> shoppingListFragment to TAG_SHOPPING
                else -> {
                    Timber.w("Navigation : ID inconnu -> ${item.itemId}")
                    return@setOnItemSelectedListener false
                }
            }
            showFragment(target, tag)
            true
        }
    }

    /**
     * Affiche l'onglet demandé sans détruire les autres (add/hide/show), pour conserver l'état
     * et garder les flux actifs. Les onglets sont ajoutés à la demande.
     */
    private fun showFragment(target: Fragment, tag: String) {
        if (target === activeFragment) return
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)
            if (target.isAdded) show(target) else add(R.id.container, target, tag)
        }.commit()
        activeFragment = target
    }

    /** Revenir à l'onglet Recettes (ex. depuis le CTA de l'état vide de la liste de courses). */
    fun showRecipesTab() {
        menu.selectedItemId = R.id.alim
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
