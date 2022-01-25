package com.example.fakeartistny.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
        .setNegativeButton("Yes") {_, _ ->
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
        .setPositiveButton("No") {_, _ ->
            // Do nothing
        }
        .show()
}