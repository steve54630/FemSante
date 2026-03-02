package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.alim.RecipeViewModel
import kotlinx.coroutines.launch

class RecetteActivity : AppCompatActivity() {

    private val tag = "ACT_RECETTE"

    @Suppress("UNCHECKED_CAST")
    private val viewModel: RecipeViewModel by viewModels {
        Log.d(tag, "Récupération des extras pour l'initialisation du ViewModel")
        val bundle = intent.extras ?: Bundle()

        // Gestion de la compatibilité pour la récupération de la Map
        val recipeMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map")
        } as? HashMap<String, String> ?: hashMapOf()

        if (recipeMap.isEmpty()) Log.w(tag, "Attention : La map des recettes reçue est vide.")

        RecipeViewModel.Factory(
            title = bundle.getString("Title") ?: "Recettes",
            map = recipeMap,
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

        // Observation de l'état UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    Log.v(tag, "Mise à jour UI : Recette sélectionnée = ${state.isRecipeSelected}")

                    titleView.text = state.title
                    helpView.visibility = if (state.isRecipeSelected) View.VISIBLE else View.INVISIBLE
                    recettePdf.visibility = if (state.isRecipeSelected) View.VISIBLE else View.GONE

                    if (spinner.adapter == null && state.recipeNames.isNotEmpty()) {
                        Log.d(tag, "Initialisation du Spinner avec ${state.recipeNames.size} recettes")
                        setupSpinner(spinner, state.recipeNames)
                    }

                    val resId = state.imageResourceId
                    if (resId != 0) {
                        try {
                            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
                            recettePdf.setImageDrawable(drawable)
                        } catch (e: Exception) {
                            Log.e(tag, "Erreur lors du chargement du drawable (ID: $resId)", e)
                        }
                    }
                }
            }
        }

        // Observation de la Navigation vers le PDF
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { fullPath ->
                    Log.i(tag, "Navigation : Ouverture du PDF -> $fullPath")
                    val intentTarget = Intent(this@RecetteActivity, PdfActivity::class.java)
                    intentTarget.putExtra("PDF", fullPath)
                    startActivity(intentTarget)
                }
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (spinner.selectedItemId >= 0) {
                    val selectedName = spinner.selectedItem.toString()
                    Log.d(tag, "Spinner : Sélection de '$selectedName'")
                    viewModel.onRecipeSelected(selectedName)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        recettePdf.setOnClickListener {
            Log.d(tag, "Clic sur l'image de la recette pour ouvrir le PDF")
            viewModel.onOpenPdfClicked()
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_recette, this)
    }
}