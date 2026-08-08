package com.optimistopti.mykeyboard

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SoundActivity : AppCompatActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_sound); title = "Звук и вибрация"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val c = findViewById<LinearLayout>(R.id.sound_container)
        val d = resources.displayMetrics.density
        addToggle(c, "Вибрация при нажатии", prefs.vibrateEnabled) { prefs.vibrateEnabled = it }
        addToggle(c, "Звук нажатия", prefs.soundEnabled) { prefs.soundEnabled = it }
        addSeek(c, d, "Длительность вибрации (мс)", prefs.vibrateDurationMs, 10, 90) { prefs.vibrateDurationMs = it }
    }
    private fun addToggle(c: LinearLayout, name: String, cur: Boolean, save: (Boolean) -> Unit) {
        val row = layoutInflater.inflate(R.layout.item_toggle, c, false)
        row.findViewById<TextView>(R.id.toggle_title).text = name
        val sw = row.findViewById<Switch>(R.id.toggle_switch); sw.isChecked = cur
        sw.setOnCheckedChangeListener { _, v -> save(v) }; c.addView(row)
    }
    private fun addSeek(c: LinearLayout, d: Float, name: String, cur: Int, min: Int, max: Int, save: (Int) -> Unit) {
        val label = TextView(this).apply {
            text = "$name: $cur"; textSize = 15f; setTextColor(prefs.textColor())
            setPadding((20*d).toInt(), (12*d).toInt(), (20*d).toInt(), 0)
        }
        val seek = SeekBar(this).apply {
            this.max = max-min; progress = cur-min
            setPadding((20*d).toInt(), (4*d).toInt(), (20*d).toInt(), (8*d).toInt())
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) { val r=v+min; label.text="$name: $r"; save(r) }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        c.addView(label); c.addView(seek)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}
