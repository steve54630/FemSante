package com.audreyRetournayDiet.femSante.features

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.PdfNavigationEvent
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.ToolboxViewModel
import kotlinx.coroutines.launch

class ToolboxActivity : AppCompatActivity() {

    private val tag = "ACT_TOOLBOX"
    private val viewModel: ToolboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbox)
        Log.d(tag, "onCreate: Ouverture de la Boîte à Outils")

        setupButtons()
        observeNavigation()
    }

    private fun setupButtons() {
        // Map associant l'ID du bouton à l'ID de l'outil dans le ViewModel
        val toolMap = mapOf(
            R.id.button1 to 1,
            R.id.buttonHistamine to 2,
            R.id.button3 to 3,
            R.id.button4 to 4,
            R.id.button5 to 5,
            R.id.button6 to 6,
            R.id.button7 to 7
        )

        toolMap.forEach { (resId, toolId) ->
            findViewById<Button>(resId).setOnClickListener {
                Log.i(tag, "Action: Clic sur l'outil ID $toolId")
                viewModel.onToolClicked(toolId)
            }
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            // Utilisation de repeatOnLifecycle pour une collecte sécurisée
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is PdfNavigationEvent.NavigateToPdf -> {
                            Log.i(tag, "Navigation: Ouverture du PDF -> ${event.fileName}")
                            val intent = Intent(this@ToolboxActivity, PdfActivity::class.java).apply {
                                putExtra("PDF", event.fileName)
                            }
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }
}