package com.optimistopti.mykeyboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class KeysActivity : AppCompatActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_keys); title = "Клавиши"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val c = findViewById<LinearLayout>(R.id.keys_container)
        addToggle(c, "Ряд с цифрами сверху", prefs.showNumberRow) { prefs.showNumberRow = it }
        addToggle(c, "Показывать символ на клавише", prefs.showTopHints) { prefs.showTopHints = it }
        addToggle(c, "Всплывающий попап при нажатии", prefs.popupEnabled) { prefs.popupEnabled = it }
        addToggle(c, "Запятая в нижнем ряду", prefs.showCommaKey) { prefs.showCommaKey = it }
    }
    private fun addToggle(c: LinearLayout, name: String, cur: Boolean, save: (Boolean) -> Unit) {
        val row = layoutInflater.inflate(R.layout.item_toggle, c, false)
        row.findViewById<TextView>(R.id.toggle_title).text = name
        val sw = row.findViewById<Switch>(R.id.toggle_switch); sw.isChecked = cur
        sw.setOnCheckedChangeListener { _, v -> save(v) }; c.addView(row)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
