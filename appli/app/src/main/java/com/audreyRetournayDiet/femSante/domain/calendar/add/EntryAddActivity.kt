package com.audreyRetournayDiet.femSante.domain.calendar.add

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.UserStore
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryEvent
import com.audreyRetournayDiet.femSante.viewModels.calendar.EntryViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.time.LocalDate

class EntryAddActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView
    private lateinit var container: FrameLayout
    private lateinit var btnSaveEntry: Button

    // Typage explicite de la Map pour éviter le "Argument type mismatch"
    private lateinit var fragments: Map<Int, Fragment>

    private val viewModel: EntryViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        EntryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_add)

        // 1. Initialisation de la Map de fragments (typage forcé à Fragment)
        val map = HashMap<Int, Fragment>()
        map[R.id.nav_general] = GeneralFragment() as Fragment
        map[R.id.nav_symptoms] = SymptomsFragment() as Fragment
        map[R.id.nav_psych] = PsychologicalFragment() as Fragment
        map[R.id.nav_context] = ContextFragment() as Fragment
        fragments = map

        // 2. Récupération des vues (Vérifie que l'ID est bien bottomNavigation)
        navBar = findViewById(R.id.tabLayout)
        container = findViewById(R.id.container)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)

        val dateString = intent.getStringExtra("selectedDate")
        val selectedDate = if (dateString != null) LocalDate.parse(dateString) else LocalDate.now()

        val isEdit = intent.getBooleanExtra("isEditMode", false);

        if(isEdit) {
            val store = UserStore(this)
            val userId = store.getUser()?.id
            val id = intent.getLongExtra("ID", 0)

            if (userId != null) viewModel.loadExistingData(userId, id)
        }

        viewModel.setDate(selectedDate)

        setupNavigation()
        observeEvents()

        btnSaveEntry.setOnClickListener {
            val store = UserStore(this)
            val userId = store.getUser()?.id
            if (userId != null) {
                viewModel.saveAllData(userId)
            } else {
                Log.e("EntryAdd", "Impossible de sauvegarder : UserID est null")
                Toast.makeText(this, "Erreur d'authentification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigation() {
        navBar.setOnItemSelectedListener { item ->
            val fragment = fragments[item.itemId]
            if (fragment != null) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.container, fragment)
                    .commit()
                true
            } else {
                false
            }
        }

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            navBar.selectedItemId = R.id.nav_general
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is EntryEvent.Success -> {
                        Toast.makeText(this@EntryAddActivity, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is EntryEvent.Error -> {
                        Toast.makeText(this@EntryAddActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}