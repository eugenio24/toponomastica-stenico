package com.ferrarieugenio.toponomastica_stenico_app.util.ui

import android.content.res.Resources

fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()