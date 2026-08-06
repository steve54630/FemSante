package com.audreyRetournayDiet.femSante.features.calendar.add

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.audreyRetournayDiet.femSante.room.type.PainZone

/**
 * Carte corporelle de la douleur : silhouette (face / dos) dont chaque zone se colore selon
 * son intensité (1 à 10), en « carte de chaleur ». On touche une zone pour la sélectionner,
 * l'hôte affiche alors le sélecteur d'intensité et rappelle [setIntensity].
 *
 * La vue dessine dans un **espace de conception fixe** (280 × 430) mis à l'échelle vers la
 * taille réelle : les silhouettes et les zones restent identiques quelle que soit la densité.
 * La détection du toucher se fait en re-projetant le point dans cet espace.
 */
class BodyPainMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    /** Zone dessinée : à quelle [PainZone] et quel côté elle correspond, + sa (ses) forme(s). */
    private class ZoneShape(val zone: PainZone, val back: Boolean, val paths: List<Path>) {
        val region: Region = Region().apply {
            val clip = Region(0, 0, DESIGN_W, DESIGN_H)
            paths.forEach { p ->
                val r = Region()
                r.setPath(p, clip)
                op(r, Region.Op.UNION)
            }
        }
    }

    private val silhouette: List<Path> = buildSilhouette()
    private val zones: List<ZoneShape> = buildZones()

    private val painByZone = linkedMapOf<PainZone, Int>()

    var showBack: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                selectedZone = null
                invalidate()
            }
        }

    var selectedZone: PainZone? = null
        private set

    /** Notifié quand une zone est touchée (l'hôte ouvre le sélecteur d'intensité). */
    var onZoneSelected: ((PainZone) -> Unit)? = null

    /** Notifié à chaque changement de la carte (pour remonter l'état au ViewModel). */
    var onChanged: ((Map<PainZone, Int>) -> Unit)? = null

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = Color.parseColor("#EBE5DB")
    }
    private val bodyStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1.5f; color = Color.parseColor("#D6CDBE")
    }
    private val zoneIdle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = Color.parseColor("#E3DACB")
    }
    private val zoneIdleStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.parseColor("#CDC3B2")
    }
    private val heatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2.5f; color = Color.parseColor("#3A3A3A")
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f, 4f), 0f)
    }

    private val drawMatrix = Matrix()
    private var scale = 1f
    private var dx = 0f
    private var dy = 0f

    /** Remplace l'ensemble des intensités (préchargement depuis le ViewModel). */
    fun setPainByZone(values: Map<PainZone, Int>) {
        painByZone.clear()
        values.forEach { (z, lvl) -> if (lvl in 1..10) painByZone[z] = lvl }
        invalidate()
    }

    /** Fixe (1–10) ou retire (0) l'intensité d'une zone, puis notifie l'hôte. */
    fun setIntensity(zone: PainZone, level: Int) {
        if (level in 1..10) painByZone[zone] = level else painByZone.remove(zone)
        invalidate()
        onChanged?.invoke(HashMap(painByZone))
    }

    fun intensityOf(zone: PainZone): Int = painByZone[zone] ?: 0

    /** Retire la sélection courante (ex. à la fermeture de la modale d'intensité). */
    fun clearSelection() {
        if (selectedZone != null) {
            selectedZone = null
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        // Ratio fixe de la silhouette (hauteur = largeur × 430/280).
        val h = (w * DESIGN_H / DESIGN_W)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        scale = minOf(w.toFloat() / DESIGN_W, h.toFloat() / DESIGN_H)
        dx = (w - DESIGN_W * scale) / 2f
        dy = (h - DESIGN_H * scale) / 2f
        drawMatrix.reset()
        drawMatrix.postScale(scale, scale)
        drawMatrix.postTranslate(dx, dy)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(drawMatrix)

        silhouette.forEach { canvas.drawPath(it, bodyPaint); canvas.drawPath(it, bodyStroke) }

        zones.filter { it.back == showBack }.forEach { shape ->
            val level = painByZone[shape.zone] ?: 0
            shape.paths.forEach { p ->
                if (level in 1..10) {
                    heatPaint.color = heatColor(level)
                    canvas.drawPath(p, heatPaint)
                } else {
                    canvas.drawPath(p, zoneIdle)
                    canvas.drawPath(p, zoneIdleStroke)
                }
                if (shape.zone == selectedZone) canvas.drawPath(p, selectionPaint)
            }
        }
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (scale <= 0f) return false
            val px = ((event.x - dx) / scale).toInt()
            val py = ((event.y - dy) / scale).toInt()
            val hit = zones.firstOrNull { it.back == showBack && it.region.contains(px, py) }
            if (hit != null) {
                selectedZone = hit.zone
                invalidate()
                onZoneSelected?.invoke(hit.zone)
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private companion object {
        const val DESIGN_W = 280
        const val DESIGN_H = 430

        /** Échelle de chaleur : pâle (1) -> rouge profond (10). */
        val HEAT = intArrayOf(
            Color.parseColor("#FCE9C0"), Color.parseColor("#FADCA6"), Color.parseColor("#F7C97F"),
            Color.parseColor("#F3B15A"), Color.parseColor("#EF9A44"), Color.parseColor("#EA8340"),
            Color.parseColor("#E4703F"), Color.parseColor("#DB5638"), Color.parseColor("#D03E30"),
            Color.parseColor("#C62D2A")
        )

        fun heatColor(level: Int): Int = HEAT[level.coerceIn(1, 10) - 1]

        fun oval(cx: Float, cy: Float, rx: Float, ry: Float): Path =
            Path().apply { addOval(RectF(cx - rx, cy - ry, cx + rx, cy + ry), Path.Direction.CW) }

        fun buildSilhouette(): List<Path> {
            val head = Path().apply { addCircle(140f, 52f, 26f, Path.Direction.CW) }
            val neck = Path().apply { addRect(131f, 76f, 149f, 88f, Path.Direction.CW) }
            val torso = Path().apply { addRoundRect(RectF(104f, 86f, 176f, 236f), 30f, 30f, Path.Direction.CW) }
            val armL = Path().apply {
                addRoundRect(RectF(82f, 94f, 100f, 214f), 9f, 9f, Path.Direction.CW)
                transform(Matrix().apply { setRotate(8f, 91f, 98f) })
            }
            val armR = Path().apply {
                addRoundRect(RectF(180f, 94f, 198f, 214f), 9f, 9f, Path.Direction.CW)
                transform(Matrix().apply { setRotate(-8f, 189f, 98f) })
            }
            val pelvis = oval(140f, 238f, 46f, 28f)
            val legL = Path().apply { addRoundRect(RectF(110f, 232f, 137f, 397f), 13f, 13f, Path.Direction.CW) }
            val legR = Path().apply { addRoundRect(RectF(143f, 232f, 170f, 397f), 13f, 13f, Path.Direction.CW) }
            return listOf(head, neck, torso, armL, armR, pelvis, legL, legR)
        }

        fun buildZones(): List<ZoneShape> = listOf(
            ZoneShape(PainZone.TETE, back = false, listOf(oval(140f, 52f, 26f, 27f))),
            ZoneShape(PainZone.SEINS, back = false, listOf(oval(140f, 116f, 30f, 22f))),
            ZoneShape(PainZone.BRAS, back = false, listOf(oval(90f, 150f, 13f, 42f), oval(190f, 150f, 13f, 42f))),
            ZoneShape(PainZone.ABDOMEN, back = false, listOf(oval(140f, 172f, 28f, 22f))),
            ZoneShape(PainZone.BASSIN, back = false, listOf(oval(140f, 222f, 40f, 24f))),
            ZoneShape(PainZone.CUISSES, back = false, listOf(oval(124f, 285f, 15f, 46f), oval(156f, 285f, 15f, 46f))),
            ZoneShape(PainZone.HAUT_DOS, back = true, listOf(oval(140f, 120f, 32f, 26f))),
            ZoneShape(PainZone.LOMBAIRES, back = true, listOf(oval(140f, 184f, 32f, 22f))),
            ZoneShape(PainZone.JAMBES, back = true, listOf(oval(124f, 330f, 14f, 52f), oval(156f, 330f, 14f, 52f)))
        )
    }
}
