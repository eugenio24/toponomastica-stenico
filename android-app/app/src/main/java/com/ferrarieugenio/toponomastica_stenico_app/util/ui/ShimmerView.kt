package com.ferrarieugenio.toponomastica_stenico_app.util.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.LinearLayout

class ShimmerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val paint = Paint()
    private var gradient: LinearGradient? = null
    private val gradientMatrix = Matrix()
    private var translateX = 0f
    private var animator: ValueAnimator? = null

    init {
        paint.isAntiAlias = true
        startShimmer()
    }

    private fun createGradient(width: Int) {
        gradient = LinearGradient(
            -width.toFloat(), 0f,
            0f, 0f,
            intArrayOf(
                Color.LTGRAY,
                Color.WHITE,
                Color.LTGRAY
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) {
            createGradient(w)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (gradient == null) return

        gradientMatrix.setTranslate(translateX, 0f)
        gradient!!.setLocalMatrix(gradientMatrix)
        canvas.drawRoundRect(
            0f, 0f, width.toFloat(), height.toFloat(),
            height / 2f, height / 2f, paint)  // rounded corners
    }

    fun startShimmer() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, width * 2f).apply {
            duration = 1500L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                translateX = it.animatedValue as Float - width.toFloat()
                invalidate()
            }
            start()
        }
    }

    fun stopShimmer() {
        animator?.cancel()
        animator = null
    }
}

fun createDetailShimmerPlaceholder(context: Context): View {
    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
    }

    fun addBar(heightDp: Int, marginTopDp: Int = 8) {
        val shimmerBar = ShimmerView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightDp.dpToPx()
            ).apply {
                topMargin = marginTopDp.dpToPx()
            }
        }
        container.addView(shimmerBar)
    }

    // Top toolbar placeholder
    addBar(heightDp = 56, marginTopDp = 0)

    // Simulated content fields
    addBar(32)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(20)

    addBar(40, marginTopDp = 24)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(20)

    addBar(16)
    addBar(80, marginTopDp = 24)

    return container
}
