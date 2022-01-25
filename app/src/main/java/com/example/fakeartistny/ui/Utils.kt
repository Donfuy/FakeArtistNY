package com.example.fakeartistny.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Shows soft keyboard with focus on the provided view.
 */
fun showSoftKeyboard(activity: Activity, view: View) {
    if (view.requestFocus()) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun hideSoftKeyboard(activity: Activity, view: View) {
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}
