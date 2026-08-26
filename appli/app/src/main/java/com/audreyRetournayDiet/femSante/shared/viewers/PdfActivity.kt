package com.audreyRetournayDiet.femSante.shared.viewers

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.shared.LoadingAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

/**
 * Affiche un PDF embarqué dans les assets (CGV, mentions légales) via l'API native
 * [PdfRenderer] — chaque page est rendue en bitmap et affichée dans une liste verticale.
 *
 * Remplace l'ancienne dépendance com.github.mhiew:android-pdf-viewer (binaires natifs
 * pdfium non alignés 16 Ko, incompatibles avec les exigences Play Store post 01/11/2025).
 *
 * FLAG_SECURE reste actif pendant toute la lecture pour empêcher les captures d'écran.
 */
class PdfActivity : AppCompatActivity() {

    private lateinit var alert: LoadingAlert
    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
        window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

        alert = LoadingAlert(this)
        val recyclerView = findViewById<RecyclerView>(R.id.pdfRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val assetName = intent.getStringExtra("PDF")
        if (assetName == null) {
            Timber.e("PdfActivity : aucun nom de fichier PDF fourni")
            Toast.makeText(this, "Document introuvable", Toast.LENGTH_SHORT).show()
            return
        }

        alert.start()
        lifecycleScope.launch {
            try {
                val pages = withContext(Dispatchers.IO) { renderPages(assetName) }
                recyclerView.adapter = PdfPageAdapter(pages)
            } catch (e: Exception) {
                Timber.e(e, "Erreur lors du rendu du PDF : $assetName")
                Toast.makeText(this@PdfActivity, "Impossible d'afficher le document", Toast.LENGTH_SHORT).show()
            } finally {
                alert.close()
            }
        }
    }

    /**
     * Copie l'asset vers le cache (PdfRenderer exige un [ParcelFileDescriptor] sur un vrai
     * fichier), puis rend chaque page en bitmap à la largeur de l'écran.
     */
    private fun renderPages(assetName: String): List<Bitmap> {
        val cacheFile = File(cacheDir, assetName)
        assets.open(assetName).use { input ->
            cacheFile.outputStream().use { output -> input.copyTo(output) }
        }

        val pfd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
        fileDescriptor = pfd
        val pdfRenderer = PdfRenderer(pfd)
        renderer = pdfRenderer

        val displayWidth = resources.displayMetrics.widthPixels
        return (0 until pdfRenderer.pageCount).map { index ->
            pdfRenderer.openPage(index).use { page ->
                val scale = displayWidth.toFloat() / page.width
                val bitmap = Bitmap.createBitmap(displayWidth, (page.height * scale).toInt(), Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    override fun onDestroy() {
        window.clearFlags(LayoutParams.FLAG_SECURE)
        renderer?.close()
        fileDescriptor?.close()
        super.onDestroy()
    }

    private class PdfPageAdapter(private val pages: List<Bitmap>) : RecyclerView.Adapter<PdfPageAdapter.PageViewHolder>() {

        class PageViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_pdf_page, parent, false) as ImageView
            return PageViewHolder(view)
        }

        override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
            holder.imageView.setImageBitmap(pages[position])
        }

        override fun getItemCount(): Int = pages.size
    }
}
