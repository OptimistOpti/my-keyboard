package com.optimistopti.mykeyboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.optimistopti.mykeyboard.databinding.ActivitySettingsBaseBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class SettingsManagerActivity : BaseSettingsActivity() {
    private lateinit var b: ActivitySettingsBaseBinding
    private val PICK_JSON  = 2001
    private val SAVE_JSON  = 2002

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySettingsBaseBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbarBack(b.toolbar, "Управление настройками")
        setupFab(b.fab)

        val d = resources.displayMetrics.density

        addSectionHeader(b.content, "Экспорт и импорт")

        // Export button
        addActionButton(b.content, d, "📤  Экспортировать настройки",
            "Сохранить все настройки в JSON-файл") {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "keyboard_you_settings.json")
            }
            startActivityForResult(intent, SAVE_JSON)
        }

        // Import button
        addActionButton(b.content, d, "📥  Импортировать настройки",
            "Загрузить настройки из JSON-файла") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, PICK_JSON)
        }

        addDivider(b.content)
        addSectionHeader(b.content, "Сброс")

        // Reset button
        addActionButton(b.content, d, "🔄  Сбросить всё до заводских",
            "Все настройки вернутся к значениям по умолчанию") {
            MaterialAlertDialogBuilder(this)
                .setTitle("Сбросить настройки?")
                .setMessage("Все настройки будут сброшены до значений по умолчанию. Это действие нельзя отменить.")
                .setPositiveButton("Сбросить") { _, _ ->
                    prefs.resetToDefaults()
                    Snackbar.make(b.root, "✅ Настройки сброшены", Snackbar.LENGTH_LONG)
                        .setAction("OK") {}
                        .show()
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        addDivider(b.content)
        addSectionHeader(b.content, "О настройках")
        addInfoText(b.content, d,
            "Настройки хранятся локально на устройстве и сохраняются при закрытии приложения. " +
            "Используй экспорт для резервной копии или переноса на другое устройство.")
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        when {
            req == SAVE_JSON && res == Activity.RESULT_OK -> {
                val uri = data?.data ?: return
                try {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(prefs.exportToJson().toByteArray())
                    }
                    Snackbar.make(b.root, "✅ Настройки экспортированы", Snackbar.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            req == PICK_JSON && res == Activity.RESULT_OK -> {
                val uri = data?.data ?: return
                try {
                    val json = contentResolver.openInputStream(uri)?.use { inp ->
                        BufferedReader(InputStreamReader(inp)).readText()
                    } ?: return
                    if (prefs.importFromJson(json)) {
                        Snackbar.make(b.root, "✅ Настройки импортированы", Snackbar.LENGTH_LONG)
                            .setAction("Перезапустить") { recreate() }
                            .show()
                    } else {
                        Toast.makeText(this, "❌ Неверный формат файла", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun addActionButton(
        container: android.widget.LinearLayout,
        d: Float,
        title: String,
        subtitle: String,
        onClick: () -> Unit
    ) {
        val card = com.google.android.material.card.MaterialCardView(this).apply {
            layoutParams = android.widget.LinearLayout.LayoutParams(-1, -2).apply {
                setMargins((12*d).toInt(), (4*d).toInt(), (12*d).toInt(), (4*d).toInt())
            }
            radius = (12*d)
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }
        }
        val inner = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding((16*d).toInt(), (14*d).toInt(), (16*d).toInt(), (14*d).toInt())
        }
        val tv1 = com.google.android.material.textview.MaterialTextView(this).apply {
            text = title; textSize = 15f; setTextColor(prefs.textColor())
        }
        val tv2 = com.google.android.material.textview.MaterialTextView(this).apply {
            text = subtitle; textSize = 12f; setTextColor(prefs.hintTextColor())
            setPadding(0, (3*d).toInt(), 0, 0)
        }
        inner.addView(tv1); inner.addView(tv2)
        card.addView(inner); container.addView(card)
    }

    private fun addInfoText(container: android.widget.LinearLayout, d: Float, text: String) {
        val tv = com.google.android.material.textview.MaterialTextView(this).apply {
            this.text = text; textSize = 13f; setTextColor(prefs.hintTextColor())
            setPadding((20*d).toInt(), (8*d).toInt(), (20*d).toInt(), (16*d).toInt())
        }
        container.addView(tv)
    }
}
