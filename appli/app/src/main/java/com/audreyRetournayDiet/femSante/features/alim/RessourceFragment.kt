package com.audreyRetournayDiet.femSante.features.alim

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.resource.ResourceDocument
import com.audreyRetournayDiet.femSante.repository.local.ResourceContentRepository
import com.audreyRetournayDiet.femSante.shared.viewers.PdfActivity
import timber.log.Timber

/**
 * Écran « Ressources » (nutrition) : fiches PDF (intolérances, sensibilités) issues de
 * `assets/resources.json`, plus un raccourci fixe vers le module Micronutriments.
 *
 * Le raccourci Micronutriments n'est pas un onglet de nav à part : le placer ici évite d'alourdir
 * une navigation déjà imbriquée sur 3 niveaux (Accueil → Découvrir → AlimActivity), et le rend
 * même plus direct qu'avant (une carte au lieu d'un bouton dédié).
 */
class RessourceFragment : Fragment() {

    private val repository by lazy { ResourceContentRepository(requireContext().applicationContext) }
    private val adapter = ResourceCardAdapter(::onRowClicked)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_ressource, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerRessources)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        val rows = repository.getAll().map { ResourceRow.Document(it) } +
            ResourceRow.MicronutrientsShortcut +
            ResourceRow.FodmapShortcut
        adapter.submitList(rows)
    }

    private fun onRowClicked(row: ResourceRow) {
        when (row) {
            is ResourceRow.Document -> openPdf(row.document)
            ResourceRow.MicronutrientsShortcut -> openMicronutrients()
            ResourceRow.FodmapShortcut -> startActivity(Intent(requireContext(), FodmapActivity::class.java))
        }
    }

    private fun openPdf(document: ResourceDocument) {
        Timber.i("Ressources : ouverture du PDF %s", document.pdf)
        val intent = Intent(requireActivity(), PdfActivity::class.java).putExtra("PDF", document.pdf)
        startActivity(intent)
    }

    private fun openMicronutrients() {
        Timber.i("Ressources : ouverture du module Micronutriments")
        startActivity(Intent(requireContext(), MicronutrientsActivity::class.java))
    }
}
