package com.audreyRetournayDiet.femSante.features.calendar.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.Locale

/**
 * Petit graphe en courbe dessiné à la main (Canvas), sans dépendance. Affiche une série
 * de points (date → valeur) : axe de valeurs à gauche (min/max), dates aux extrémités,
 * ligne + points. Gère l'absence de données et le cas d'un seul point.
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    data class Point(val label: String, val value: Double)

    private var points: List<Point> = emptyList()
    private var emptyText: String = "Aucune donnée"

    fun setData(points: List<Point>, emptyText: String) {
        this.points = points
        this.emptyText = emptyText
        invalidate()
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 4f; color = Color.parseColor("#0085ac")
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = Color.parseColor("#0085ac")
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.parseColor("#E3DCCF")
    }
    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7A7368"); textSize = 26f
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9A9388"); textSize = 30f; textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()

        if (points.isEmpty()) {
            canvas.drawText(emptyText, w / 2f, h / 2f, emptyPaint)
            return
        }

        val leftPad = 90f
        val rightPad = 24f
        val topPad = 20f
        val bottomPad = 40f
        val plotW = w - leftPad - rightPad
        val plotH = h - topPad - bottomPad

        val values = points.map { it.value }
        var minV = values.minOrNull()!!
        var maxV = values.maxOrNull()!!
        if (minV == maxV) { minV -= 1.0; maxV += 1.0 } // série plate : on aère l'échelle

        fun xAt(i: Int): Float =
            if (points.size == 1) leftPad + plotW / 2f
            else leftPad + plotW * i / (points.size - 1)

        fun yAt(v: Double): Float =
            topPad + plotH - ((v - minV) / (maxV - minV) * plotH).toFloat()

        // Lignes de repère haute/basse + valeurs
        canvas.drawLine(leftPad, yAt(maxV), w - rightPad, yAt(maxV), gridPaint)
        canvas.drawLine(leftPad, yAt(minV), w - rightPad, yAt(minV), gridPaint)
        canvas.drawText(num(maxV), 12f, yAt(maxV) + 9f, axisTextPaint)
        canvas.drawText(num(minV), 12f, yAt(minV) + 9f, axisTextPaint)

        // Courbe
        if (points.size > 1) {
            val path = Path()
            points.forEachIndexed { i, p ->
                val x = xAt(i); val y = yAt(p.value)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, linePaint)
        }
        points.forEachIndexed { i, p -> canvas.drawCircle(xAt(i), yAt(p.value), 6f, dotPaint) }

        // Dates aux extrémités
        val baseY = h - 10f
        canvas.drawText(points.first().label, leftPad, baseY, axisTextPaint)
        if (points.size > 1) {
            val last = points.last().label
            canvas.drawText(last, w - rightPad - axisTextPaint.measureText(last), baseY, axisTextPaint)
        }
    }

    private fun num(value: Double): String =
        if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.FRANCE, "%.1f", value)
}
