package com.optimistopti.mykeyboard

import android.content.Intent
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
            KeyboardPrefs.THEME_DAY    -> 0
            KeyboardPrefs.THEME_AMOLED -> 2
            else                       -> 1
        }
        addSegment(b.content, "Выбор темы", modes, curIdx) { idx ->
            prefs.themeMode = when (idx) {
                0    -> KeyboardPrefs.THEME_DAY
                2    -> KeyboardPrefs.THEME_AMOLED
                else -> KeyboardPrefs.THEME_NIGHT
            }
            notifyThemeChanged()
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

            // Color circle
            val circle = android.widget.FrameLayout(this).apply {
                val sz = (56*d).toInt()
                layoutParams = LinearLayout.LayoutParams(sz, sz)
            }
            val bg = android.view.View(this).apply {
                val sz = (56*d).toInt()
                layoutParams = android.widget.FrameLayout.LayoutParams(sz, sz)
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    if (isSelected) setStroke((3*d).toInt(), prefs.textColor())
                }
            }
            val check = android.widget.TextView(this).apply {
                text = if (isSelected) "✓" else ""
                textSize = 20f
                gravity = android.view.Gravity.CENTER
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = android.widget.FrameLayout.LayoutParams(-1, -1)
            }
            circle.addView(bg); circle.addView(check)
            circle.setOnClickListener {
                prefs.accentColor = color
                notifyThemeChanged()
                recreate()
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

    // Broadcast so keyboard service picks up new colors immediately
    private fun notifyThemeChanged() {
        sendBroadcast(Intent(ACTION_THEME_CHANGED).setPackage(packageName))
    }

    companion object {
        const val ACTION_THEME_CHANGED = "com.optimistopti.mykeyboard.THEME_CHANGED"
    }
}
