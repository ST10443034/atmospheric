package com.group24.atmospheric.ui.forecast

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.group24.atmospheric.R

/**
 * A minimal single-series line chart: three gridlines, one round-joined polyline.
 * No axes, no legends, no chart junk — matches the design's "one series" graph card.
 */
class LineGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var values: List<Float> = emptyList()
    private var isCachedSeries = false

    private val gridPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.line)
        strokeWidth = 2f
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun setData(values: List<Float>, isCached: Boolean) {
        this.values = values
        this.isCachedSeries = isCached
        linePaint.color = ContextCompat.getColor(
            context,
            if (isCached) R.color.warn else R.color.link
        )
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        // Three horizontal gridlines.
        for (i in 1..3) {
            val y = h * i / 4f
            canvas.drawLine(0f, y, w, y, gridPaint)
        }

        if (values.size < 2) return
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0.01f } ?: 1f

        val path = Path()
        values.forEachIndexed { index, value ->
            val x = w * index / (values.size - 1)
            val normalized = (value - min) / range
            val y = h - (normalized * h)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)
    }
}
