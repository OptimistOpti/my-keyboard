package com.optimistopti.mykeyboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.android.material.textview.MaterialTextView

abstract class BaseSettingsActivity : AppCompatActivity() {

    protected lateinit var prefs: KeyboardPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = KeyboardPrefs(this)
        applyAppTheme()
    }

    protected fun applyAppTheme() {
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
    }

    protected fun setupToolbarBack(toolbar: Toolbar, title: String) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    protected fun setupFab(fab: FloatingActionButton) {
        fab.setOnClickListener { showKeyboardPreview() }
    }

    protected fun showKeyboardPreview() {
        val et = EditText(this).apply {
            hint = "Введи текст для теста…"
            setPadding(48, 32, 48, 32)
        }
        AlertDialog.Builder(this)
            .setTitle("🎹 Тест клавиатуры")
            .setView(et)
            .setPositiveButton("Закрыть", null)
            .show()
        et.post {
            et.requestFocus()
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    // ── Builder helpers ───────────────────────────────────────────────────────

    protected fun addSectionHeader(container: LinearLayout, text: String) {
        val d = resources.displayMetrics.density
        val tv = MaterialTextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(prefs.accentColor)
            letterSpacing = 0.05f
            setPadding((24*d).toInt(), (20*d).toInt(), (24*d).toInt(), (6*d).toInt())
        }
        container.addView(tv)
    }

    protected fun addToggle(
        container: LinearLayout,
        title: String,
        subtitle: String = "",
        current: Boolean,
        onChanged: (Boolean) -> Unit
    ) {
        val row = layoutInflater.inflate(R.layout.item_toggle, container, false)
        row.findViewById<TextView>(R.id.toggle_title).text = title
        val sub = row.findViewById<TextView>(R.id.toggle_subtitle)
        if (subtitle.isNotEmpty()) { sub.text = subtitle; sub.visibility = android.view.View.VISIBLE }
        val sw = row.findViewById<MaterialSwitch>(R.id.toggle_switch)
        sw.isChecked = current
        sw.setOnCheckedChangeListener { _, v -> onChanged(v) }
        container.addView(row)
    }

    protected fun addSlider(
        container: LinearLayout,
        title: String,
        current: Float,
        from: Float,
        to: Float,
        stepSize: Float = 1f,
        labelFormatter: (Float) -> String = { it.toInt().toString() },
        onChanged: (Float) -> Unit
    ) {
        val d = resources.displayMetrics.density
        val label = MaterialTextView(this).apply {
            text = "$title: ${labelFormatter(current)}"
            textSize = 14f
            setTextColor(prefs.textColor())
            setPadding((24*d).toInt(), (14*d).toInt(), (24*d).toInt(), 0)
        }
        val slider = Slider(this).apply {
            valueFrom = from; valueTo = to; value = current.coerceIn(from, to)
            this.stepSize = stepSize
            setPadding((12*d).toInt(), (4*d).toInt(), (12*d).toInt(), (4*d).toInt())
            addOnChangeListener { _, v, _ ->
                label.text = "$title: ${labelFormatter(v)}"
                onChanged(v)
            }
        }
        container.addView(label)
        container.addView(slider)
    }

    protected fun addDivider(container: LinearLayout) {
        val d = resources.displayMetrics.density
        val v = android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 1).apply {
                setMargins((16*d).toInt(), (8*d).toInt(), (16*d).toInt(), (8*d).toInt())
            }
            setBackgroundColor(getColor(android.R.color.transparent))
            background = android.graphics.drawable.ColorDrawable(
                (prefs.textColor() and 0x00FFFFFF) or 0x18000000
            )
        }
        container.addView(v)
    }
}
