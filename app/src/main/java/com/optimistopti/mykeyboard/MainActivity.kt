package com.optimistopti.mykeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : BaseSettingsActivity() {
    private lateinit var prefs: KeyboardPrefs

    data class SettingsItem(val icon: String, val title: String, val subtitle: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_main)
        addKeyboardFab()
        setupList()
        updateStatus()
    }

    override fun onResume() { super.onResume(); updateStatus() }

    private fun setupList() {
        val items = listOf(
            SettingsItem("🌐", "Язык", langSubtitle(), LanguageActivity::class.java),
            SettingsItem("🎨", "Темы", "Цвет акцента, тёмная/светлая тема", ThemeActivity::class.java),
            SettingsItem("🖼️", "Фон клавиатуры", "Изображение, прозрачность, размытие", BackgroundActivity::class.java),
            SettingsItem("📐", "Внешний вид", "Высота, отступы, скругление, прозрачность клавиш", AppearanceActivity::class.java),
            SettingsItem("🔊", "Звук и вибрация", "Нажатие, интенсивность", SoundActivity::class.java),
            SettingsItem("⌨️", "Клавиши", "Ряд цифр, подсказки, попап", KeysActivity::class.java),
            SettingsItem("✏️", "Ввод текста", "Автокоррекция, подсказки слов", TextInputActivity::class.java),
            SettingsItem("👆", "Жесты и свайп", "Свайп-ввод, удаление жестом", GesturesActivity::class.java)
        )
        val rv = findViewById<RecyclerView>(R.id.rv_settings)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            inner class VH(val root: android.view.View) : RecyclerView.ViewHolder(root) {
                val icon  = root.findViewById<TextView>(R.id.item_icon)
                val title = root.findViewById<TextView>(R.id.item_title)
                val sub   = root.findViewById<TextView>(R.id.item_subtitle)
            }
            override fun onCreateViewHolder(parent: android.view.ViewGroup, vt: Int) =
                VH(layoutInflater.inflate(R.layout.item_settings, parent, false))
            override fun getItemCount() = items.size
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
                val h = holder as VH; val item = items[pos]
                h.icon.text = item.icon; h.title.text = item.title; h.sub.text = item.subtitle
                h.root.setOnClickListener { startActivity(Intent(this@MainActivity, item.target)) }
            }
        }
        findViewById<android.widget.Button>(R.id.btn_enable).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        findViewById<android.widget.Button>(R.id.btn_select).setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
    }

    private fun langSubtitle(): String {
        val map = mapOf("ru" to "Русский","uk" to "Українська","en" to "English")
        return prefs.enabledLanguages.split(",").mapNotNull { map[it] }.joinToString(", ")
    }

    private fun updateStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val tv = findViewById<TextView>(R.id.tv_status)
        if (enabled) { tv.text = "✅ Активна"; tv.setTextColor(0xFF4CAF50.toInt()) }
        else { tv.text = "⚠️ Нажми «Включить»"; tv.setTextColor(0xFFF44336.toInt()) }
    }
}
