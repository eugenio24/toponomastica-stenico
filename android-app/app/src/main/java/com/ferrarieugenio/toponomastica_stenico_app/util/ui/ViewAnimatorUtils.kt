package com.ferrarieugenio.toponomastica_stenico_app.util.ui

import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd

object ViewAnimatorUtils {
    fun showBottomPanel(view: View, duration: Long = 200L) {
        view.apply {
            alpha = 0f
            translationY = 100f
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .start()
        }
    }

    fun hideBottomPanel(view: View, duration: Long = 200L, endAction: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                view.translationY = 0f
                view.alpha = 1f
                endAction?.invoke()
            }.start()
    }

    fun expandWithShimmerAndNavigate(
        preview: ViewGroup,
        rootView: ViewGroup,
        shimmerContainer: ViewGroup,
        contentContainer: ViewGroup,
        shimmerView: View,
        duration: Long = 300L,
        onNavigate: () -> Unit
    ) {
        // Hide actual content and show shimmer
        contentContainer.visibility = View.GONE
        shimmerContainer.removeAllViews()
        shimmerContainer.addView(shimmerView)
        shimmerContainer.visibility = View.VISIBLE

        val startHeight = preview.height
        val endHeight = rootView.height

        val animator = ValueAnimator.ofInt(startHeight, endHeight).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()

            addUpdateListener { valueAnimator ->
                val newHeight = valueAnimator.animatedValue as Int
                preview.layoutParams = preview.layoutParams.apply {
                    height = newHeight
                }
            }

            doOnEnd {
                onNavigate()
                // Delay to allow navigation to complete before cleanup
                preview.post {
                    shimmerContainer.removeAllViews()
                    shimmerContainer.visibility = View.GONE
                    contentContainer.visibility = View.VISIBLE
                    preview.layoutParams = preview.layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            }
        }

        animator.start()
    }

}
