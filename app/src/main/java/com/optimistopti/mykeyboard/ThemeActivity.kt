package com.optimistopti.mykeyboard

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import com.google.android.material.card.MaterialCardView
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class ThemeActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Темы")
        setupFab(b.fab)

        addSectionHeader(b.content, "Тема")
        addToggle(b.content, "Тёмная тема", "Переключает светлую/тёмную тему", prefs.isDarkTheme) {
            prefs.isDarkTheme = it; recreate()
        }

        addDivider(b.content)
        addSectionHeader(b.content, "Цвет акцента")
        buildColorPicker(b.content)
    }

    private fun buildColorPicker(c: LinearLayout) {
        val d = resources.displayMetrics.density
        val scroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins((8*d).toInt(), (4*d).toInt(), (8*d).toInt(), (12*d).toInt())
            }
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        KeyboardPrefs.PRESET_COLORS.forEachIndexed { i, color ->
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((68*d).toInt(), -2).apply {
                    setMargins((4*d).toInt(), 0, (4*d).toInt(), 0)
                }
            }
            val btn = android.widget.ImageButton(this).apply {
                val sz = (52*d).toInt()
                layoutParams = LinearLayout.LayoutParams(sz, sz)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL; setColor(color)
                    if (color == prefs.accentColor)
                        setStroke((3*d).toInt(), if (prefs.isDarkTheme) 0xFFFFFFFF.toInt() else 0xFF000000.toInt())
                }
                setOnClickListener { prefs.accentColor = color; buildColorPicker(c.also { it.removeViewAt(it.childCount-1) }) }
            }
            val lbl = android.widget.TextView(this).apply {
                text = KeyboardPrefs.COLOR_NAMES[i]; textSize = 10f
                gravity = android.view.Gravity.CENTER
                setTextColor(prefs.textColor())
                layoutParams = LinearLayout.LayoutParams(-2,-2).apply { topMargin=(4*d).toInt() }
            }
            col.addView(btn); col.addView(lbl); row.addView(col)
        }
        scroll.addView(row); c.addView(scroll)
    }
}
