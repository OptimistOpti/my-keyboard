package com.optimistopti.mykeyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

class KeyboardPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_KEY_RADIUS = "key_radius"
        const val KEY_VIBRATE = "vibrate"
        const val KEY_SOUND = "sound"
        const val KEY_KEY_HEIGHT = "key_height"

        // Default accent colors
        val PRESET_COLORS = listOf(
            0xFF6750A4.toInt(), // Material Purple (default)
            0xFF0061A4.toInt(), // Blue
            0xFF006E2C.toInt(), // Green
            0xFFB3261E.toInt(), // Red
            0xFF6B5E2F.toInt(), // Yellow/Gold
            0xFF006A6A.toInt(), // Teal
            0xFFE94560.toInt(), // Pink/Red
            0xFF4A4458.toInt(), // Grey Purple
        )
    }

    var accentColor: Int
        get() = prefs.getInt(KEY_ACCENT_COLOR, PRESET_COLORS[0])
        set(v) = prefs.edit().putInt(KEY_ACCENT_COLOR, v).apply()

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(v) = prefs.edit().putBoolean(KEY_DARK_THEME, v).apply()

    var keyRadius: Float
        get() = prefs.getFloat(KEY_KEY_RADIUS, 10f)
        set(v) = prefs.edit().putFloat(KEY_KEY_RADIUS, v).apply()

    var vibrateEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATE, true)
        set(v) = prefs.edit().putBoolean(KEY_VIBRATE, v).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, false)
        set(v) = prefs.edit().putBoolean(KEY_SOUND, v).apply()

    var keyHeightDp: Int
        get() = prefs.getInt(KEY_KEY_HEIGHT, 52)
        set(v) = prefs.edit().putInt(KEY_KEY_HEIGHT, v).apply()

    // Derived theme colors
    fun bgColor(): Int = if (isDarkTheme) 0xFF1C1B1F.toInt() else 0xFFFEF7FF.toInt()
    fun keyColor(): Int = if (isDarkTheme) 0xFF49454F.toInt() else 0xFFFFFFFF.toInt()
    fun specialKeyColor(): Int = if (isDarkTheme) 0xFF332D41.toInt() else 0xFFE8DEF8.toInt()
    fun textColor(): Int = if (isDarkTheme) 0xFFE6E1E5.toInt() else 0xFF1C1B1F.toInt()
    fun hintTextColor(): Int = if (isDarkTheme) 0xFF938F99.toInt() else 0xFF79747E.toInt()
    fun shadowColor(): Int = if (isDarkTheme) 0x33000000 else 0x22000000
    fun pressedColor(): Int = accentColor and 0x00FFFFFF or 0x33000000
    fun accentTextColor(): Int = 0xFFFFFFFF.toInt()
}
