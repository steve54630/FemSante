package com.audreyRetournayDiet.femSante.data.report

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Écriture locale + partage du récap médical.
 *
 * Garde-fous RGPD :
 * - Le PDF est écrit dans le **cache privé** de l'app (`cacheDir/reports`), pas dans un
 *   stockage partagé persistant.
 * - Il n'est exposé que via [FileProvider] (URI temporaire, permission de lecture accordée
 *   au seul destinataire choisi), jamais par un chemin `file://` en clair.
 * - Le partage passe par le **sélecteur système** : c'est l'utilisatrice qui décide si, et
 *   avec qui, elle partage. Rien n'est envoyé automatiquement ni au backend.
 */
object MedicalReportSharer {

    private val fileDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /** Dossier privé dédié aux récaps (créé au besoin). */
    private fun reportsDir(context: Context): File =
        File(context.cacheDir, "reports").apply { mkdirs() }

    /**
     * Fichier cible pour un nouveau récap. Le nom est daté et lisible pour l'utilisatrice
     * une fois partagé. On repart d'un dossier vidé pour ne pas accumuler d'anciens PDF.
     */
    fun newReportFile(context: Context): File {
        val dir = reportsDir(context)
        dir.listFiles()?.forEach { it.delete() }
        return File(dir, "recap-suivi-${LocalDate.now().format(fileDateFormat)}.pdf")
    }

    /** Lance le sélecteur de partage système pour le PDF donné. */
    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}
