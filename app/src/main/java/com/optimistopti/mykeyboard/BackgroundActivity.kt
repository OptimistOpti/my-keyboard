package com.optimistopti.mykeyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding

class BackgroundActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    private val PICK = 1001
    private lateinit var statusTv: TextView

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Фон клавиатуры")
        setupFab(b.fab)

        addSectionHeader(b.content, "Изображение")

        val d = resources.displayMetrics.density
        // Status text
        statusTv = android.widget.TextView(this).apply {
            text = if (prefs.bgImageUri.isEmpty()) "Фон не выбран" else "✅ Изображение выбрано"
            textSize = 14f; setTextColor(prefs.hintTextColor())
            setPadding((24*d).toInt(), (4*d).toInt(), (24*d).toInt(), (8*d).toInt())
        }
        b.content.addView(statusTv)

        // Pick button
        val btnRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding((16*d).toInt(), (4*d).toInt(), (16*d).toInt(), (8*d).toInt())
        }
        val btnPick = MaterialButton(this).apply {
            text = "Выбрать из галереи"
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = (8*d).toInt() }
            setOnClickListener { startActivityForResult(Intent(Intent.ACTION_PICK).apply { type = "image/*" }, PICK) }
        }
        val btnClear = MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle).apply {
            text = "Убрать фон"
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            setOnClickListener {
                prefs.bgImageUri = ""; statusTv.text = "Фон не выбран"
                Snackbar.make(b.root, "Фон убран", Snackbar.LENGTH_SHORT).show()
            }
        }
        btnRow.addView(btnPick); btnRow.addView(btnClear); b.content.addView(btnRow)

        addDivider(b.content)
        addSectionHeader(b.content, "Настройка фона")
        addSlider(b.content, "Яркость фона", prefs.bgImageOpacity.toFloat(), 0f, 100f,
            labelFormatter = { "${it.toInt()}%" }) { prefs.bgImageOpacity = it.toInt() }

        addDivider(b.content)
        addSectionHeader(b.content, "Клавиши поверх фона")
        addSlider(b.content, "Непрозрачность клавиш", prefs.keyAlpha.toFloat(), 0f, 255f,
            labelFormatter = { "${(it/255*100).toInt()}%" }) { prefs.keyAlpha = it.toInt() }
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK && res == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.bgImageUri = uri.toString()
            statusTv.text = "✅ Изображение выбрано"
            Snackbar.make(b.root, "Фон сохранён ✅", Snackbar.LENGTH_SHORT).show()
        }
    }
}
