package com.optimistopti.mykeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import com.optimistopti.mykeyboard.databinding.ActivityMainBinding

class MainActivity : BaseSettingsActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: KeyboardPrefs

    data class Item(val icon: String, val title: String, val sub: String, val target: Class<*>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = KeyboardPrefs(this)
        applyAppTheme(prefs)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar(binding.toolbar, null)
        setupContent()
        updateStatus()
    }

    override fun onResume() { super.onResume(); updateStatus() }

    private fun setupContent() {
        val items = listOf(
            Item("🌐", "Язык",         langSubtitle(),                      LanguageActivity::class.java),
            Item("🎨", "Темы",         "Цвет, светлая/тёмная",              ThemeActivity::class.java),
            Item("🖼️", "Фон",          "Изображение, прозрачность",         BackgroundActivity::class.java),
            Item("📐", "Внешний вид",  "Высота, скругление, отступы",       AppearanceActivity::class.java),
            Item("🔊", "Звук и вибрация","Нажатие, интенсивность",          SoundActivity::class.java),
            Item("⌨️", "Клавиши",      "Цифры, попап, подсказки",           KeysActivity::class.java),
            Item("✏️", "Ввод текста",  "Автокоррекция, предсказание",       TextInputActivity::class.java),
            Item("👆", "Жесты",        "Свайп-набор, удаление жестом",      GesturesActivity::class.java)
        )

        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.adapter = object : RecyclerView.Adapter<SettingVH>() {
            override fun onCreateViewHolder(p: ViewGroup, t: Int) =
                SettingVH(layoutInflater.inflate(R.layout.item_setting_md3, p, false))
            override fun getItemCount() = items.size
            override fun onBindViewHolder(h: SettingVH, i: Int) {
                val item = items[i]
                h.icon.text  = item.icon
                h.title.text = item.title
                h.sub.text   = item.sub
                h.root.setOnClickListener { startActivity(Intent(this@MainActivity, item.target)) }
            }
        }

        binding.btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        binding.btnSelect.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }

        binding.fab.setOnClickListener { showKeyboardPreview() }
    }

    private fun langSubtitle(): String {
        val map = mapOf("ru" to "Русский", "uk" to "Українська", "en" to "English")
        return prefs.enabledLanguages.split(",").mapNotNull { map[it] }.joinToString(", ")
    }

    private fun updateStatus() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val on = imm.enabledInputMethodList.any { it.packageName == packageName }
        binding.tvStatus.text = if (on) "✅ Активна" else "⚠️ Клавиатура не включена"
        val col = if (on) getColor(com.google.android.material.R.color.m3_sys_color_light_tertiary)
                  else    getColor(com.google.android.material.R.color.design_error)
        binding.tvStatus.setTextColor(col)
        binding.btnEnable.visibility = if (on) View.GONE else View.VISIBLE
    }

    inner class SettingVH(val root: View) : RecyclerView.ViewHolder(root) {
        val icon  = root.findViewById<TextView>(R.id.icon)
        val title = root.findViewById<TextView>(R.id.title)
        val sub   = root.findViewById<TextView>(R.id.subtitle)
    }
}
