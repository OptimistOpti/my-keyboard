package com.optimistopti.mykeyboard

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class BaseSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Call this after setContentView to add FAB
    protected fun addKeyboardFab() {
        val root = window.decorView.findViewById<FrameLayout>(android.R.id.content)
        val fab = FloatingActionButton(this).apply {
            setImageResource(android.R.drawable.ic_input_get)
            size = com.google.android.material.floatingactionbutton.FloatingActionButton.SIZE_NORMAL
            val density = resources.displayMetrics.density
            val margin = (16 * density).toInt()
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
                setMargins(margin, margin, margin, (margin * 4))
            }
            layoutParams = params
            contentDescription = "Показать клавиатуру"
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                KeyboardPrefs(context).accentColor
            )
            setOnClickListener {
                // Show a small edit text dialog to invoke the keyboard
                showKeyboardPreview()
            }
        }
        root.addView(fab)
    }

    private fun showKeyboardPreview() {
        val et = android.widget.EditText(this)
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Предпросмотр клавиатуры")
            .setMessage("Введи текст чтобы протестировать:")
            .setView(et)
            .setPositiveButton("Закрыть", null)
            .create()
        dialog.show()
        et.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
    }
}
