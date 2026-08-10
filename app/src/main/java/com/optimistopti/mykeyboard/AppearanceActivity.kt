package com.optimistopti.mykeyboard

import android.os.Bundle
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class AppearanceActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Внешний вид")
        setupFab(b.fab)

        addSectionHeader(b.content, "Размеры")
        addSlider(b.content, "Высота клавиатуры", prefs.keyHeightDp.toFloat(), 36f, 72f,
            labelFormatter = { "${it.toInt()} dp" }) { prefs.keyHeightDp = it.toInt() }

        addSlider(b.content, "Отступы между клавишами", prefs.keyPaddingDp.toFloat(), 2f, 12f,
            labelFormatter = { "${it.toInt()} dp" }) { prefs.keyPaddingDp = it.toInt() }

        addDivider(b.content)
        addSectionHeader(b.content, "Форма")
        addSlider(b.content, "Скругление клавиш", prefs.keyRadius, 0f, 28f,
            labelFormatter = { "${it.toInt()} dp" }) { prefs.keyRadius = it }

        addDivider(b.content)
        addSectionHeader(b.content, "Прозрачность")
        addSlider(b.content, "Непрозрачность клавиш", prefs.keyAlpha.toFloat(), 0f, 255f,
            labelFormatter = { "${(it/255*100).toInt()}%" }) { prefs.keyAlpha = it.toInt() }
    }
}
