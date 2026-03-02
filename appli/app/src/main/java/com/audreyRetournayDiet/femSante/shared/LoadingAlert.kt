package com.audreyRetournayDiet.femSante.shared

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import com.audreyRetournayDiet.femSante.R

/**
 * Composant utilitaire pour afficher une boîte de dialogue de chargement.
 * * ### Rôle :
 * 1. Empêcher toute interaction utilisateur pendant un traitement lourd (Paiement, API).
 * 2. Afficher un indicateur visuel (Spinner/ProgressBar) via un layout personnalisé.
 */
class LoadingAlert(myActivity: Activity) {

    private var activity: Activity = myActivity
    private lateinit var dialog: Dialog

    /**
     * Construit et affiche la boîte de dialogue.
     * Le paramètre [setCancelable] à false garantit que l'utilisatrice ne peut pas
     * fermer l'alerte en cliquant à côté ou sur le bouton retour.
     */
    @SuppressLint("InflateParams")
    fun start() {
        val builder = AlertDialog.Builder(activity)

        // Utilisation du layout personnalisé pour le design de l'application
        builder.setView(activity.layoutInflater.inflate(R.layout.alert_internet_dialog, null))
        builder.setCancelable(false)

        dialog = builder.create()
        dialog.show()
    }

    /**
     * Ferme proprement la boîte de dialogue une fois le traitement terminé.
     */
    fun close() {
        // Sécurité : on vérifie que le dialogue a bien été initialisé avant de tenter la fermeture
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}