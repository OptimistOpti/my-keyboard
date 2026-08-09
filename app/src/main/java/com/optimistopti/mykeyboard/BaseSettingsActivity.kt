package com.optimistopti.mykeyboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

abstract class BaseSettingsActivity : AppCompatActivity() {

    protected fun applyAppTheme(prefs: KeyboardPrefs) {
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
    }

    protected fun setupToolbar(toolbar: Toolbar, title: String?) {
        setSupportActionBar(toolbar)
        title?.let { supportActionBar?.title = it }
        supportActionBar?.setDisplayHomeAsUpEnabled(title != null)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    protected fun showKeyboardPreview() {
        val et = EditText(this).apply { hint = "Введи текст…" }
        AlertDialog.Builder(this)
            .setTitle("Тест клавиатуры")
            .setView(et)
            .setPositiveButton("Закрыть", null)
            .show()
        et.post {
            et.requestFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
