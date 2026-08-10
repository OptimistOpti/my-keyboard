package com.optimistopti.mykeyboard

import android.os.Bundle
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class KeysActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Клавиши")
        setupFab(b.fab)

        addSectionHeader(b.content, "Дополнительные клавиши")
        addToggle(b.content, "Ряд с цифрами", "Показывать 0-9 над буквенным рядом", prefs.showNumberRow) { prefs.showNumberRow = it }
        addToggle(b.content, "Запятая в нижнем ряду", current = prefs.showCommaKey) { prefs.showCommaKey = it }

        addDivider(b.content)
        addSectionHeader(b.content, "Подсказки")
        addToggle(b.content, "Символы на клавишах", "Цифры и знаки в углу клавиш", prefs.showTopHints) { prefs.showTopHints = it }
        addToggle(b.content, "Попап при нажатии", "Увеличенная буква над клавишей", prefs.popupEnabled) { prefs.popupEnabled = it }
    }
}
