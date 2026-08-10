package com.optimistopti.mykeyboard

import android.os.Bundle
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class SoundActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Звук и вибрация")
        setupFab(b.fab)

        addSectionHeader(b.content, "Вибрация")
        addToggle(b.content, "Вибрация при нажатии", current = prefs.vibrateEnabled) { prefs.vibrateEnabled = it }
        addSlider(b.content, "Длительность вибрации", prefs.vibrateDurationMs.toFloat(), 10f, 100f,
            labelFormatter = { "${it.toInt()} мс" }) { prefs.vibrateDurationMs = it.toInt() }

        addDivider(b.content)
        addSectionHeader(b.content, "Звук")
        addToggle(b.content, "Звук нажатия", "Щелчок при каждом нажатии", prefs.soundEnabled) { prefs.soundEnabled = it }
    }
}
