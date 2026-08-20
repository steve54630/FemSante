package com.audreyRetournayDiet.femSante.features.toolbox

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxAdvice
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxItem
import com.audreyRetournayDiet.femSante.data.toolbox.ToolboxProtocol
import com.audreyRetournayDiet.femSante.viewmodels.ToolboxFileDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ToolboxFileDetailActivity : AppCompatActivity() {

    private val viewModel: ToolboxFileDetailViewModel by viewModels()

    companion object {
        /** Clé de l'identifiant de fiche passé à l'écran détail (lue par le ViewModel). */
        const val EXTRA_FILE_ID = "EXTRA_FILE_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toolbox_file)

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fileState.collect { file ->
                    file?.let { bindUi(it) }
                }
            }
        }
    }

    private fun bindUi(file: ToolboxAdvice) {
        findViewById<TextView>(R.id.tvCategory).text = file.category.label
        findViewById<TextView>(R.id.tvTitle).text = file.title
        findViewById<TextView>(R.id.tvAuthor).text = "Par ${file.author.name} — ${file.author.role}"
        findViewById<TextView>(R.id.tvSummary).text = file.summary

        // 1. Ingrédients / Éléments
        val llItems = findViewById<LinearLayout>(R.id.llItems)
        llItems.removeAllViews()
        file.items.forEach { item ->
            llItems.addView(createItemView(item))
        }

        // 2. Protocoles
        val llProtocols = findViewById<LinearLayout>(R.id.llProtocols)
        llProtocols.removeAllViews()
        file.protocols.forEach { protocol ->
            llProtocols.addView(createProtocolView(protocol))
        }

        // 3. Précautions (Masqué si vide)
        val cardPrecautions = findViewById<View>(R.id.cardPrecautions)
        val llPrecautions = findViewById<LinearLayout>(R.id.llPrecautions)
        llPrecautions.removeAllViews()

        if (file.precautions.isNotEmpty()) {
            cardPrecautions.visibility = View.VISIBLE
            file.precautions.forEach { precaution ->
                val textView = TextView(this).apply {
                    text = "• $precaution"
                    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
                    setPadding(0, 4, 0, 4)
                }
                llPrecautions.addView(textView)
            }
        } else {
            cardPrecautions.visibility = View.GONE
        }
    }

    private fun createItemView(item: ToolboxItem): View {
        val view = layoutInflater.inflate(R.layout.item_toolbox_file_element, null)
        val tvName = view.findViewById<TextView>(R.id.tvItemName)
        val tvDetails = view.findViewById<TextView>(R.id.tvItemDetails)

        val nameText = if (!item.latinName.isNullOrBlank()) "${item.name} (${item.latinName})" else item.name
        tvName.text = nameText

        val details = mutableListOf<String>()
        if (item.properties.isNotEmpty()) details.add("Propriétés : ${item.properties.joinToString(", ")}")
        if (item.contraindications.isNotEmpty()) details.add("Contre-indications : ${item.contraindications.joinToString(", ")}")

        tvDetails.text = details.joinToString("\n")
        tvDetails.visibility = if (details.isNotEmpty()) View.VISIBLE else View.GONE

        return view
    }

    private fun createProtocolView(protocol: ToolboxProtocol): View {
        val view = layoutInflater.inflate(R.layout.item_toolbox_file_protocol, null)
        val tvIndication = view.findViewById<TextView>(R.id.tvIndication)
        val tvSite = view.findViewById<TextView>(R.id.tvSite)
        val tvSteps = view.findViewById<TextView>(R.id.tvSteps)

        tvIndication.text = protocol.indication
        tvSite.text = "Zone : ${protocol.applicationSite}"
        tvSteps.text = protocol.steps.mapIndexed { index, step -> "${index + 1}. $step" }.joinToString("\n")

        return view
    }
}