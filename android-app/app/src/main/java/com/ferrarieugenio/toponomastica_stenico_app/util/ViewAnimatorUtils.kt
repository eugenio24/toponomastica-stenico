package com.ferrarieugenio.toponomastica_stenico_app.util

import android.view.View

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

    fun hideBottomPanel(view: View, duration: Long = 200L, endAction: () -> Unit) {
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                view.translationY = 0f
                view.alpha = 1f
                endAction()
            }.start()
    }
}
