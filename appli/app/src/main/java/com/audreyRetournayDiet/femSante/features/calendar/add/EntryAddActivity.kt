package com.audreyRetournayDiet.femSante.features.calendar.add

import android.os.Bundle
import android.util.Log
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
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.viewModels.calendar.event.EntryEvent
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate

class EntryAddActivity : AppCompatActivity() {

    private val tag = "ACT_ENTRY_ADD"
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
        Log.d(tag, "onCreate : Initialisation de l'écran d'ajout/édition")

        // 1. Initialisation de la Map de fragments
        fragments = mapOf(
            R.id.nav_general to GeneralFragment(),
            R.id.nav_symptoms to SymptomsFragment(),
            R.id.nav_psych to PsychologicalFragment(),
            R.id.nav_context to ContextFragment()
        )

        // 2. Récupération des vues
        navBar = findViewById(R.id.tabLayout)
        container = findViewById(R.id.container)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)

        // 3. Traitement des données entrantes (Intent)
        val dateString = intent.getStringExtra("selectedDate")
        val selectedDate = if (dateString != null) {
            LocalDate.parse(dateString)
        } else {
            Log.w(tag, "Aucune date reçue, utilisation de la date du jour par défaut")
            LocalDate.now()
        }

        val isEdit = intent.getBooleanExtra("isEditMode", false)
        viewModel.setEdit(isEdit)
        viewModel.setDate(selectedDate)

        if(isEdit) {
            val store = UserStore(this)
            val user = store.getUser()
            id = intent.getLongExtra("ID", 0)

            Log.i(tag, "Mode ÉDITION activé pour l'ID : $id (Utilisateur : ${user?.id})")

            if (user?.id != null) {
                viewModel.loadExistingData(user.id, id!!)
            } else {
                Log.e(tag, "Impossible de charger les données : UserID introuvable")
            }
        } else {
            Log.i(tag, "Mode CRÉATION activé pour la date : $selectedDate")
        }

        setupNavigation()
        observeEvents()

        btnSaveEntry.setOnClickListener {
            val store = UserStore(this)
            val userId = store.getUser()?.id
            if (userId != null) {
                Log.d(tag, "Action : Clic sur Sauvegarder (Mode: ${if(isEdit) "Update" else "Insert"})")
                viewModel.saveAllData(userId, id)
            } else {
                Log.e(tag, "Échec de sauvegarde : UserID est null")
                Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigation() {
        navBar.setOnItemSelectedListener { item ->
            val fragment = fragments[item.itemId]
            if (fragment != null) {
                Log.v(tag, "Navigation : Changement vers l'onglet ${item.title}")
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container, fragment)
                    .commit()
                true
            } else {
                Log.w(tag, "Navigation : Aucun fragment associé à l'ID ${item.itemId}")
                false
            }
        }

        // Chargement du premier fragment si vide
        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            navBar.selectedItemId = R.id.nav_general
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is EntryEvent.Success -> {
                            Log.i(tag, "Événement : Sauvegarde réussie. Fermeture de l'activité.")
                            Toast.makeText(this@EntryAddActivity, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is EntryEvent.Error -> {
                            Log.e(tag, "Événement : Erreur lors de la sauvegarde - ${event.message}")
                            Toast.makeText(this@EntryAddActivity, event.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}