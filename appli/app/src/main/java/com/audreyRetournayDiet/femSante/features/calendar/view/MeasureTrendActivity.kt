package com.audreyRetournayDiet.femSante.features.calendar.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.report.MeasurementMetric
import com.audreyRetournayDiet.femSante.room.dto.MeasurementHistoryRow
import com.audreyRetournayDiet.femSante.viewModels.calendar.MeasureTrendViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Écran « Mon évolution » : courbe de tendance d'une mesure (poids ou tours) dans le temps.
 * L'utilisatrice choisit la mesure via des chips ; le graphe et le résumé se recalculent.
 * Données strictement locales (lecture seule).
 */
@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class MeasureTrendActivity : AppCompatActivity() {

    private val viewModel: MeasureTrendViewModel by viewModels()

    private lateinit var chart: LineChartView
    private lateinit var summary: TextView

    private var history: List<MeasurementHistoryRow> = emptyList()
    private var selectedMetric: MeasurementMetric = MeasurementMetric.WEIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measure_trend)

        chart = findViewById(R.id.lineChart)
        summary = findViewById(R.id.tvTrendSummary)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupMetric)

        buildMetricChips(chipGroup)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.history.collect { rows ->
                    history = rows
                    render(selectedMetric)
                }
            }
        }
    }

    private fun buildMetricChips(group: ChipGroup) {
        MeasurementMetric.values().forEach { metric ->
            val chip = Chip(this).apply {
                text = metricLabel(metric)
                isCheckable = true
                tag = metric
                id = View.generateViewId()
            }
            group.addView(chip)
        }
        (group.getChildAt(0) as Chip).isChecked = true

        group.setOnCheckedStateChangeListener { g, ids ->
            val metric = ids.firstOrNull()?.let { g.findViewById<Chip>(it)?.tag as? MeasurementMetric }
            if (metric != null) render(metric)
        }
    }

    private fun render(metric: MeasurementMetric) {
        selectedMetric = metric
        val series = history.mapNotNull { row ->
            metricValue(row, metric)?.let { LineChartView.Point(shortDate(row.date), it) }
        }
        chart.setData(series, getString(R.string.trend_empty))
        summary.text = summaryText(metric, series)
    }

    private fun summaryText(metric: MeasurementMetric, series: List<LineChartView.Point>): String {
        val label = metricLabel(metric)
        val unit = metricUnit(metric)
        return when {
            series.isEmpty() -> getString(R.string.trend_empty)
            series.size == 1 -> "$label : ${num(series.first().value)} $unit (1 mesure)"
            else -> {
                val first = series.first().value
                val last = series.last().value
                "$label : ${num(first)} → ${num(last)} $unit (${deltaText(last - first, unit)})"
            }
        }
    }

    private fun metricValue(row: MeasurementHistoryRow, metric: MeasurementMetric): Double? = when (metric) {
        MeasurementMetric.WEIGHT -> row.weight
        MeasurementMetric.WAIST -> row.waist
        MeasurementMetric.HIPS -> row.hips
        MeasurementMetric.THIGHS -> row.thighs
        MeasurementMetric.CHEST -> row.chest
        MeasurementMetric.ARMS -> row.arms
    }

    private fun metricLabel(metric: MeasurementMetric): String = when (metric) {
        MeasurementMetric.WEIGHT -> "Poids"
        MeasurementMetric.WAIST -> "Taille"
        MeasurementMetric.HIPS -> "Hanches"
        MeasurementMetric.THIGHS -> "Cuisses"
        MeasurementMetric.CHEST -> "Poitrine"
        MeasurementMetric.ARMS -> "Bras"
    }

    private fun metricUnit(metric: MeasurementMetric): String =
        if (metric == MeasurementMetric.WEIGHT) "kg" else "cm"

    private fun deltaText(delta: Double, unit: String): String {
        if (delta == 0.0) return "stable"
        val magnitude = if (delta < 0) -delta else delta
        val sign = if (delta > 0) "+" else "-"
        return "$sign${num(magnitude)} $unit"
    }

    private fun num(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.FRANCE, "%.1f", value)

    private fun shortDate(timestamp: Long): String =
        Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate().format(dayFormat)

    private companion object {
        val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.FRANCE)
    }
}
