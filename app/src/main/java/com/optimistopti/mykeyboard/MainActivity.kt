package com.optimistopti.mykeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.optimistopti.mykeyboard.databinding.ActivityMainBinding

class MainActivity : BaseSettingsActivity() {
    private lateinit var b: ActivityMainBinding

    data class Item(val icon: String, val title: String, val sub: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Redirect to setup if keyboard not yet enabled
        if (!isKeyboardEnabled()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setSupportActionBar(b.toolbar)
        supportActionBar?.title = ""
        setupFab(b.fab)
        buildList()
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        if (!::b.isInitialized) return
        updateStatus()
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun buildList() {
        val items = listOf(
            Item("🌐", "Язык",           langSub(),                        LanguageActivity::class.java),
            Item("🎨", "Темы",           "Цвет, Day / Night / AMOLED",     ThemeActivity::class.java),
            Item("🖼️", "Фон",            "Изображение, прозрачность",      BackgroundActivity::class.java),
            Item("📐", "Внешний вид",    "Высота, скругление, отступы",    AppearanceActivity::class.java),
            Item("🔊", "Звук и вибрация","Нажатие, интенсивность",         SoundActivity::class.java),
            Item("⌨️", "Клавиши",        "Цифры, попап, подсказки",        KeysActivity::class.java),
            Item("✏️", "Ввод текста",    "Автокоррекция, предсказание",    TextInputActivity::class.java),
            Item("👆", "Жесты",          "Свайп-набор, удаление жестом",   GesturesActivity::class.java),
            Item("⚙️", "Управление настройками", "Экспорт, импорт, сброс",             SettingsManagerActivity::class.java)
        )

        b.rv.layoutManager = LinearLayoutManager(this)
        b.rv.adapter = object : RecyclerView.Adapter<VH>() {
            override fun onCreateViewHolder(p: ViewGroup, t: Int) =
                VH(layoutInflater.inflate(R.layout.item_setting_md3, p, false))
            override fun getItemCount() = items.size
            override fun onBindViewHolder(h: VH, i: Int) {
                val item = items[i]
                h.icon.text  = item.icon
                h.title.text = item.title
                h.sub.text   = item.sub
                h.root.setOnClickListener { startActivity(Intent(this@MainActivity, item.target)) }
            }
        }
    }

    private fun langSub(): String {
        val m = mapOf("ru" to "Русский", "uk" to "Українська", "en" to "English")
        return prefs.enabledLanguages.split(",").mapNotNull { m[it] }.joinToString(", ")
    }

    private fun updateStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val on = imm.enabledInputMethodList.any { it.packageName == packageName }
        b.tvStatus.text = if (on) "✅ Активна" else "⚠️ Не включена"
        b.tvStatus.setTextColor(if (on) 0xFF4CAF50.toInt() else 0xFFEF5350.toInt())
    }

    inner class VH(val root: View) : RecyclerView.ViewHolder(root) {
        val icon  = root.findViewById<TextView>(R.id.icon)
        val title = root.findViewById<TextView>(R.id.title)
        val sub   = root.findViewById<TextView>(R.id.subtitle)
    }
}
