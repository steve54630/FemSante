package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewmodels.calendar.EntryViewModel
import com.audreyRetournayDiet.femSante.viewmodels.calendar.event.EntryEvent
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

/**
 * Activité de gestion des entrées quotidiennes du calendrier (Ajout et Modification).
 * * Cette activité héberge quatre fragments thématiques via une [BottomNavigationView] :
 * - [SymptomsFragment] : Corps & douleur (carte du corps, nausée, notes).
 * - [PsychologicalFragment] : Moral & sommeil (moral, causes, fatigue, gratitude).
 * - [ContextFragment] : Mode de vie (activité, alimentation, médicaments).
 * - [MeasuresFragment] : Mesures (poids, tours).
 * * ### Fonctionnement :
 * L'activité initialise un [EntryViewModel] partagé par tous les fragments. La navigation se fait
 * en **add/hide/show** (et non `replace()` sur instances réutilisées) : chaque onglet conserve sa
 * vue et sa saisie en cours quand on passe à un autre.
 */
@AndroidEntryPoint
class EntryAddActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var container: FrameLayout
    private lateinit var btnSaveEntry: Button

    private lateinit var fragments: Map<Int, Fragment>
    private lateinit var activeFragment: Fragment
    private var id: Long? = null

    // Hilt injecte le ViewModel et son DailyRepository.
    private val viewModel: EntryViewModel by viewModels()

    private companion object {
        val TAGS = mapOf(
            R.id.nav_symptoms to "entry_symptoms",
            R.id.nav_psych to "entry_psych",
            R.id.nav_context to "entry_context",
            R.id.nav_measures to "entry_measures"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_add)

        setupViews()
        initializeFragments(savedInstanceState)
        handleIntentData()
        setupNavigation()
        observeEvents()
        setupSaveListener()
    }

    private fun setupViews() {
        navBar = findViewById(R.id.tabLayout)
        container = findViewById(R.id.container)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)
    }

    /** Fabrique un fragment neuf pour un onglet donné. */
    private fun newFragment(menuId: Int): Fragment = when (menuId) {
        R.id.nav_symptoms -> SymptomsFragment()
        R.id.nav_psych -> PsychologicalFragment()
        R.id.nav_context -> ContextFragment()
        else -> MeasuresFragment()
    }

    /**
     * Prépare les instances de fragments. Au premier lancement, ajoute l'onglet initial ; après
     * une rotation, récupère les instances déjà recréées par le FragmentManager (via leur tag).
     */
    private fun initializeFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            fragments = TAGS.mapValues { (menuId, _) -> newFragment(menuId) }
            val first = fragments.getValue(R.id.nav_symptoms)
            supportFragmentManager.beginTransaction()
                .add(R.id.container, first, TAGS.getValue(R.id.nav_symptoms))
                .commit()
            activeFragment = first
        } else {
            fragments = TAGS.mapValues { (menuId, tag) ->
                supportFragmentManager.findFragmentByTag(tag) ?: newFragment(menuId)
            }
            activeFragment = supportFragmentManager.fragments.lastOrNull { !it.isHidden }
                ?: fragments.getValue(R.id.nav_symptoms)
        }
    }

    /**
     * Analyse les données reçues via l'Intent pour configurer le ViewModel.
     * Gère la récupération de la date sélectionnée et le chargement des données existantes en édition.
     */
    private fun handleIntentData() {
        val dateString = intent.getStringExtra("selectedDate")
        val selectedDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

        val isEdit = intent.getBooleanExtra("isEditMode", false)
        viewModel.setEdit(isEdit)
        viewModel.setDate(selectedDate)

        if (isEdit) {
            id = intent.getLongExtra("ID", 0)
            val userId = UserStore(this).getUser()?.id

            if (userId != null) {
                Timber.i("Mode ÉDITION : Chargement de l'ID $id")
                viewModel.loadExistingData(userId, id!!)
            } else {
                Timber.e("Erreur : UserID introuvable pour l'édition")
            }
        } else {
            Timber.i("Mode CRÉATION : Date $selectedDate")
        }
    }

    /**
     * Configure la navigation entre les fragments (add/hide/show + fondu) et synchronise la
     * surbrillance de la barre avec l'onglet actif.
     */
    private fun setupNavigation() {
        navBar.setOnItemSelectedListener { item ->
            val target = fragments[item.itemId] ?: return@setOnItemSelectedListener false
            showFragment(target, TAGS.getValue(item.itemId))
            true
        }
        // Surbrillance alignée sur l'onglet actif (la navigation associée est un no-op).
        fragments.entries.firstOrNull { it.value === activeFragment }?.let {
            navBar.selectedItemId = it.key
        }
    }

    /** Affiche l'onglet demandé sans détruire les autres (conserve vue + saisie en cours). */
    private fun showFragment(target: Fragment, tag: String) {
        if (target === activeFragment) return
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .apply {
                hide(activeFragment)
                if (target.isAdded) show(target) else add(R.id.container, target, tag)
            }
            .commit()
        activeFragment = target
    }

    /**
     * Observe les événements de succès ou d'erreur émis par le ViewModel.
     * En cas de succès, l'activité se ferme pour revenir au calendrier.
     */
    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is EntryEvent.Success -> {
                            Toast.makeText(this@EntryAddActivity, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is EntryEvent.Error -> {
                            Timber.e("Erreur de sauvegarde : ${event.message}")
                            Toast.makeText(this@EntryAddActivity, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Gère le clic sur le bouton de sauvegarde globale.
     */
    private fun setupSaveListener() {
        btnSaveEntry.setOnClickListener {
            val userId = UserStore(this).getUser()?.id
            if (userId != null) {
                viewModel.saveAllData(userId, id)
            } else {
                Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
