package com.optimistopti.mykeyboard

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

class KeyboardPrefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("keyboard_prefs", Context.MODE_PRIVATE)
    private val ctx = context.applicationContext

    companion object {
        const val THEME_DAY    = "day"
        const val THEME_NIGHT  = "night"
        const val THEME_AMOLED = "amoled"

        // FlorisBoard Floris Night exact palette
        val PRESET_COLORS = listOf(
            0xFF1565C0.toInt(),  // Blue (Floris primary)
            0xFF6750A4.toInt(),  // Purple (MD3)
            0xFF006E2C.toInt(),  // Green
            0xFFB3261E.toInt(),  // Red
            0xFF7B4F00.toInt(),  // Gold
            0xFF006A6A.toInt(),  // Teal
            0xFFE94560.toInt(),  // Pink
            0xFF4A4458.toInt()   // Grey
        )
        val COLOR_NAMES = listOf("Синий","Фиолетовый","Зелёный","Красный","Золотой","Бирюзовый","Розовый","Серый")

        // FlorisBoard Night defaults
        private const val DEFAULT_KEY_BG       = 0xFF2B2B2B.toInt()   // key.background
        private const val DEFAULT_KEY_SPEC_BG  = 0xFF383838.toInt()   // shift/del/num bg
        private const val DEFAULT_KB_BG        = 0xFF1E1E1E.toInt()   // keyboard.background
        private const val DEFAULT_SPACE_BG     = 0xFF3A3A3A.toInt()   // key:space
        private const val DEFAULT_TEXT         = 0xFFEEEEEE.toInt()   // key.foreground
        private const val DEFAULT_HINT         = 0xFF888888.toInt()   // hint text
        private const val DEFAULT_SHADOW       = 0x55000000           // shadow
        private const val DEFAULT_ACCENT       = 0xFF1565C0.toInt()   // window.colorPrimary
    }

    // ── Theme ──────────────────────────────────────────────────────────────
    var themeMode: String
        get() = prefs.getString("theme_mode", THEME_NIGHT) ?: THEME_NIGHT
        set(v) = prefs.edit().putString("theme_mode", v).apply()

    val isDarkTheme get() = themeMode != THEME_DAY
    val isAmoled    get() = themeMode == THEME_AMOLED

    // ── Accent ─────────────────────────────────────────────────────────────
    var accentColor: Int
        get() = prefs.getInt("accent_color", DEFAULT_ACCENT)
        set(v) = prefs.edit().putInt("accent_color", v).apply()

    // ── Geometry ───────────────────────────────────────────────────────────
    var keyRadius: Float
        get() = prefs.getFloat("key_radius", 8f)   // FlorisBoard uses ~8dp
        set(v) = prefs.edit().putFloat("key_radius", v).apply()

    var keyHeightDp: Int
        get() = prefs.getInt("key_height", 48)      // FlorisBoard default ~48dp
        set(v) = prefs.edit().putInt("key_height", v).apply()

    var keyPaddingDp: Int
        get() = prefs.getInt("key_padding", 4)      // FlorisBoard ~4dp gaps
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

    // ── Sound / haptic ─────────────────────────────────────────────────────
    var vibrateEnabled: Boolean
        get() = prefs.getBoolean("vibrate", true)
        set(v) = prefs.edit().putBoolean("vibrate", v).apply()

    var vibrateDurationMs: Int
        get() = prefs.getInt("vibrate_ms", 22)
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

    // ── FlorisBoard Night color palette ────────────────────────────────────
    fun bgColor() = when (themeMode) {
        THEME_DAY    -> 0xFFEEEEEE.toInt()   // Floris Day keyboard bg
        THEME_AMOLED -> 0xFF000000.toInt()
        else         -> DEFAULT_KB_BG         // 0xFF1E1E1E — Floris Night
    }
    fun keyColor() = when (themeMode) {
        THEME_DAY    -> 0xFFFFFFFF.toInt()
        THEME_AMOLED -> 0xFF111111.toInt()
        else         -> DEFAULT_KEY_BG        // 0xFF2B2B2B — Floris Night key
    }
    fun specialKeyColor() = when (themeMode) {
        THEME_DAY    -> 0xFFDEDEDE.toInt()
        THEME_AMOLED -> 0xFF1A1A1A.toInt()
        else         -> DEFAULT_KEY_SPEC_BG   // 0xFF383838 — shift/del/num
    }
    fun spaceBarColor() = when (themeMode) {
        THEME_DAY    -> 0xFFD0D0D0.toInt()
        THEME_AMOLED -> 0xFF1C1C1C.toInt()
        else         -> DEFAULT_SPACE_BG      // 0xFF3A3A3A — space bar
    }
    fun surfaceColor() = when (themeMode) {
        THEME_DAY    -> 0xFFF5F5F5.toInt()
        THEME_AMOLED -> 0xFF000000.toInt()
        else         -> 0xFF242424.toInt()
    }
    fun textColor() = when (themeMode) {
        THEME_DAY -> 0xFF212121.toInt()
        else      -> DEFAULT_TEXT             // 0xFFEEEEEE — Floris Night
    }
    fun hintTextColor() = when (themeMode) {
        THEME_DAY -> 0xFF757575.toInt()
        else      -> DEFAULT_HINT
    }
    fun shadowColor() = DEFAULT_SHADOW
    fun accentTextColor() = 0xFFFFFFFF.toInt()
    fun keyColorAlpha(): Int        = (keyColor()        and 0x00FFFFFF) or (keyAlpha shl 24)
    fun specialKeyColorAlpha(): Int = (specialKeyColor() and 0x00FFFFFF) or (keyAlpha shl 24)

    // ── Export / Import / Reset ────────────────────────────────────────────
    fun exportToJson(): String {
        val j = JSONObject()
        j.put("theme_mode",      themeMode)
        j.put("accent_color",    accentColor)
        j.put("key_radius",      keyRadius)
        j.put("key_height",      keyHeightDp)
        j.put("key_padding",     keyPaddingDp)
        j.put("key_alpha",       keyAlpha)
        j.put("bg_image_uri",    bgImageUri)
        j.put("bg_opacity",      bgImageOpacity)
        j.put("vibrate",         vibrateEnabled)
        j.put("vibrate_ms",      vibrateDurationMs)
        j.put("sound",           soundEnabled)
        j.put("number_row",      showNumberRow)
        j.put("top_hints",       showTopHints)
        j.put("popup",           popupEnabled)
        j.put("swipe",           swipeEnabled)
        j.put("swipe_delete",    swipeDeleteEnabled)
        j.put("enabled_langs",   enabledLanguages)
        j.put("primary_lang",    primaryLanguage)
        return j.toString(2)
    }

    fun importFromJson(json: String): Boolean {
        return try {
            val j = JSONObject(json)
            val e = prefs.edit()
            if (j.has("theme_mode"))    e.putString("theme_mode",    j.getString("theme_mode"))
            if (j.has("accent_color"))  e.putInt("accent_color",     j.getInt("accent_color"))
            if (j.has("key_radius"))    e.putFloat("key_radius",      j.getDouble("key_radius").toFloat())
            if (j.has("key_height"))    e.putInt("key_height",        j.getInt("key_height"))
            if (j.has("key_padding"))   e.putInt("key_padding",       j.getInt("key_padding"))
            if (j.has("key_alpha"))     e.putInt("key_alpha",         j.getInt("key_alpha"))
            if (j.has("bg_image_uri"))  e.putString("bg_image_uri",   j.getString("bg_image_uri"))
            if (j.has("bg_opacity"))    e.putInt("bg_image_opacity",  j.getInt("bg_opacity"))
            if (j.has("vibrate"))       e.putBoolean("vibrate",       j.getBoolean("vibrate"))
            if (j.has("vibrate_ms"))    e.putInt("vibrate_ms",        j.getInt("vibrate_ms"))
            if (j.has("sound"))         e.putBoolean("sound",         j.getBoolean("sound"))
            if (j.has("number_row"))    e.putBoolean("number_row",    j.getBoolean("number_row"))
            if (j.has("top_hints"))     e.putBoolean("top_hints",     j.getBoolean("top_hints"))
            if (j.has("popup"))         e.putBoolean("popup",         j.getBoolean("popup"))
            if (j.has("swipe"))         e.putBoolean("swipe",         j.getBoolean("swipe"))
            if (j.has("swipe_delete"))  e.putBoolean("swipe_delete",  j.getBoolean("swipe_delete"))
            if (j.has("enabled_langs")) e.putString("enabled_langs",  j.getString("enabled_langs"))
            if (j.has("primary_lang"))  e.putString("primary_lang",   j.getString("primary_lang"))
            e.apply()
            true
        } catch (_: Exception) { false }
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
