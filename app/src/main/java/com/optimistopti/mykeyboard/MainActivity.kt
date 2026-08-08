package com.optimistopti.mykeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: KeyboardPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = KeyboardPrefs(this)
        applyTheme()
        setContentView(R.layout.activity_main)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun applyTheme() {
        if (prefs.isDarkTheme) setTheme(R.style.Theme_MyKeyboard_Dark)
        else setTheme(R.style.Theme_MyKeyboard_Light)
    }

    private fun setupUI() {
        // Status buttons
        findViewById<Button>(R.id.btn_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        findViewById<Button>(R.id.btn_select).setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }

        // Theme toggle
        val switchTheme = findViewById<Switch>(R.id.switch_theme)
        switchTheme.isChecked = prefs.isDarkTheme
        switchTheme.setOnCheckedChangeListener { _, checked ->
            prefs.isDarkTheme = checked
            recreate()
        }

        // Vibration toggle
        val switchVibrate = findViewById<Switch>(R.id.switch_vibrate)
        switchVibrate.isChecked = prefs.vibrateEnabled
        switchVibrate.setOnCheckedChangeListener { _, checked ->
            prefs.vibrateEnabled = checked
        }

        // Key radius seekbar
        val seekRadius = findViewById<SeekBar>(R.id.seek_radius)
        seekRadius.progress = prefs.keyRadius.toInt()
        seekRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) { prefs.keyRadius = v.toFloat() }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Key height seekbar
        val seekHeight = findViewById<SeekBar>(R.id.seek_height)
        seekHeight.progress = prefs.keyHeightDp - 36
        seekHeight.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, v: Int, u: Boolean) { prefs.keyHeightDp = v + 36 }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Color presets
        setupColorPicker()
        updateStatus()
    }

    private fun setupColorPicker() {
        val container = findViewById<LinearLayout>(R.id.color_picker_container)
        container.removeAllViews()
        KeyboardPrefs.PRESET_COLORS.forEach { color ->
            val btn = ImageButton(this).apply {
                val size = (52 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size).also { it.setMargins(8, 8, 8, 8) }
                val bg = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                    if (color == prefs.accentColor) setStroke((3 * resources.displayMetrics.density).toInt(), 0xFFFFFFFF.toInt())
                }
                background = bg
                setOnClickListener {
                    prefs.accentColor = color
                    setupColorPicker() // refresh borders
                }
            }
            container.addView(btn)
        }
    }

    private fun updateStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val tv = findViewById<TextView>(R.id.tv_status)
        tv.text = if (enabled) "✅ Клавиатура включена" else "⚠️ Нажми «Включить»"
        tv.setTextColor(if (enabled) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
    }
}
