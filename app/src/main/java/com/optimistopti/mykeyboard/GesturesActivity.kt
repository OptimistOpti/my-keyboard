package com.optimistopti.mykeyboard

import android.os.Bundle
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class GesturesActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Жесты и свайп")
        setupFab(b.fab)

        addSectionHeader(b.content, "Свайп-набор")
        addToggle(b.content, "Свайп-набор слов", "Проведи по буквам чтобы написать слово", prefs.swipeEnabled) { prefs.swipeEnabled = it }

        addDivider(b.content)
        addSectionHeader(b.content, "Удаление")
        addToggle(b.content, "Удаление слова жестом", "Свайп влево на ⌫ удаляет слово", prefs.swipeDeleteEnabled) { prefs.swipeDeleteEnabled = it }

        addDivider(b.content)
        addSectionHeader(b.content, "Пробел")
        addToggle(b.content, "Смена языка свайпом", "Свайп по пробелу меняет язык", true) { /* always on */ }
    }
}
