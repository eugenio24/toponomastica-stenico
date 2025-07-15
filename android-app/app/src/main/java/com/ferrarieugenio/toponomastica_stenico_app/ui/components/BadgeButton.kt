package com.ferrarieugenio.toponomastica_stenico_app.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ReplacementSpan
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.google.android.material.button.MaterialButton

class BadgeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialButtonStyle
) : MaterialButton(context, attrs, defStyleAttr) {

    private var badgeNumber: Int = 0
    private var isUpdatingText = false
    private var baseText: String = ""  // store base text explicitly

    private var badgeBackgroundColor: Int = resolveThemeColor(com.google.android.material.R.attr.colorPrimary)


    private var badgeTextColor: Int = Color.WHITE
    private val badgeCornerRadiusDp = 12f
    private val badgeHorizontalPaddingDp = 6f

    init {
        if (attrs != null) {
            context.withStyledAttributes(attrs, intArrayOf(android.R.attr.text)) {
                baseText = getString(0) ?: ""
            }
        }
        updateText()
    }

    private fun resolveThemeColor(attrResId: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attrResId))
        val color = typedArray.getColor(0, Color.BLACK)
        typedArray.recycle()
        return color
    }

    fun setBaseText(text: String) {
        baseText = text
        updateText()
    }

    fun setBadgeNumber(number: Int) {
        badgeNumber = number
        updateText()
    }

    private fun updateText() {
        if (isUpdatingText) return
        isUpdatingText = true

        if (badgeNumber <= 0) {
            super.setText(baseText)
        } else {
            val badgeText = badgeNumber.toString()
            val combinedText = "$baseText $badgeText"
            val spannable = SpannableString(combinedText)

            val start = baseText.length + 1
            val end = combinedText.length

            val cornerRadiusPx = dpToPx(badgeCornerRadiusDp)
            val horizontalPaddingPx = dpToPx(badgeHorizontalPaddingDp)

            spannable.setSpan(
                RoundedBackgroundSpan(badgeBackgroundColor, badgeTextColor, cornerRadiusPx, horizontalPaddingPx),
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            super.setText(spannable)
        }

        isUpdatingText = false
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (isUpdatingText) {
            super.setText(text, type)
        } else {
            // Store the base text when setText is called externally (without badge)
            baseText = text?.toString() ?: ""
            updateText()
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    private class RoundedBackgroundSpan(
        private val backgroundColor: Int,
        private val textColor: Int,
        private val cornerRadius: Float,
        private val horizontalPadding: Float
    ) : ReplacementSpan() {

        override fun getSize(
            paint: Paint,
            text: CharSequence?,
            start: Int,
            end: Int,
            fm: Paint.FontMetricsInt?
        ): Int {
            return (paint.measureText(text, start, end) + 2 * horizontalPadding).toInt()
        }

        override fun draw(
            canvas: Canvas,
            text: CharSequence?,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: Paint
        ) {
            if (text == null) return
            val textToDraw = text.subSequence(start, end).toString()
            val width = paint.measureText(text, start, end)
            val height = bottom - top

            val rect = RectF(
                x,
                top.toFloat(),
                x + width + 2 * horizontalPadding,
                bottom.toFloat()
            )

            val originalColor = paint.color

            paint.color = backgroundColor
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            paint.color = textColor
            canvas.drawText(textToDraw, x + horizontalPadding, y.toFloat(), paint)

            paint.color = originalColor
        }
    }
}
