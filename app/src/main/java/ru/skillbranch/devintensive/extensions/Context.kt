package ru.skillbranch.devintensive.extensions

import android.content.Context
import kotlin.math.roundToInt

fun Context.dpToPx(dp: Int): Float {
    return dp.toFloat() * this.resources.displayMetrics.density
}

fun Context.pxToDp(px: Float): Int {
    return (px / this.resources.displayMetrics.density).toInt()
}

fun Context.spToPx(sp: Int): Int {
    return sp * this.resources.displayMetrics.scaledDensity.roundToInt()
}
