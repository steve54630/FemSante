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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.NothingSelectedSpinnerAdapter
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import com.audreyRetournayDiet.femSante.viewModels.alim.RecipeViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Activité gérant l'affichage détaillé d'une catégorie de recettes.
 *
 * Elle permet à l'utilisatrice de sélectionner une recette via un [Spinner] et de visualiser
 * le PDF correspondant. La logique d'affichage est pilotée par le [RecipeViewModel] via un flux d'état.
 *
 * ### Données attendues en Extra :
 * - `Title` (String) : Titre de la catégorie.
 * - `map` (Serializable) : Mapping entre les noms de recettes et les noms de fichiers.
 * - `FOLDER_PATH` (String) : Chemin vers le dossier des ressources.
 */
class RecetteActivity : AppCompatActivity() {

    @Suppress("UNCHECKED_CAST")
    private val viewModel: RecipeViewModel by viewModels {
        val bundle = intent.extras ?: Bundle()

        // Gestion de la compatibilité Android Tiramisu+ pour la désérialisation
        val recipeMap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("map", HashMap::class.java)
        } else {
            @Suppress("DEPRECATION") intent.getSerializableExtra("map")
        } as? HashMap<String, String> ?: hashMapOf()

        if (recipeMap.isEmpty()) Timber.w("Init : La map des recettes est vide.")

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

        observeUiState(titleView, helpView, recettePdf, spinner)
        observeNavigation()
        setupListeners(spinner, recettePdf)
    }

    /**
     * Observe et applique l'état de l'UI (titre, visibilité des boutons, chargement d'image).
     */
    private fun observeUiState(title: TextView, help: TextView, button: ImageButton, spinner: Spinner) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    title.text = state.title
                    help.visibility = if (state.isRecipeSelected) View.VISIBLE else View.INVISIBLE
                    button.visibility = if (state.isRecipeSelected) View.VISIBLE else View.GONE

                    if (spinner.adapter == null && state.recipeNames.isNotEmpty()) {
                        setupSpinner(spinner, state.recipeNames)
                    }

                    if (state.imageResourceId != 0) {
                        updateRecipeImage(button, state.imageResourceId)
                    }
                }
            }
        }
    }

    /**
     * Gère la navigation vers le lecteur PDF suite à une action du ViewModel.
     */
    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { fullPath ->
                    Timber.i("Navigation : Ouverture du PDF -> $fullPath")
                    val intentTarget = Intent(this@RecetteActivity, PdfActivity::class.java).apply {
                        putExtra("PDF", fullPath)
                    }
                    startActivity(intentTarget)
                }
            }
        }
    }

    /**
     * Initialise les interactions avec le Spinner et le bouton d'ouverture PDF.
     */
    private fun setupListeners(spinner: Spinner, recettePdf: ImageButton) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (spinner.selectedItemId >= 0) {
                    val selectedName = spinner.selectedItem.toString()
                    viewModel.onRecipeSelected(selectedName)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        recettePdf.setOnClickListener {
            viewModel.onOpenPdfClicked()
        }
    }

    /**
     * Charge le drawable de la recette de manière sécurisée.
     */
    private fun updateRecipeImage(button: ImageButton, resId: Int) {
        try {
            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
            button.setImageDrawable(drawable)
        } catch (e: Exception) {
            Timber.e(e, "Échec du chargement du drawable ID: $resId")
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_choice_recette, this)
    }
}