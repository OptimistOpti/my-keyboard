package com.optimistopti.mykeyboard

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ThemeActivity : AppCompatActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_theme); title = "Темы"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val container = findViewById<LinearLayout>(R.id.theme_container)
        val d = resources.displayMetrics.density

        // Dark/Light toggle
        val darkRow = layoutInflater.inflate(R.layout.item_toggle, container, false)
        darkRow.findViewById<TextView>(R.id.toggle_title).text = "Тёмная тема"
        val sw = darkRow.findViewById<Switch>(R.id.toggle_switch)
        sw.isChecked = prefs.isDarkTheme
        sw.setOnCheckedChangeListener { _, v -> prefs.isDarkTheme = v; recreate() }
        container.addView(darkRow)

        // Color section label
        val label = TextView(this).apply {
            text = "Цвет акцента"; textSize = 13f; setTextColor(prefs.accentColor)
            setPadding((20*d).toInt(),(20*d).toInt(),(20*d).toInt(),(8*d).toInt())
        }
        container.addView(label)

        // Horizontal color strip
        val hsv = HorizontalScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = (16*d).toInt() }
        }
        val strip = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding((12*d).toInt(), 0, (12*d).toInt(), 0)
        }
        setupColors(strip)
        hsv.addView(strip); container.addView(hsv)
    }

    private fun setupColors(strip: LinearLayout) {
        strip.removeAllViews()
        val d = resources.displayMetrics.density
        KeyboardPrefs.PRESET_COLORS.forEachIndexed { i, color ->
            val col = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams((64*d).toInt(), -2).apply {
                    setMargins((8*d).toInt(), 0, (8*d).toInt(), 0)
                }
            }
            val btn = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams((52*d).toInt(), (52*d).toInt())
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL; setColor(color)
                    if (color == prefs.accentColor) setStroke((3*d).toInt(), 0xFFFFFFFF.toInt())
                }
                setOnClickListener { prefs.accentColor = color; setupColors(strip) }
            }
            val lbl = TextView(this).apply {
                text = KeyboardPrefs.COLOR_NAMES[i]; textSize = 10f
                gravity = android.view.Gravity.CENTER; setTextColor(prefs.textColor())
                layoutParams = LinearLayout.LayoutParams(-2, -2).apply { topMargin = (4*d).toInt() }
            }
            col.addView(btn); col.addView(lbl); strip.addView(col)
        }
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
