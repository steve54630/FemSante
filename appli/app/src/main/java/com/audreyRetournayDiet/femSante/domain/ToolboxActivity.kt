package com.audreyRetournayDiet.femSante.domain

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.entities.ToolboxNavigationEvent
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.ToolboxViewModel
import kotlinx.coroutines.launch

class ToolboxActivity : AppCompatActivity() {

    private val viewModel: ToolboxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbox)

        setupButtons()
        observeNavigation()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.button1).setOnClickListener { viewModel.onToolClicked(1) }
        findViewById<Button>(R.id.buttonHistamine).setOnClickListener { viewModel.onToolClicked(2) }
        findViewById<Button>(R.id.button3).setOnClickListener { viewModel.onToolClicked(3) }
        findViewById<Button>(R.id.button4).setOnClickListener { viewModel.onToolClicked(4) }
        findViewById<Button>(R.id.button5).setOnClickListener { viewModel.onToolClicked(5) }
        findViewById<Button>(R.id.button6).setOnClickListener { viewModel.onToolClicked(6) }
        findViewById<Button>(R.id.button7).setOnClickListener { viewModel.onToolClicked(7) }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is ToolboxNavigationEvent.NavigateToPdf -> {
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