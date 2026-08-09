package com.optimistopti.mykeyboard
import android.os.Bundle
import android.widget.*
class LanguageActivity : BaseSettingsActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_language); title = "Язык"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addKeyboardFab()
        val container = findViewById<LinearLayout>(R.id.language_container)
        val langs = mapOf("ru" to "🇷🇺  Русский","uk" to "🇺🇦  Українська","en" to "🇬🇧  English")
        val enabled = prefs.enabledLanguages.split(",").toMutableSet()
        langs.forEach { (code, name) ->
            val row = layoutInflater.inflate(R.layout.item_toggle, container, false)
            row.findViewById<TextView>(R.id.toggle_title).text = name
            val sw = row.findViewById<Switch>(R.id.toggle_switch); sw.isChecked = code in enabled
            sw.setOnCheckedChangeListener { _, checked ->
                if (checked) enabled.add(code) else enabled.remove(code)
                if (enabled.isEmpty()) { enabled.add(code); sw.isChecked = true }
                prefs.enabledLanguages = enabled.joinToString(",")
            }
            container.addView(row)
        }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}