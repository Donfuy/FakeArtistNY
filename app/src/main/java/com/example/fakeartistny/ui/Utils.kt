package com.example.fakeartistny.ui

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.graphics.ColorUtils
import com.example.fakeartistny.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.absoluteValue
import kotlin.random.Random

/**
 * Shows soft keyboard with focus on the provided view.
 */
fun showSoftKeyboard(activity: Activity, view: View) {
    if (view.requestFocus()) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

/**
 * Hides soft keyboard
 */
fun hideSoftKeyboard(activity: Activity, view: View) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

/**
 * Shows an alert dialog asking to reset the game
 */
fun showResetGameAlertDialog(context: Context, function: () -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setMessage("Go back to the main menu? (this will clear everything up and reset the game")
        .setNegativeButton("Yes") { _, _ ->
            function()
        }
        .setPositiveButton("No") { _, _ ->
            // Do nothing
        }
        .show()
}

/**
 * Shows an alert dialog asking to go back to the previous player
 */
fun showGoBackAlertDialog(context: Context, function: () -> Unit) {
    MaterialAlertDialogBuilder(context)
        .setMessage("Go back to the previous player?")
        .setNegativeButton("Yes") { _, _ ->
            function()
        }
        .setPositiveButton("No") { _, _ ->
            // Do nothing
        }
        .show()
}

private fun getFgColors(context: Context): MutableList<Int> {
    val taFgColors = context.resources.obtainTypedArray(R.array.fg_colors)
    val fgColors = mutableListOf<Int>()
    for (i in 0 until taFgColors.length()) {
        fgColors.add(taFgColors.getResourceId(i, 0))
    }
    taFgColors.recycle()
    return fgColors
}

private fun getBgColors(context: Context): MutableList<Int> {
    val taBgColors = context.resources.obtainTypedArray(R.array.bg_colors)
    val bgColors = mutableListOf<Int>()
    for (i in 0 until taBgColors.length()) {
        bgColors.add(taBgColors.getResourceId(i, 0))
    }
    taBgColors.recycle()
    return bgColors
}

fun getNextColor(context: Context, fgColor: Int): Int {
    val fgColors = getFgColors(context)

    return if (fgColors.indexOf(fgColor) == fgColors.size - 1) {
        fgColors[0]
    } else {
        fgColors[fgColors.indexOf(fgColor) + 1]
    }
}

fun getBgColor(context: Context, fgColor: Int): Int {
    val fgColors = getFgColors(context)
    val bgColors = getBgColors(context)
    return bgColors[fgColors.indexOf(fgColor)]
}

fun darkenStatusBar(activity: Activity, color: Int) {
    activity.window.statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.2f)
}

/**
 * @param chance - % chance of true
 */
fun lucky(chance: Int): Boolean {
    return Random.nextInt(1, 100) <= chance
}