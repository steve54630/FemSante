package com.audreyRetournayDiet.femSante.domain.calendar

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.domain.main.GeneralFragment
import com.audreyRetournayDiet.femSante.repository.local.DailyRepository
import com.audreyRetournayDiet.femSante.room.database.DatabaseProvider
import com.audreyRetournayDiet.femSante.viewModels.EntryEvent
import com.audreyRetournayDiet.femSante.viewModels.EntryViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class EntryAddActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var container: FrameLayout
    private lateinit var btnSaveEntry: Button

    // Utilisation de la bonne Factory
    private val viewModel: EntryViewModel by viewModels {
        val database = DatabaseProvider.getDatabase(this)
        val repository = DailyRepository(database.dailyDao())
        EntryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar_add)

        tabLayout = findViewById(R.id.tabLayout)
        container = findViewById(R.id.container)
        btnSaveEntry = findViewById(R.id.btnSaveEntry)

        setupNavigation()
        observeEvents()

        btnSaveEntry.setOnClickListener {
            // Ici, récupère l'ID réel de l'utilisateur (via SharedPreferences ou ton UserManager)
            val userId = "user_test"
            viewModel.saveAllDate(userId)
        }
    }

    private fun setupNavigation() {
        val fragments = listOf(
            getString(R.string.calendar_general) to GeneralFragment(),
            getString(R.string.calendar_moral) to PsychologicalFragment(),
            getString(R.string.calendar_symptom) to SymptomsFragment(),
            getString(R.string.calendar_context) to ContextFragment()
        )

        for ((title, _) in fragments) {
            tabLayout.addTab(tabLayout.newTab().setText(title))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.container, fragments[it.position].second)
                        .commit()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // On affiche le premier fragment par défaut
        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragments[0].second)
                .commit()
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            // Utilisation de repeatOnLifecycle(Started) est plus safe pour l'UI
            viewModel.events.collect { event ->
                when (event) {
                    is EntryEvent.Success -> {
                        Toast.makeText(this@EntryAddActivity, getString(R.string.msg_save_success), Toast.LENGTH_SHORT).show()
                        finish() // On ferme l'activité après sauvegarde réussie
                    }
                    is EntryEvent.Error -> {
                        Toast.makeText(this@EntryAddActivity, event.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}