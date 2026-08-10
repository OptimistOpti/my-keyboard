package com.optimistopti.mykeyboard

import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class LanguageActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Язык")
        setupFab(b.fab)

        val langs = linkedMapOf("ru" to "🇷🇺  Русский", "uk" to "🇺🇦  Українська", "en" to "🇬🇧  English")
        val enabled = prefs.enabledLanguages.split(",").toMutableSet()

        addSectionHeader(b.content, "Активные языки")
        langs.forEach { (code, name) ->
            addToggle(b.content, name, current = code in enabled) { on ->
                if (on) enabled.add(code) else enabled.remove(code)
                if (enabled.isEmpty()) enabled.add(code)
                prefs.enabledLanguages = enabled.joinToString(",")
            }
        }

        addSectionHeader(b.content, "Основной язык")
        val primary = listOf("ru", "uk", "en")
        primary.forEach { code ->
            val name = langs[code] ?: return@forEach
            addToggle(b.content, name, current = prefs.primaryLanguage == code) { on ->
                if (on) prefs.primaryLanguage = code
            }
        }
    }
}
