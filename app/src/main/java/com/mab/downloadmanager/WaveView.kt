package com.mab.downloadmanager


/**
 * Created by Mirza Ahmed Baig on 2019-08-05.
 * Avantari Technologies
 * mirza@avantari.org
 */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class WaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var amplitude = 30f.toDp() // scale
    private var speed = 0f
    private val path = Path()
    private var paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        animator?.cancel()
        animator = createAnimator().apply { start() }
    }

    override fun onDraw(c: Canvas) = c.drawPath(path, paint)

    private fun createAnimator(): ValueAnimator {
        return ValueAnimator.ofFloat(0f, Float.MAX_VALUE).apply {
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                speed -= WAVE_SPEED
                createPath()
                invalidate()
            }
        }
    }

    private fun createPath() {
        path.reset()
        paint.color = Color.parseColor("#1da6f9")
        path.moveTo(0f, height.toFloat())
        path.lineTo(0f, amplitude)
        var i = 0
        while (i < width + 10) {
            val wx = i.toFloat()
            val wy = amplitude * 2 + amplitude * sin((i + 10) * Math.PI / WAVE_AMOUNT_ON_SCREEN + speed).toFloat()
            path.lineTo(wx, wy)
            i += 10
            path.rQuadTo(wx, 5f, 40f, wy)
            path.rQuadTo(20f, wy, wx, 0f)
        }
        path.lineTo(width.toFloat(), height.toFloat())
        path.close()
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        const val WAVE_SPEED = 0.2f
        const val WAVE_AMOUNT_ON_SCREEN = 270
    }

    private fun Float.toDp() = this * context.resources.displayMetrics.density
}