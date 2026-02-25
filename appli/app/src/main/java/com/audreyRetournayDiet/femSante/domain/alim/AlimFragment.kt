package com.audreyRetournayDiet.femSante.domain.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.audreyRetournayDiet.femSante.R

class AlimFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_alim, container, false)

        // Configuration des boutons
        setupButton(view, R.id.buttonBreakfirst, "breakfast", "Petit-déjeuner")
        setupButton(view, R.id.buttonEntry, "starters", "Entrées")
        setupButton(view, R.id.buttonPlat, "main_courses", "Plats")
        setupButton(view, R.id.buttonEBook, "desserts", "Desserts")

        return view
    }

    private fun setupButton(view: View, buttonId: Int, folderName: String, displayTitle: String) {
        view.findViewById<Button>(buttonId).setOnClickListener {
            // 1. On scanne le dossier correspondant dans les assets
            val recipesMap = scanAssetsFolder(folderName)

            // 2. On lance l'activité avec les données dynamiques
            val intent = Intent(activity, RecetteActivity::class.java).apply {
                putExtra("Title", displayTitle)
                putExtra("map", recipesMap) // Envoi de la HashMap générée
                putExtra("FOLDER_PATH", folderName) // Utile pour charger le PDF plus tard
            }
            startActivity(intent)
        }
    }

    private fun scanAssetsFolder(path: String): HashMap<String, String> {
        val map = HashMap<String, String>()
        try {
            val files = requireContext().assets.list(path) ?: emptyArray()

            for (fileName in files) {
                if (fileName.endsWith(".pdf")) {
                    // Clé : nom_du_fichier (ex: salade_ete)
                    val key = fileName.substringBeforeLast(".")

                    // Valeur : Nom propre (ex: Salade ete)
                    val value = key.replace("_", " ")
                        .replaceFirstChar { it.uppercase() }

                    map[key] = value
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }
}