package com.ferrarieugenio.toponomastica_stenico_app.util.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class SwipeGestureListener(
    context: Context,
    private val onSwipeUp: (() -> Unit)? = null,
    private val onSwipeDown: (() -> Unit)? = null
) : View.OnTouchListener {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 80
        private val SWIPE_VELOCITY_THRESHOLD = 80

        override fun onDown(e: MotionEvent): Boolean {
            return true // Needed to ensure other gestures are detected
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x

            if (abs(diffY) > abs(diffX)) {
                if (diffY < -SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    onSwipeUp?.invoke()
                    return true
                } else if (diffY > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    onSwipeDown?.invoke()
                    return true
                }
            }
            return false
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
}