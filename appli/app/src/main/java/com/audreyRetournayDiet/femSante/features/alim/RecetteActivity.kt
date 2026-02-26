package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.alim.RecetteViewModel
import kotlinx.coroutines.launch

class RecetteActivity : AppCompatActivity() {

    @Suppress("UNCHECKED_CAST")
    private val viewModel: RecetteViewModel by viewModels {
        val bundle = intent.extras!!
        val recipeMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map")
        }
        RecetteViewModel.Factory(
            title = bundle.getString("Title") ?: "",
            map = recipeMap as HashMap<String, String>,
            path = intent.getStringExtra("FOLDER_PATH") ?: "",
            context = this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recette)

        val recettePdf = findViewById<ImageButton>(R.id.buttonRecette)
        val titleView = findViewById<TextView>(R.id.textViewTitre)
        val spinner = findViewById<Spinner>(R.id.spinnerMeditation)
        val helpView = findViewById<TextView>(R.id.textHelp)

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                titleView.text = state.title
                helpView.visibility = if (state.isRecipeSelected) View.VISIBLE else View.INVISIBLE
                recettePdf.visibility = if (state.isRecipeSelected) View.VISIBLE else View.GONE

                if (spinner.adapter == null) {
                    setupSpinner(spinner, state.recipeNames)
                }

                val resId = state.imageResourceId
                if (resId != 0) {
                    val drawable = ResourcesCompat.getDrawable(resources, resId, null)
                    recettePdf.setImageDrawable(drawable)
                }
            }
        }

        // Navigation
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { fullPath ->
                val intentTarget = Intent(this@RecetteActivity, PdfActivity::class.java)
                intentTarget.putExtra("PDF", fullPath)
                startActivity(intentTarget)
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinner.selectedItemId >= 0) {
                    viewModel.onRecipeSelected(spinner.selectedItem.toString())
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        recettePdf.setOnClickListener { viewModel.onOpenPdfClicked() }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_recette, this)
    }
}