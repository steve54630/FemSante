package com.audreyRetournayDiet.femSante.data.report

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.audreyRetournayDiet.femSante.room.type.CycleProfile
import com.audreyRetournayDiet.femSante.room.type.PainZone
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Génère le PDF du récap médical à partir d'un [MedicalReport] déjà agrégé.
 *
 * Utilise [PdfDocument] natif (aucune dépendance externe). Le rendu est volontairement
 * **sobre et clinique** (noir sur blanc), distinct de la charte « soleil » de l'app : le
 * document est destiné à un professionnel de santé, pas à l'usage quotidien.
 *
 * Aucune interprétation médicale : uniquement la restitution factuelle des saisies.
 * Le fichier est écrit en local ; son partage éventuel reste une action explicite de
 * l'utilisatrice (cf. flux de partage). Rien n'est envoyé au backend.
 */
class MedicalReportPdfGenerator {

    /**
     * Écrit le PDF dans [outputFile] et le retourne. Le dossier parent est créé au besoin.
     */
    fun generate(report: MedicalReport, format: ReportFormat, outputFile: File): File {
        outputFile.parentFile?.mkdirs()

        val document = PdfDocument()
        val writer = PageWriter(document)

        drawHeader(writer, report)
        drawCycleSection(writer, report.cycle)
        drawSymptomsSection(writer, report.symptoms)
        drawSleepSection(writer, report.sleep)
        drawMeasuresSection(writer, report.measurements)
        if (format == ReportFormat.SUMMARY_AND_JOURNAL) {
            drawJournalSection(writer, report.days)
        }

        writer.finish()

        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()
        Timber.i("PDF récap médical généré : ${outputFile.name} (${outputFile.length()} octets)")
        return outputFile
    }

    // --- Sections ---

    private fun drawHeader(w: PageWriter, report: MedicalReport) {
        w.text("Récap de suivi", titlePaint)
        w.gap(4f)
        w.text("Période du ${report.from.format(dateFormat)} au ${report.to.format(dateFormat)}", subtitlePaint)
        w.text("Généré le ${LocalDate.now().format(dateFormat)}", subtitlePaint)
        w.gap(16f)
    }

    private fun drawCycleSection(w: PageWriter, cycle: CycleSummary) {
        w.section("Cycle")
        w.field("Profil déclaré", cycleProfileLabel(cycle.profile))
        w.field("Jours de règles enregistrés", cycle.periodDaysCount.toString())
        w.field("Débuts de règles", cycle.periodStartsCount.toString())
        w.field(
            "Longueur moyenne du cycle",
            cycle.averageCycleLength?.let { "$it jours" } ?: "non calculable (moins de 2 débuts)"
        )
        w.gap(14f)
    }

    private fun drawSymptomsSection(w: PageWriter, s: SymptomSummary) {
        w.section("Symptômes")
        w.field("Jours de journal renseignés", s.loggedDaysCount.toString())
        w.field("Douleur moyenne", s.averagePain?.let { String.format(Locale.FRANCE, "%.1f/10", it) } ?: "—")
        w.field("Douleur maximale", s.maxPain?.let { "$it/10" } ?: "—")
        w.field(
            "Zones les plus fréquentes",
            if (s.topPainZones.isEmpty()) "—"
            else s.topPainZones.joinToString(", ") { (zone, count) -> "${painZoneLabel(zone)} ($count j)" }
        )
        w.field("Jours avec nausées", s.nauseaDaysCount.toString())
        w.field("Jours avec prise de médicament", s.medicationDaysCount.toString())
        w.gap(14f)
    }

    private fun drawSleepSection(w: PageWriter, sleep: SleepSummary) {
        w.section("Sommeil")
        w.field("Nuits renseignées", sleep.loggedNights.toString())
        w.field("Durée moyenne de sommeil", sleep.averageDurationMinutes?.let { formatDuration(it) } ?: "—")
        w.gap(14f)
    }

    private fun drawMeasuresSection(w: PageWriter, m: MeasurementSummary) {
        w.section("Mesures (évolution)")
        if (m.trends.isEmpty()) {
            w.field("Mesures", "aucune mesure sur la période")
        } else {
            m.trends.forEach { t ->
                val unit = metricUnit(t.metric)
                val value = "${num(t.firstValue)} $unit (${t.firstDate.format(dateFormat)}) → " +
                    "${num(t.lastValue)} $unit (${t.lastDate.format(dateFormat)}), ${deltaText(t.lastValue - t.firstValue, unit)}"
                w.field(metricLabel(t.metric), value)
                // Une courbe n'a de sens qu'à partir de 3 points (2 points = segment droit).
                if (t.points.size >= 3) w.sparkline(t.points)
            }
        }
        w.gap(14f)
    }

    private fun drawJournalSection(w: PageWriter, days: List<DayLine>) {
        w.section("Journal jour par jour")
        if (days.isEmpty()) {
            w.wrapped("Aucune saisie sur la période.", valuePaint)
            return
        }
        for (day in days) {
            drawDayLine(w, day)
        }
    }

    private fun drawDayLine(w: PageWriter, day: DayLine) {
        val parts = mutableListOf<String>()
        day.painLevel?.let { parts.add("douleur ${it}/10") }
        if (day.zones.isNotEmpty()) parts.add(day.zones.joinToString(", ") { painZoneLabel(it) })
        if (day.isPeriod) parts.add("règles")
        if (day.nausea) parts.add("nausées")
        day.sleepDurationMinutes?.let { parts.add("sommeil ${formatDuration(it)}") }
        val summary = if (parts.isEmpty()) "—" else parts.joinToString(" · ")

        // On garde une ligne (date + résumé) solidaire de ses notes éventuelles.
        val notesLines = day.notes?.takeIf { it.isNotBlank() }
            ?.let { wrapLines("Notes : $it", notesPaint, CONTENT_WIDTH - 16f) } ?: emptyList()
        w.ensureSpace(LINE_HEIGHT + notesLines.size * NOTE_LINE_HEIGHT + 6f)

        w.text("${day.date.format(dayFormat)} — $summary", valuePaint)
        notesLines.forEach { w.indentedText(it, notesPaint, 16f) }
        w.gap(6f)
    }

    // --- Libellés français (restent alignés sur strings.xml : zone_*, cycle_profile_*) ---

    private fun cycleProfileLabel(profile: CycleProfile): String = when (profile) {
        CycleProfile.REGULIER -> "Réguliers"
        CycleProfile.IRREGULIER -> "Irréguliers"
        CycleProfile.ABSENT_OU_PILULE -> "Absents ou sous pilule"
    }

    private fun painZoneLabel(zone: PainZone): String = when (zone) {
        PainZone.BASSIN -> "Bassin"
        PainZone.LOMBAIRES -> "Lombaires"
        PainZone.SEINS -> "Seins"
        PainZone.TETE -> "Tête"
        PainZone.ABDOMEN -> "Abdomen"
        PainZone.BRAS -> "Bras"
        PainZone.CUISSES -> "Cuisses"
        PainZone.HAUT_DOS -> "Haut du dos"
        PainZone.JAMBES -> "Jambes"
        PainZone.AUTRE -> "Autre"
    }

    private fun formatDuration(minutes: Int): String =
        "${minutes / 60} h ${String.format(Locale.FRANCE, "%02d", minutes % 60)}"

    private fun metricLabel(metric: MeasurementMetric): String = when (metric) {
        MeasurementMetric.WEIGHT -> "Poids"
        MeasurementMetric.WAIST -> "Tour de taille"
        MeasurementMetric.HIPS -> "Tour de hanches"
        MeasurementMetric.THIGHS -> "Tour de cuisses"
        MeasurementMetric.CHEST -> "Tour de poitrine"
        MeasurementMetric.ARMS -> "Tour de bras"
    }

    private fun metricUnit(metric: MeasurementMetric): String =
        if (metric == MeasurementMetric.WEIGHT) "kg" else "cm"

    private fun num(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.FRANCE, "%.1f", value)

    /** Variation lisible : "+2 kg", "-3 cm", ou "stable". */
    private fun deltaText(delta: Double, unit: String): String {
        if (delta == 0.0) return "stable"
        val magnitude = if (delta < 0) -delta else delta
        val sign = if (delta > 0) "+" else "-"
        return "$sign${num(magnitude)} $unit"
    }

    // --- Word-wrap manuel (mesuré au Paint) ---

    private fun wrapLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }

    /**
     * Curseur de dessin gérant la pagination : dépose le texte de haut en bas et ouvre une
     * nouvelle page dès que l'espace manque, avec un pied de page sur chaque feuille.
     */
    private inner class PageWriter(private val document: PdfDocument) {
        private var pageNumber = 0
        private lateinit var page: PdfDocument.Page
        private lateinit var canvas: Canvas
        private var y = 0f

        init { startNewPage() }

        private fun startNewPage() {
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            page = document.startPage(info)
            canvas = page.canvas
            y = MARGIN_TOP
            drawBrandBand()
        }

        /**
         * Bandeau de marque en pied de page : bande bleue + vague orange (reprise du
         * graphisme de l'app, redessinée nativement) et signature en blanc. Présent sur
         * chaque page.
         */
        private fun drawBrandBand() {
            val width = PAGE_WIDTH.toFloat()
            val bandTop = PAGE_HEIGHT - BAND_HEIGHT

            val wave = Path().apply {
                moveTo(300f, bandTop)
                cubicTo(380f, bandTop, 405f, bandTop - 27f, 470f, bandTop - 27f)
                cubicTo(520f, bandTop - 27f, 545f, bandTop - 13f, width, bandTop - 19f)
                lineTo(width, bandTop)
                close()
            }
            canvas.drawPath(wave, orangePaint)
            canvas.drawRect(0f, bandTop, width, PAGE_HEIGHT.toFloat(), bluePaint)
            canvas.drawText("Généré par FemSanté", MARGIN_LEFT, PAGE_HEIGHT - 13f, brandFooterPaint)
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > PAGE_HEIGHT - MARGIN_BOTTOM) {
                document.finishPage(page)
                startNewPage()
            }
        }

        fun gap(height: Float) { y += height }

        /** Une ligne de texte simple (part du bord gauche). */
        fun text(value: String, paint: Paint) {
            ensureSpace(LINE_HEIGHT)
            canvas.drawText(value, MARGIN_LEFT, y, paint)
            y += LINE_HEIGHT
        }

        fun indentedText(value: String, paint: Paint, indent: Float) {
            ensureSpace(NOTE_LINE_HEIGHT)
            canvas.drawText(value, MARGIN_LEFT + indent, y, paint)
            y += NOTE_LINE_HEIGHT
        }

        /** Titre de section avec filet de séparation. */
        fun section(title: String) {
            ensureSpace(LINE_HEIGHT + 18f)
            gap(2f)
            canvas.drawText(title, MARGIN_LEFT, y, sectionPaint)
            y += 6f
            canvas.drawLine(MARGIN_LEFT, y, MARGIN_LEFT + CONTENT_WIDTH, y, rulePaint)
            y += 14f
        }

        /** Couple libellé (gris) → valeur (noir) sur une même ligne. */
        fun field(label: String, value: String) {
            val labelText = "$label : "
            val valueLines = wrapLines(value, valuePaint, CONTENT_WIDTH - LABEL_WIDTH)
            ensureSpace(LINE_HEIGHT.coerceAtLeast(valueLines.size * LINE_HEIGHT))
            canvas.drawText(labelText, MARGIN_LEFT, y, labelPaint)
            valueLines.forEachIndexed { index, line ->
                if (index > 0) { ensureSpace(LINE_HEIGHT) }
                canvas.drawText(line, MARGIN_LEFT + LABEL_WIDTH, y, valuePaint)
                y += LINE_HEIGHT
            }
        }

        fun wrapped(text: String, paint: Paint) {
            wrapLines(text, paint, CONTENT_WIDTH).forEach { line ->
                ensureSpace(LINE_HEIGHT)
                canvas.drawText(line, MARGIN_LEFT, y, paint)
                y += LINE_HEIGHT
            }
        }

        /** Petit graphe (sparkline) d'une mesure : ligne + points d'extrémité. */
        fun sparkline(points: List<MeasurePoint>) {
            val chartHeight = 46f
            ensureSpace(chartHeight + LINE_HEIGHT + 6f)
            val left = MARGIN_LEFT + 8f
            val right = MARGIN_LEFT + CONTENT_WIDTH
            val top = y
            val bottom = y + chartHeight

            val values = points.map { it.value }
            var minV = values.minOrNull()!!
            var maxV = values.maxOrNull()!!
            if (minV == maxV) { minV -= 1.0; maxV += 1.0 }

            fun xAt(i: Int): Float = left + (right - left) * i / (points.size - 1)
            fun yAt(v: Double): Float = bottom - ((v - minV) / (maxV - minV) * (bottom - top)).toFloat()

            canvas.drawLine(left, bottom, right, bottom, sparkGuidePaint)
            val path = Path()
            points.forEachIndexed { i, p ->
                val x = xAt(i); val py = yAt(p.value)
                if (i == 0) path.moveTo(x, py) else path.lineTo(x, py)
            }
            canvas.drawPath(path, sparkLinePaint)
            canvas.drawCircle(xAt(0), yAt(points.first().value), 2.5f, sparkDotPaint)
            canvas.drawCircle(xAt(points.size - 1), yAt(points.last().value), 2.5f, sparkDotPaint)

            // Saute une ligne sous le graphe pour ne pas coller au texte suivant.
            y = bottom + LINE_HEIGHT + 6f
        }

        fun finish() { document.finishPage(page) }
    }

    private companion object {
        // A4 en points (72 dpi) : 595 × 842.
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN_LEFT = 48f
        const val MARGIN_TOP = 60f
        // Marge basse large : garde le contenu au-dessus du bandeau + de la vague orange.
        const val MARGIN_BOTTOM = 72f
        const val BAND_HEIGHT = 34f
        const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN_LEFT
        const val LABEL_WIDTH = 220f
        const val LINE_HEIGHT = 18f
        const val NOTE_LINE_HEIGHT = 15f

        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE)
        val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd/MM", Locale.FRANCE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 22f; typeface = Typeface.DEFAULT_BOLD
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 90, 90); textSize = 11f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(90, 90, 90); textSize = 11f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK; textSize = 11f
        }
        val notesPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(70, 70, 70); textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
        val rulePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(200, 200, 200); strokeWidth = 0.8f
        }
        // Charte "soleil/joie" : bleu et orange de l'app (blue_app / orange_app).
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0085ac") }
        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#ee9e3d") }
        val brandFooterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 10f
        }
        // Sparkline des mesures : ligne bleue (blue_app) + repère de base gris clair.
        val sparkLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 1.4f; color = Color.parseColor("#0085ac")
            strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        }
        val sparkDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL; color = Color.parseColor("#0085ac")
        }
        val sparkGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.rgb(220, 220, 220)
        }
    }
}
