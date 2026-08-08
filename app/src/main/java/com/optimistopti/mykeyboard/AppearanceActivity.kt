package com.optimistopti.mykeyboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AppearanceActivity : AppCompatActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_appearance); title = "Внешний вид"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val container = findViewById<LinearLayout>(R.id.appearance_container)
        val d = resources.displayMetrics.density
        addSeek(container, d, "Высота клавиатуры", prefs.keyHeightDp, 36, 68) { prefs.keyHeightDp = it }
        addSeek(container, d, "Скругление клавиш", prefs.keyRadius.toInt(), 0, 24) { prefs.keyRadius = it.toFloat() }
        addSeek(container, d, "Отступы между клавишами", prefs.keyPaddingDp, 2, 10) { prefs.keyPaddingDp = it }
    }
    private fun addSeek(c: LinearLayout, d: Float, name: String, cur: Int, min: Int, max: Int, save: (Int) -> Unit) {
        val label = TextView(this).apply {
            text = "$name: $cur"; textSize = 15f; setTextColor(prefs.textColor())
            setPadding((20*d).toInt(), (12*d).toInt(), (20*d).toInt(), 0)
        }
        val seek = SeekBar(this).apply {
            this.max = max - min; progress = cur - min
            setPadding((20*d).toInt(), (4*d).toInt(), (20*d).toInt(), (8*d).toInt())
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) {
                    val real = v + min; label.text = "$name: $real"; save(real)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        c.addView(label); c.addView(seek)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
