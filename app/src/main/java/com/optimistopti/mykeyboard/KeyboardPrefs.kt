package com.optimistopti.mykeyboard

import android.content.Context
import android.content.SharedPreferences

class KeyboardPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    companion object {
        val PRESET_COLORS = listOf(
            0xFF6750A4.toInt(), 0xFF0061A4.toInt(), 0xFF006E2C.toInt(),
            0xFFB3261E.toInt(), 0xFF7B4F00.toInt(), 0xFF006A6A.toInt(),
            0xFFE94560.toInt(), 0xFF4A4458.toInt()
        )
        val COLOR_NAMES = listOf("Фиолетовый","Синий","Зелёный","Красный","Золотой","Бирюзовый","Розовый","Серый")
    }

    var accentColor: Int
        get() = prefs.getInt("accent_color", PRESET_COLORS[0])
        set(v) = prefs.edit().putInt("accent_color", v).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean("dark_theme", true)
        set(v) = prefs.edit().putBoolean("dark_theme", v).apply()

    var keyRadius: Float
        get() = prefs.getFloat("key_radius", 10f)
        set(v) = prefs.edit().putFloat("key_radius", v).apply()

    var keyHeightDp: Int
        get() = prefs.getInt("key_height", 52)
        set(v) = prefs.edit().putInt("key_height", v).apply()

    var keyPaddingDp: Int
        get() = prefs.getInt("key_padding", 5)
        set(v) = prefs.edit().putInt("key_padding", v).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate", true)
        set(v) = prefs.edit().putBoolean("vibrate", v).apply()

    var vibrateDurationMs: Int
        get() = prefs.getInt("vibrate_ms", 25)
        set(v) = prefs.edit().putInt("vibrate_ms", v).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", false)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    var showNumberRow: Boolean
        get() = prefs.getBoolean("number_row", false)
        set(v) = prefs.edit().putBoolean("number_row", v).apply()

    var showCommaKey: Boolean
        get() = prefs.getBoolean("comma_key", true)
        set(v) = prefs.edit().putBoolean("comma_key", v).apply()

    var showTopHints: Boolean
        get() = prefs.getBoolean("top_hints", true)
        set(v) = prefs.edit().putBoolean("top_hints", v).apply()

    var autocorrectEnabled: Boolean
        get() = prefs.getBoolean("autocorrect", true)
        set(v) = prefs.edit().putBoolean("autocorrect", v).apply()

    var predictionEnabled: Boolean
        get() = prefs.getBoolean("prediction", true)
        set(v) = prefs.edit().putBoolean("prediction", v).apply()

    var swipeEnabled: Boolean
        get() = prefs.getBoolean("swipe", true)
        set(v) = prefs.edit().putBoolean("swipe", v).apply()

    var swipeDeleteEnabled: Boolean
        get() = prefs.getBoolean("swipe_delete", true)
        set(v) = prefs.edit().putBoolean("swipe_delete", v).apply()

    var popupEnabled: Boolean
        get() = prefs.getBoolean("popup", true)
        set(v) = prefs.edit().putBoolean("popup", v).apply()

    // enabled languages list (comma-separated)
    var enabledLanguages: String
        get() = prefs.getString("enabled_langs", "ru,en") ?: "ru,en"
        set(v) = prefs.edit().putString("enabled_langs", v).apply()

    var primaryLanguage: String
        get() = prefs.getString("primary_lang", "ru") ?: "ru"
        set(v) = prefs.edit().putString("primary_lang", v).apply()

    // theme colors
    fun bgColor()          = if (isDarkTheme) 0xFF1C1B1F.toInt() else 0xFFFEF7FF.toInt()
    fun keyColor()         = if (isDarkTheme) 0xFF49454F.toInt() else 0xFFFFFFFF.toInt()
    fun specialKeyColor()  = if (isDarkTheme) 0xFF332D41.toInt() else 0xFFE8DEF8.toInt()
    fun surfaceColor()     = if (isDarkTheme) 0xFF2B2930.toInt() else 0xFFF3EFF4.toInt()
    fun textColor()        = if (isDarkTheme) 0xFFE6E1E5.toInt() else 0xFF1C1B1F.toInt()
    fun hintTextColor()    = if (isDarkTheme) 0xFF938F99.toInt() else 0xFF79747E.toInt()
    fun shadowColor()      = if (isDarkTheme) 0x44000000 else 0x22000000
    fun accentTextColor()  = 0xFFFFFFFF.toInt()
}
