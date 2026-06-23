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
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.shared.UserStore
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.audreyRetournayDiet.femSante.viewModels.calendar.event.EntryEvent
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate

/**
 * Activité de gestion des entrées quotidiennes du calendrier (Ajout et Modification).
 * * Cette activité héberge quatre fragments thématiques via une [BottomNavigationView] :
 * - [GeneralFragment] : Humeur et flux.
 * - [SymptomsFragment] : Douleurs et symptômes physiques.
 * - [PsychologicalFragment] : État émotionnel.
 * - [ContextFragment] : Activité physique et notes.
 * * ### Fonctionnement :
 * L'activité initialise un [EntryViewModel] partagé par tous les fragments. Elle gère
 * l'ID de l'entrée en cas d'édition et déclenche la sauvegarde globale des données.
 * * @property fragments Map associant les IDs du menu aux instances de fragments.
 * @property id ID de l'entrée en base de données (uniquement en mode édition).
 */
class EntryAddActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var container: FrameLayout
    private lateinit var btnSaveEntry: Button

    private lateinit var fragments: Map<Int, Fragment>
    private var id : Long? = null

    private val viewModel: EntryViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        EntryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_add)

        initializeFragments()
        setupViews()
        handleIntentData()
        setupNavigation()
        observeEvents()
        setupSaveListener()
    }

    /**
     * Prépare les instances de fragments pour la navigation.
     */
    private fun initializeFragments() {
        fragments = mapOf(
            R.id.nav_general to GeneralFragment(),
            R.id.nav_symptoms to SymptomsFragment(),
            R.id.nav_psych to PsychologicalFragment(),
            R.id.nav_context to ContextFragment()
        )
    }

    private fun setupViews() {
        navBar = findViewById(R.id.tabLayout)
        container = findViewById(R.id.container)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)
    }

    /**
     * Analyse les données reçues via l'Intent pour configurer le ViewModel.
     * Gère la récupération de la date sélectionnée et le chargement des données existantes si [isEditMode] est vrai.
     */
    private fun handleIntentData() {
        val dateString = intent.getStringExtra("selectedDate")
        val selectedDate = dateString?.let { LocalDate.parse(it) } ?: LocalDate.now()

        val isEdit = intent.getBooleanExtra("isEditMode", false)
        viewModel.setEdit(isEdit)
        viewModel.setDate(selectedDate)

        if(isEdit) {
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
     * Configure la navigation entre les fragments avec une animation de fondu.
     */
    private fun setupNavigation() {
        navBar.setOnItemSelectedListener { item ->
            fragments[item.itemId]?.let { fragment ->
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container, fragment)
                    .commit()
                true
            } ?: false
        }

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            navBar.selectedItemId = R.id.nav_general
        }
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