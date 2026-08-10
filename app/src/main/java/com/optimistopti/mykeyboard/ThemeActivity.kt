package com.optimistopti.mykeyboard

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class ThemeActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Темы")
        setupFab(b.fab)

        buildContent()
    }

    private fun buildContent() {
        b.content.removeAllViews()

        addSectionHeader(b.content, "Тема оформления")
        val modes = listOf("☀️ Day", "🌙 Night", "⬛ AMOLED")
        val curIdx = when (prefs.themeMode) {
            KeyboardPrefs.THEME_DAY   -> 0
            KeyboardPrefs.THEME_AMOLED -> 2
            else                      -> 1
        }
        addSegment(b.content, "Выбор темы", modes, curIdx) { idx ->
            prefs.themeMode = when (idx) {
                0    -> KeyboardPrefs.THEME_DAY
                2    -> KeyboardPrefs.THEME_AMOLED
                else -> KeyboardPrefs.THEME_NIGHT
            }
            recreate()
        }

        addDivider(b.content)
        addSectionHeader(b.content, "Цвет акцента (Material You)")
        buildColorPicker()
    }

    private fun buildColorPicker() {
        val d = resources.displayMetrics.density
        val scroll = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins((8*d).toInt(), (4*d).toInt(), (8*d).toInt(), (16*d).toInt())
            }
        }
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding((8*d).toInt(), (8*d).toInt(), (8*d).toInt(), (8*d).toInt())
        }
        KeyboardPrefs.PRESET_COLORS.forEachIndexed { i, color ->
            val isSelected = color == prefs.accentColor
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((72*d).toInt(), -2).apply {
                    setMargins((4*d).toInt(), 0, (4*d).toInt(), 0)
                }
            }
            val circle = android.widget.ImageButton(this).apply {
                val sz = (56*d).toInt()
                layoutParams = LinearLayout.LayoutParams(sz, sz)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    if (isSelected)
                        setStroke((3*d).toInt(), prefs.textColor())
                }
                setImageDrawable(null)
                setOnClickListener {
                    prefs.accentColor = color
                    recreate()
                }
            }
            val lbl = android.widget.TextView(this).apply {
                text = KeyboardPrefs.COLOR_NAMES[i]
                textSize = 10f
                gravity = android.view.Gravity.CENTER
                setTextColor(if (isSelected) prefs.accentColor else prefs.hintTextColor())
                layoutParams = LinearLayout.LayoutParams(-2, -2).apply { topMargin = (4*d).toInt() }
            }
            col.addView(circle); col.addView(lbl); row.addView(col)
        }
        scroll.addView(row); b.content.addView(scroll)
    }
}
