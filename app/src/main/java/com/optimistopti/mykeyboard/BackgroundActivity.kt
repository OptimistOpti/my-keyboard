package com.optimistopti.mykeyboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class BackgroundActivity : BaseSettingsActivity() {
    private lateinit var prefs: KeyboardPrefs
    private val PICK_IMAGE = 1001

    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_background); title = "Фон клавиатуры"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addKeyboardFab()
        val d = resources.displayMetrics.density
        val c = findViewById<LinearLayout>(R.id.background_container)

        // Current image preview
        val previewLabel = TextView(this).apply {
            text = if (prefs.bgImageUri.isEmpty()) "Фон: не выбран" else "Фон: изображение выбрано ✅"
            textSize = 15f; setTextColor(prefs.textColor())
            setPadding((20*d).toInt(),(16*d).toInt(),(20*d).toInt(),(4*d).toInt())
        }
        c.addView(previewLabel)

        // Pick image button
        val btnPick = android.widget.Button(this).apply {
            text = "Выбрать изображение из галереи"
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins((20*d).toInt(),(8*d).toInt(),(20*d).toInt(),(8*d).toInt())
            }
            setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
                startActivityForResult(intent, PICK_IMAGE)
            }
        }
        c.addView(btnPick)

        // Remove image button
        val btnRemove = android.widget.Button(this).apply {
            text = "Убрать фон"
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins((20*d).toInt(),0,(20*d).toInt(),(16*d).toInt())
            }
            setOnClickListener {
                prefs.bgImageUri = ""
                previewLabel.text = "Фон: не выбран"
                Toast.makeText(this@BackgroundActivity, "Фон убран", Toast.LENGTH_SHORT).show()
            }
        }
        c.addView(btnRemove)

        addSeek(c, d, "Яркость фона", prefs.bgImageOpacity, 0, 100) {
            prefs.bgImageOpacity = it; previewLabel.text = if (prefs.bgImageUri.isEmpty()) "Фон: не выбран" else "Фон: изображение ✅"
        }
        addSeek(c, d, "Размытие фона", prefs.bgBlurRadius, 0, 25) { prefs.bgBlurRadius = it }
        addSeek(c, d, "Прозрачность клавиш (0=прозрачно, 255=непрозрачно)", prefs.keyAlpha, 0, 255) { prefs.keyAlpha = it }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            // Persist permission
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.bgImageUri = uri.toString()
            Toast.makeText(this, "Фон выбран ✅", Toast.LENGTH_SHORT).show()
            recreate()
        }
    }

    private fun addSeek(c: LinearLayout, d: Float, name: String, cur: Int, min: Int, max: Int, save: (Int)->Unit) {
        val label = TextView(this).apply {
            text = "$name: $cur"; textSize = 14f; setTextColor(prefs.textColor())
            setPadding((20*d).toInt(),(12*d).toInt(),(20*d).toInt(),0)
        }
        val seek = SeekBar(this).apply {
            this.max = max-min; progress = cur-min
            setPadding((20*d).toInt(),(4*d).toInt(),(20*d).toInt(),(8*d).toInt())
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
