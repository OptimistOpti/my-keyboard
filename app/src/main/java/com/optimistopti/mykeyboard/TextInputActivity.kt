package com.optimistopti.mykeyboard

import android.os.Bundle
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class TextInputActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Ввод текста")
        setupFab(b.fab)

        addSectionHeader(b.content, "Коррекция")
        addToggle(b.content, "Автокоррекция", "Исправлять опечатки автоматически", prefs.autocorrectEnabled) { prefs.autocorrectEnabled = it }

        addDivider(b.content)
        addSectionHeader(b.content, "Предсказание")
        addToggle(b.content, "Предсказание слов", "Показывать варианты над клавиатурой", prefs.predictionEnabled) { prefs.predictionEnabled = it }
    }
}
