package com.optimistopti.mykeyboard
import android.os.Bundle
import android.widget.*
class GesturesActivity : BaseSettingsActivity() {
    private lateinit var prefs: KeyboardPrefs
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); prefs = KeyboardPrefs(this)
        setTheme(if (prefs.isDarkTheme) R.style.Theme_MyKeyboard_Dark else R.style.Theme_MyKeyboard_Light)
        setContentView(R.layout.activity_gestures); title = "Жесты и свайп"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        addKeyboardFab()
        val c = findViewById<LinearLayout>(R.id.gestures_container)
        addToggle(c,"Свайп-набор слов",prefs.swipeEnabled){prefs.swipeEnabled=it}
        addToggle(c,"Удаление слова свайпом на ⌫",prefs.swipeDeleteEnabled){prefs.swipeDeleteEnabled=it}
    }
    private fun addToggle(c:LinearLayout, name:String, cur:Boolean, save:(Boolean)->Unit) {
        val row=layoutInflater.inflate(R.layout.item_toggle,c,false)
        row.findViewById<TextView>(R.id.toggle_title).text=name
        val sw=row.findViewById<Switch>(R.id.toggle_switch); sw.isChecked=cur
        sw.setOnCheckedChangeListener{_,v->save(v)}; c.addView(row)
    }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}