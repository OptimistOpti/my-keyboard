package com.optimistopti.mykeyboard

import android.content.Context
import android.content.SharedPreferences

class KeyboardPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)

    companion object {
        const val THEME_DAY   = "day"
        const val THEME_NIGHT = "night"
        const val THEME_AMOLED = "amoled"

        val PRESET_COLORS = listOf(
            0xFF6750A4.toInt(), // Purple
            0xFF0061A4.toInt(), // Blue
            0xFF006E2C.toInt(), // Green
            0xFFB3261E.toInt(), // Red
            0xFF7B4F00.toInt(), // Gold
            0xFF006A6A.toInt(), // Teal
            0xFFE94560.toInt(), // Pink
            0xFF4A4458.toInt()  // Grey
        )
        val COLOR_NAMES = listOf("Фиолетовый","Синий","Зелёный","Красный","Золотой","Бирюзовый","Розовый","Серый")
    }

    // ── Theme ──────────────────────────────────────────────────────────────
    var themeMode: String
        get() = prefs.getString("theme_mode", THEME_NIGHT) ?: THEME_NIGHT
        set(v) = prefs.edit().putString("theme_mode", v).apply()

    val isDarkTheme: Boolean
        get() = themeMode != THEME_DAY

    val isAmoled: Boolean
        get() = themeMode == THEME_AMOLED

    // ── Accent color ───────────────────────────────────────────────────────
    var accentColor: Int
        get() = prefs.getInt("accent_color", PRESET_COLORS[0])
        set(v) = prefs.edit().putInt("accent_color", v).apply()

    // ── Keyboard geometry ──────────────────────────────────────────────────
    var keyRadius: Float
        get() = prefs.getFloat("key_radius", 10f)
        set(v) = prefs.edit().putFloat("key_radius", v).apply()

    var keyHeightDp: Int
        get() = prefs.getInt("key_height", 52)
        set(v) = prefs.edit().putInt("key_height", v).apply()

    var keyPaddingDp: Int
        get() = prefs.getInt("key_padding", 5)
        set(v) = prefs.edit().putInt("key_padding", v).apply()

    var keyAlpha: Int
        get() = prefs.getInt("key_alpha", 255)
        set(v) = prefs.edit().putInt("key_alpha", v).apply()

    // ── Background ─────────────────────────────────────────────────────────
    var bgImageUri: String
        get() = prefs.getString("bg_image_uri", "") ?: ""
        set(v) = prefs.edit().putString("bg_image_uri", v).apply()

    var bgImageOpacity: Int
        get() = prefs.getInt("bg_image_opacity", 60)
        set(v) = prefs.edit().putInt("bg_image_opacity", v).apply()

    // ── Sound / haptic ────────────────────────────────────────────────────
    var vibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate", true)
        set(v) = prefs.edit().putBoolean("vibrate", v).apply()

    var vibrateDurationMs: Int
        get() = prefs.getInt("vibrate_ms", 25)
        set(v) = prefs.edit().putInt("vibrate_ms", v).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound", false)
        set(v) = prefs.edit().putBoolean("sound", v).apply()

    // ── Keys ───────────────────────────────────────────────────────────────
    var showNumberRow: Boolean
        get() = prefs.getBoolean("number_row", false)
        set(v) = prefs.edit().putBoolean("number_row", v).apply()

    var showCommaKey: Boolean
        get() = prefs.getBoolean("comma_key", true)
        set(v) = prefs.edit().putBoolean("comma_key", v).apply()

    var showTopHints: Boolean
        get() = prefs.getBoolean("top_hints", true)
        set(v) = prefs.edit().putBoolean("top_hints", v).apply()

    var popupEnabled: Boolean
        get() = prefs.getBoolean("popup", true)
        set(v) = prefs.edit().putBoolean("popup", v).apply()

    // ── Text input ─────────────────────────────────────────────────────────
    var autocorrectEnabled: Boolean
        get() = prefs.getBoolean("autocorrect", true)
        set(v) = prefs.edit().putBoolean("autocorrect", v).apply()

    var predictionEnabled: Boolean
        get() = prefs.getBoolean("prediction", true)
        set(v) = prefs.edit().putBoolean("prediction", v).apply()

    // ── Gestures ───────────────────────────────────────────────────────────
    var swipeEnabled: Boolean
        get() = prefs.getBoolean("swipe", true)
        set(v) = prefs.edit().putBoolean("swipe", v).apply()

    var swipeDeleteEnabled: Boolean
        get() = prefs.getBoolean("swipe_delete", true)
        set(v) = prefs.edit().putBoolean("swipe_delete", v).apply()

    // ── Language ───────────────────────────────────────────────────────────
    var enabledLanguages: String
        get() = prefs.getString("enabled_langs", "ru,en") ?: "ru,en"
        set(v) = prefs.edit().putString("enabled_langs", v).apply()

    var primaryLanguage: String
        get() = prefs.getString("primary_lang", "ru") ?: "ru"
        set(v) = prefs.edit().putString("primary_lang", v).apply()

    // ── Color helpers ──────────────────────────────────────────────────────
    fun bgColor() = when (themeMode) {
        THEME_DAY   -> 0xFFFEF7FF.toInt()
        THEME_AMOLED -> 0xFF000000.toInt()
        else        -> 0xFF141218.toInt()
    }
    fun keyColor() = when (themeMode) {
        THEME_DAY    -> 0xFFFFFFFF.toInt()
        THEME_AMOLED -> 0xFF0D0D0D.toInt()
        else         -> 0xFF2B2930.toInt()
    }
    fun specialKeyColor() = when (themeMode) {
        THEME_DAY    -> 0xFFE8DEF8.toInt()
        THEME_AMOLED -> 0xFF1A0040.toInt()
        else         -> 0xFF332D41.toInt()
    }
    fun surfaceColor() = when (themeMode) {
        THEME_DAY    -> 0xFFF3EFF4.toInt()
        THEME_AMOLED -> 0xFF000000.toInt()
        else         -> 0xFF1C1B1F.toInt()
    }
    fun textColor() = when (themeMode) {
        THEME_DAY -> 0xFF1C1B1F.toInt()
        else      -> 0xFFE6E1E5.toInt()
    }
    fun hintTextColor() = when (themeMode) {
        THEME_DAY -> 0xFF79747E.toInt()
        else      -> 0xFF938F99.toInt()
    }
    fun shadowColor() = when (themeMode) {
        THEME_DAY -> 0x22000000
        else      -> 0x55000000
    }
    fun accentTextColor() = 0xFFFFFFFF.toInt()
    fun keyColorAlpha(): Int = (keyColor() and 0x00FFFFFF) or (keyAlpha shl 24)
    fun specialKeyColorAlpha(): Int = (specialKeyColor() and 0x00FFFFFF) or (keyAlpha shl 24)

    // FlorisBoard-style space bar — slightly different from regular keys
    fun spaceBarColor() = when (themeMode) {
        THEME_DAY    -> 0xFFE8E0F0.toInt()  // slightly tinted
        THEME_AMOLED -> 0xFF111111.toInt()  // dark but visible
        else         -> 0xFF3A3540.toInt()  // lighter than key bg
    }
}
