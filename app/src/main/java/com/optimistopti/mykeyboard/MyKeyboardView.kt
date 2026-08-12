package com.optimistopti.mykeyboard

import android.content.Context
import android.graphics.*
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sqrt

class MyKeyboardView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var service: MyKeyboardService? = null
    val prefs = KeyboardPrefs(context)

    // ── State ─────────────────────────────────────────────────────────────────
    private var currentLangIndex = 0
    private var isShifted = false
    private var isCapsLock = false
    private var lastShiftTime = 0L
    private var keyboardMode = Mode.ALPHA   // ALPHA | NUMERIC | SYMBOL | STICKER | CLIPBOARD

    enum class Mode { ALPHA, NUMERIC, SYMBOL, STICKER, CLIPBOARD }

    // ── Touch ─────────────────────────────────────────────────────────────────
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var swipePath = mutableListOf<PointF>()
    private var isSwipeMode = false
    private var swipeOnSpace = false
    private var swipeOnBackspace = false
    private var pressedKey: Key? = null

    // Long press repeat
    private var longPressRunnable: Runnable? = null
    private val LONG_PRESS_DELAY = 400L
    private val REPEAT_DELAY     = 50L
    private var isRepeating = false

    // Clipboard
    private val clipItems = mutableListOf<String>()

    // ── Icon cache ────────────────────────────────────────────────────────────
    private var iconBackspace: Bitmap? = null
    private var iconBackspaceActive: Bitmap? = null
    private var iconEnter: Bitmap? = null
    private var iconEnterActive: Bitmap? = null
    private var iconShift: Bitmap? = null
    private var iconShiftActive: Bitmap? = null
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    internal var iconThemeKey = ""

    // ── Paints ────────────────────────────────────────────────────────────────
    private val keyPaint     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val hintPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface  = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val popupPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val popupTxtPaint= Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface  = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        color     = 0xFFFFFFFF.toInt()
    }
    private val bgImagePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ── Keys ──────────────────────────────────────────────────────────────────
    private val keys     = mutableListOf<Key>()
    private var popupKey: Key? = null

    enum class KeyType {
        CHAR, BACKSPACE, SHIFT, ENTER, SPACE,
        NUMBERS, SYMBOL_PAGE, BACK_ALPHA,
        COMMA, PERIOD, STICKER, LANG,
        CLIP_ITEM, CLIP_CLOSE
    }

    data class Key(
        var x: Float, var y: Float, var w: Float, var h: Float,
        val label: String,
        val type: KeyType = KeyType.CHAR,
        val hint: String  = ""
    )

    // ── Layouts ───────────────────────────────────────────────────────────────
    private val allLangs get() = prefs.enabledLanguages.split(",")

    // Russian
    private val ruAlpha = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val ruAlphaCaps = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","Ы","В","А","П","Р","О","Л","Д","Ж","Э"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    // Ukrainian
    private val ukAlpha = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","і","в","а","п","р","о","л","д","ж","є"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val ukAlphaCaps = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","І","В","А","П","Р","О","Л","Д","Ж","Є"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    // English
    private val enAlpha = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("SHIFT","z","x","c","v","b","n","m","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val enAlphaCaps = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("SHIFT","Z","X","C","V","B","N","M","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )

    // Number page 1 — like GBoard/FlorisBoard
    // Row hints: what appears on long-press
    private val numPage1 = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","-","+","(",")","/"),
        listOf("=\\<","*","\"","'",":",";","!","?","BACK"),
        listOf("ABC","COMMA","SPACE","PERIOD","ENTER")
    )
    // Hints for num page 1 row 0
    private val numHints1 = listOf("~","`","|","•","√","π","÷","×","¶","∆")

    // Symbol page 2
    private val numPage2 = listOf(
        listOf("~","`","|","•","√","π","÷","×","¶","∆"),
        listOf("£","¢","€","¥","^","°","=","{","}","\\"),
        listOf("?123","<",">","[","]","_","—","…",",","BACK"),
        listOf("ABC","COMMA","SPACE","PERIOD","ENTER")
    )

    // Stickers
    private val stickers = listOf(
        "😀","😂","🥰","😎","🤔","😴","🤩","😭",
        "👍","👎","🙏","🤝","✌️","🤞","💪","👏",
        "❤️","🔥","⭐","🎉","💯","✅","❌","🚀",
        "😡","🥺","😏","🤪","🫡","🥳","😒","🫶",
        "🐶","🐱","🦊","🐻","🎵","🎮","🍕","☕"
    )

    // ── Public API ────────────────────────────────────────────────────────────
    fun setKeyboardService(s: MyKeyboardService) { service = s }

    fun reset() {
        currentLangIndex = allLangs.indexOf(prefs.primaryLanguage).coerceAtLeast(0)
        isShifted = false; isCapsLock = false
        keyboardMode = Mode.ALPHA
        iconThemeKey = ""
        stopRepeat()
        rebuildKeys(); invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh); rebuildKeys()
    }

    // ── Key building ──────────────────────────────────────────────────────────
    private fun rebuildKeys() {
        keys.clear()
        when (keyboardMode) {
            Mode.ALPHA     -> buildAlpha()
            Mode.NUMERIC   -> buildGrid(numPage1, addNumHints = true)
            Mode.SYMBOL    -> buildGrid(numPage2, addNumHints = false)
            Mode.STICKER   -> buildStickers()
            Mode.CLIPBOARD -> buildClipboard()
        }
    }

    private fun buildAlpha() {
        val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
        val rows = when (lang) {
            "uk" -> if (isShifted || isCapsLock) ukAlphaCaps else ukAlpha
            "en" -> if (isShifted || isCapsLock) enAlphaCaps else enAlpha
            else -> if (isShifted || isCapsLock) ruAlphaCaps else ruAlpha
        }
        val d = prefs.keyPaddingDp.toFloat()
        val rh = height.toFloat() / rows.size

        rows.forEachIndexed { ri, row ->
            val y = ri * rh + d; val h = rh - d * 2
            when (ri) {
                rows.size - 1 -> buildBottomRow(row, y, h, d)
                rows.size - 2 -> buildShiftRow(row, y, h, d)
                else          -> buildNormalRow(row, y, h, d)
            }
        }
    }

    private fun buildNormalRow(row: List<String>, y: Float, h: Float, d: Float) {
        val kw = (width.toFloat() - d * (row.size + 1)) / row.size
        row.forEachIndexed { i, l ->
            keys.add(Key(d + i * (kw + d), y, kw, h, l))
        }
    }

    private fun buildShiftRow(row: List<String>, y: Float, h: Float, d: Float) {
        val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
        val isEn = lang == "en"
        // English has 9 chars, RU/UK has 10
        val charCount = row.size - 2
        val sideW = width * (if (isEn) 0.135f else 0.118f)
        val kw = (width - sideW * 2 - d * (charCount + 3)) / charCount
        var x = d
        row.forEach { l ->
            when (l) {
                "SHIFT" -> { keys.add(Key(x, y, sideW, h, "⇧", KeyType.SHIFT)); x += sideW + d }
                "BACK"  -> { keys.add(Key(x, y, sideW, h, "⌫", KeyType.BACKSPACE)); x += sideW + d }
                else    -> { keys.add(Key(x, y, kw, h, l)); x += kw + d }
            }
        }
    }

    private fun buildBottomRow(row: List<String>, y: Float, h: Float, d: Float) {
        // ?123 | , | ‹ LANG › | . | ↵
        val numW    = width * 0.110f
        val smallW  = width * 0.085f
        val enterW  = width * 0.125f
        val spaceW  = width - numW - smallW * 2 - enterW - d * 5
        var x = d
        row.forEach { l ->
            when (l) {
                "?123"   -> { keys.add(Key(x, y, numW,   h, l, KeyType.NUMBERS));  x += numW   + d }
                "COMMA"  -> { keys.add(Key(x, y, smallW, h, ",", KeyType.COMMA));  x += smallW + d }
                "SPACE"  -> { keys.add(Key(x, y, spaceW, h, "space", KeyType.SPACE)); x += spaceW + d }
                "PERIOD" -> { keys.add(Key(x, y, smallW, h, ".", KeyType.PERIOD)); x += smallW + d }
                "ENTER"  -> { keys.add(Key(x, y, enterW, h, "↵", KeyType.ENTER));  x += enterW + d }
                else     -> { keys.add(Key(x, y, numW,   h, l, KeyType.NUMBERS));  x += numW   + d }
            }
        }
    }

    private fun buildGrid(rows: List<List<String>>, addNumHints: Boolean) {
        val d = prefs.keyPaddingDp.toFloat()
        val rh = height.toFloat() / rows.size
        rows.forEachIndexed { ri, row ->
            val y = ri * rh + d; val h = rh - d * 2
            when {
                ri == rows.size - 1 -> buildNumBottomRow(row, y, h, d)
                ri == rows.size - 2 -> buildNumShiftRow(row, y, h, d)
                else -> {
                    val kw = (width.toFloat() - d * (row.size + 1)) / row.size
                    row.forEachIndexed { i, l ->
                        val hint = if (addNumHints && ri == 0) numHints1.getOrElse(i) { "" } else ""
                        keys.add(Key(d + i * (kw + d), y, kw, h, l, hint = hint))
                    }
                }
            }
        }
    }

    private fun buildNumShiftRow(row: List<String>, y: Float, h: Float, d: Float) {
        val sideW = width * 0.118f
        val charCount = row.size - 2
        val kw = (width - sideW * 2 - d * (charCount + 3)) / charCount
        var x = d
        row.forEach { l ->
            when (l) {
                "=\\<"  -> { keys.add(Key(x, y, sideW, h, "=\\<", KeyType.SYMBOL_PAGE)); x += sideW + d }
                "?123"  -> { keys.add(Key(x, y, sideW, h, "?123", KeyType.SYMBOL_PAGE)); x += sideW + d }
                "BACK"  -> { keys.add(Key(x, y, sideW, h, "⌫", KeyType.BACKSPACE)); x += sideW + d }
                else    -> { keys.add(Key(x, y, kw, h, l)); x += kw + d }
            }
        }
    }

    private fun buildNumBottomRow(row: List<String>, y: Float, h: Float, d: Float) {
        val numW   = width * 0.110f
        val smallW = width * 0.085f
        val enterW = width * 0.125f
        val spaceW = width - numW - smallW * 2 - enterW - d * 5
        var x = d
        row.forEach { l ->
            when (l) {
                "ABC"    -> { keys.add(Key(x, y, numW,   h, l, KeyType.BACK_ALPHA)); x += numW   + d }
                "COMMA"  -> { keys.add(Key(x, y, smallW, h, ",", KeyType.COMMA));   x += smallW + d }
                "SPACE"  -> { keys.add(Key(x, y, spaceW, h, "space", KeyType.SPACE)); x += spaceW + d }
                "PERIOD" -> { keys.add(Key(x, y, smallW, h, ".", KeyType.PERIOD));   x += smallW + d }
                "ENTER"  -> { keys.add(Key(x, y, enterW, h, "↵", KeyType.ENTER));    x += enterW + d }
                else     -> { keys.add(Key(x, y, numW,   h, l, KeyType.NUMBERS));    x += numW   + d }
            }
        }
    }

    private fun buildStickers() {
        val d = prefs.keyPaddingDp.toFloat()
        val cols = 8; val sRows = stickers.size / cols + 1
        val sw = width.toFloat() / cols; val sh = height.toFloat() / sRows
        stickers.forEachIndexed { i, s ->
            keys.add(Key((i % cols) * sw + d, (i / cols) * sh + d, sw - d * 2, sh - d * 2, s))
        }
        val closeY = (stickers.size / cols) * sh
        keys.add(Key(d, closeY + d, width - d * 2, sh - d * 2, "← Назад", KeyType.CLIP_CLOSE))
    }

    private fun buildClipboard() {
        clipItems.clear()
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = cm.primaryClip
            if (clip != null) for (i in 0 until minOf(clip.itemCount, 4)) {
                val t = clip.getItemAt(i).coerceToText(context).toString()
                if (t.isNotBlank()) clipItems.add(t)
            }
        } catch (_: Exception) {}

        val d = prefs.keyPaddingDp.toFloat()
        val closeH = height * 0.15f
        val itemH  = height * 0.20f
        keys.add(Key(d, d, width - d * 2, closeH - d * 2, "✕  Буфер обмена", KeyType.CLIP_CLOSE))
        if (clipItems.isEmpty()) {
            keys.add(Key(d, closeH + d, width - d * 2, itemH - d * 2, "Буфер пуст"))
        } else {
            clipItems.take(4).forEachIndexed { i, text ->
                keys.add(Key(d, closeH + i * itemH + d, width - d * 2, itemH - d * 2,
                    if (text.length > 50) text.take(50) + "…" else text, KeyType.CLIP_ITEM))
            }
        }
    }

    // ── Background image ──────────────────────────────────────────────────────
    private var bgBitmap: Bitmap? = null
    private var lastBgUri = ""

    private fun loadBg() {
        val uri = prefs.bgImageUri
        if (uri == lastBgUri) return
        lastBgUri = uri
        bgBitmap = if (uri.isEmpty()) null else try {
            val inp = context.contentResolver.openInputStream(android.net.Uri.parse(uri))
            val raw = android.graphics.BitmapFactory.decodeStream(inp); inp?.close()
            if (raw != null && width > 0 && height > 0)
                Bitmap.createScaledBitmap(raw, width, height, true)
            else raw
        } catch (_: Exception) { null }
    }

    // ── Icon loading ──────────────────────────────────────────────────────────
    private fun loadIcons() {
        val key = "${prefs.themeMode}|${prefs.accentColor}"
        if (key == iconThemeKey && iconBackspace != null) return
        iconThemeKey = key
        val sz = (20f * context.resources.displayMetrics.density).toInt()
        iconBackspace      = renderIcon(R.drawable.ic_backspace,    sz, prefs.textColor())
        iconBackspaceActive= renderIcon(R.drawable.ic_backspace,    sz, prefs.accentTextColor())
        iconEnter          = renderIcon(R.drawable.ic_enter,        sz, prefs.textColor())
        iconEnterActive    = renderIcon(R.drawable.ic_enter,        sz, prefs.accentTextColor())
        iconShift          = renderIcon(R.drawable.ic_shift,        sz, prefs.textColor())
        iconShiftActive    = renderIcon(R.drawable.ic_shift_locked, sz, prefs.accentTextColor())
    }

    private fun renderIcon(resId: Int, sz: Int, tint: Int): Bitmap {
        val bm  = Bitmap.createBitmap(sz, sz, Bitmap.Config.ARGB_8888)
        val cvs = Canvas(bm)
        val d   = androidx.core.content.ContextCompat.getDrawable(context, resId) ?: return bm
        val w   = androidx.core.graphics.drawable.DrawableCompat.wrap(d).mutate()
        androidx.core.graphics.drawable.DrawableCompat.setTint(w, tint)
        w.setBounds(0, 0, sz, sz); w.draw(cvs)
        return bm
    }

    // ── Draw ──────────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        loadBg(); loadIcons()
        val r = prefs.keyRadius

        // Background
        canvas.drawColor(prefs.bgColor())
        bgBitmap?.let {
            bgImagePaint.alpha = (prefs.bgImageOpacity * 255 / 100).coerceIn(0, 255)
            canvas.drawBitmap(it, 0f, 0f, bgImagePaint)
        }

        // Keys
        keys.forEach { k -> drawKey(canvas, k, r) }
    }

    private fun drawKey(canvas: Canvas, k: Key, r: Float) {
        val pressed   = k == pressedKey
        val isSpecial = k.type in listOf(KeyType.BACKSPACE, KeyType.SHIFT, KeyType.ENTER,
            KeyType.NUMBERS, KeyType.SYMBOL_PAGE, KeyType.BACK_ALPHA, KeyType.STICKER,
            KeyType.CLIP_CLOSE)
        val isSmall   = k.type in listOf(KeyType.COMMA, KeyType.PERIOD)
        val rect      = RectF(k.x, k.y, k.x + k.w, k.y + k.h)

        // FlorisBoard-style elevation: bottom shadow stripe
        if (!pressed) {
            shadowPaint.color = prefs.shadowColor()
            canvas.drawRoundRect(
                RectF(rect.left + 1f, rect.top + 3f, rect.right + 1f, rect.bottom + 4f),
                r, r, shadowPaint
            )
        }

        // Key bg
        val shiftActive = k.type == KeyType.SHIFT && (isShifted || isCapsLock)
        val bg = when {
            pressed && k.type in listOf(KeyType.BACKSPACE, KeyType.ENTER, KeyType.SHIFT) -> prefs.accentColor
            pressed -> blend(if (isSpecial || isSmall) prefs.specialKeyColor() else prefs.keyColor(), prefs.accentColor, 0.28f)
            shiftActive -> prefs.accentColor
            k.type == KeyType.SPACE -> prefs.spaceBarColor()
            isSpecial || isSmall   -> prefs.specialKeyColor()
            k.type == KeyType.CLIP_ITEM -> prefs.keyColor()
            k.type == KeyType.CLIP_CLOSE -> prefs.accentColor
            else -> prefs.keyColor()
        }
        val drawRect = if (pressed)
            RectF(rect.left + 0.5f, rect.top + 1f, rect.right - 0.5f, rect.bottom)
        else rect

        keyPaint.color = (bg and 0x00FFFFFF) or (prefs.keyAlpha shl 24)
        canvas.drawRoundRect(drawRect, r, r, keyPaint)

        // Text / Icon
        when (k.type) {
            KeyType.BACKSPACE -> {
                val bm = if (pressed) iconBackspaceActive else iconBackspace
                drawIcon(canvas, k, bm)
            }
            KeyType.ENTER -> {
                val bm = if (pressed) iconEnterActive else iconEnter
                drawIcon(canvas, k, bm)
            }
            KeyType.SHIFT -> {
                val bm = if (shiftActive || pressed) iconShiftActive else iconShift
                drawIcon(canvas, k, bm)
            }
            KeyType.SPACE -> {
                // Language name with arrows
                val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
                val name = when (lang) { "en" -> "English"; "uk" -> "Українська"; else -> "Русский" }
                textPaint.textSize = k.h * 0.28f
                textPaint.color    = prefs.hintTextColor()
                canvas.drawText("‹ $name ›", k.x + k.w / 2f, k.y + k.h * 0.64f, textPaint)
            }
            KeyType.CLIP_CLOSE, KeyType.CLIP_ITEM -> {
                textPaint.textSize = k.h * 0.28f
                textPaint.color    = if (k.type == KeyType.CLIP_CLOSE) prefs.accentTextColor() else prefs.textColor()
                val maxW = k.w - 24f
                var txt = k.label
                while (txt.length > 1 && textPaint.measureText(txt) > maxW) txt = txt.dropLast(1)
                canvas.drawText(txt, k.x + k.w / 2f, k.y + k.h * 0.62f, textPaint)
            }
            else -> {
                // Label
                textPaint.textSize = when (k.type) {
                    KeyType.NUMBERS, KeyType.SYMBOL_PAGE, KeyType.BACK_ALPHA -> k.h * 0.31f
                    KeyType.COMMA, KeyType.PERIOD -> k.h * 0.44f
                    else -> k.h * 0.42f
                }
                val txtColor = when {
                    shiftActive || (pressed && k.type == KeyType.SHIFT) -> prefs.accentTextColor()
                    pressed && k.type in listOf(KeyType.BACKSPACE, KeyType.ENTER) -> prefs.accentTextColor()
                    else -> prefs.textColor()
                }
                textPaint.color = txtColor
                canvas.drawText(k.label, k.x + k.w / 2f, k.y + k.h * 0.64f, textPaint)

                // Hint (top-right corner, like FlorisBoard)
                if (k.hint.isNotEmpty() && prefs.showTopHints) {
                    hintPaint.textSize = k.h * 0.20f
                    hintPaint.color    = prefs.hintTextColor()
                    canvas.drawText(k.hint, k.x + k.w - 5f, k.y + k.h * 0.26f, hintPaint)
                }
            }
        }

        // Popup bubble (GBoard style: pill above key)
        if (k == popupKey && prefs.popupEnabled && k.type == KeyType.CHAR) {
            drawPopup(canvas, k, r)
        }
    }

    private fun drawIcon(canvas: Canvas, k: Key, bm: Bitmap?) {
        bm ?: return
        canvas.drawBitmap(bm,
            k.x + k.w / 2f - bm.width / 2f,
            k.y + k.h / 2f - bm.height / 2f,
            iconPaint)
    }

    private fun drawPopup(canvas: Canvas, k: Key, r: Float) {
        val pw   = k.w * 1.35f
        val ph   = k.h * 1.45f
        val px   = (k.x + k.w / 2f - pw / 2f).coerceIn(6f, width - pw - 6f)
        val py   = (k.y - ph - 8f).coerceAtLeast(4f)
        val pill = ph / 2f

        // Shadow for popup
        shadowPaint.color = prefs.shadowColor()
        canvas.drawRoundRect(RectF(px + 2f, py + 3f, px + pw + 2f, py + ph + 3f), pill, pill, shadowPaint)

        // Popup bg
        popupPaint.color = prefs.accentColor
        canvas.drawRoundRect(RectF(px, py, px + pw, py + ph), pill, pill, popupPaint)

        // Triangle pointer
        val tri = Path().apply {
            moveTo(px + pw / 2f - 7f, py + ph - 1f)
            lineTo(px + pw / 2f + 7f, py + ph - 1f)
            lineTo(px + pw / 2f,       py + ph + 9f)
            close()
        }
        canvas.drawPath(tri, popupPaint)

        // Letter
        popupTxtPaint.textSize = ph * 0.52f
        canvas.drawText(k.label, px + pw / 2f, py + ph * 0.65f, popupTxtPaint)
    }

    // ── Touch ─────────────────────────────────────────────────────────────────
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = x; touchStartY = y
                swipePath.clear(); swipePath.add(PointF(x, y))
                isSwipeMode = false

                pressedKey = findKey(x, y)
                val k = pressedKey
                swipeOnSpace     = k?.type == KeyType.SPACE
                swipeOnBackspace = k?.type == KeyType.BACKSPACE
                popupKey         = if (k?.type == KeyType.CHAR) k else null

                if (k?.type == KeyType.BACKSPACE) scheduleRepeat()
                vibrate(); invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - touchStartX; val adx = abs(dx)

                if (swipeOnSpace) { invalidate(); return true }

                if (swipeOnBackspace) {
                    if (adx > 60f && dx < 0) {
                        stopRepeat(); service?.deleteWord()
                        swipeOnBackspace = false; pressedKey = null; invalidate()
                    }
                    return true
                }

                if (adx > 22f && prefs.swipeEnabled) {
                    isSwipeMode = true; popupKey = null
                    swipePath.add(PointF(x, y))
                    pressedKey = findKey(x, y); invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopRepeat(); popupKey = null
                val dx = x - touchStartX

                when {
                    swipeOnSpace && abs(dx) > 35f -> switchLang(dx > 0)
                    swipeOnBackspace              -> deleteSelectedOrChar()
                    isSwipeMode && prefs.swipeEnabled -> handleSwipe()
                    else -> (pressedKey ?: findKey(x, y))?.let { handleKey(it) }
                }

                pressedKey = null; isSwipeMode = false
                swipeOnSpace = false; swipeOnBackspace = false
                swipePath.clear(); invalidate()
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float) =
        keys.firstOrNull { x >= it.x && x <= it.x + it.w && y >= it.y && y <= it.y + it.h }

    // ── Key actions ───────────────────────────────────────────────────────────
    private fun handleKey(k: Key) {
        when (k.type) {
            KeyType.CHAR -> {
                service?.commitText(k.label)
                if (isShifted && !isCapsLock) { isShifted = false; rebuildKeys(); invalidate() }
            }
            KeyType.COMMA  -> { service?.commitText(","); autoUnshift() }
            KeyType.PERIOD -> { service?.commitText("."); autoUnshift() }
            KeyType.BACKSPACE -> deleteSelectedOrChar()
            KeyType.SPACE     -> service?.commitText(" ")
            KeyType.ENTER     -> service?.performEnter()
            KeyType.STICKER   -> { keyboardMode = Mode.STICKER; rebuildKeys(); invalidate() }
            KeyType.NUMBERS   -> { keyboardMode = Mode.NUMERIC; rebuildKeys(); invalidate() }
            KeyType.SYMBOL_PAGE -> {
                keyboardMode = if (keyboardMode == Mode.NUMERIC) Mode.SYMBOL else Mode.NUMERIC
                rebuildKeys(); invalidate()
            }
            KeyType.BACK_ALPHA -> { keyboardMode = Mode.ALPHA; rebuildKeys(); invalidate() }
            KeyType.LANG       -> switchLang(true)
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                if (now - lastShiftTime < 400) { isCapsLock = !isCapsLock; isShifted = isCapsLock }
                else { isShifted = !isShifted; isCapsLock = false }
                lastShiftTime = now; rebuildKeys(); invalidate()
            }
            KeyType.CLIP_CLOSE -> {
                if (keyboardMode == Mode.STICKER || keyboardMode == Mode.CLIPBOARD) {
                    keyboardMode = Mode.ALPHA; rebuildKeys(); invalidate()
                }
            }
            KeyType.CLIP_ITEM -> {
                val idx = keys.filter { it.type == KeyType.CLIP_ITEM }.indexOf(k)
                if (idx >= 0 && idx < clipItems.size) service?.commitText(clipItems[idx])
                keyboardMode = Mode.ALPHA; rebuildKeys(); invalidate()
            }
            else -> {}
        }
    }

    private fun autoUnshift() {
        if (isShifted && !isCapsLock) { isShifted = false; rebuildKeys(); invalidate() }
    }

    private fun switchLang(forward: Boolean) {
        val langs = allLangs
        currentLangIndex = if (forward) (currentLangIndex + 1) % langs.size
                           else (currentLangIndex - 1 + langs.size) % langs.size
        prefs.primaryLanguage = langs[currentLangIndex]
        rebuildKeys(); invalidate(); vibrate()
    }

    private fun deleteSelectedOrChar() {
        val conn = service?.currentInputConnectionCompat
        if (conn != null) {
            val sel = conn.getSelectedText(0)
            if (!sel.isNullOrEmpty()) conn.commitText("", 1)
            else service?.deleteChar()
        } else service?.deleteChar()
    }

    private fun handleSwipe() {
        if (swipePath.size < 3) return
        val word = StringBuilder()
        swipePath.forEach { pt ->
            findKey(pt.x, pt.y)?.let {
                if (it.type == KeyType.CHAR && it.label.length == 1 &&
                    (word.isEmpty() || word.last().toString() != it.label)) word.append(it.label)
            }
        }
        if (word.length > 1) service?.commitText(word.toString() + " ")
    }

    // ── Long press repeat ─────────────────────────────────────────────────────
    private fun scheduleRepeat() {
        val repeat = object : Runnable {
            override fun run() {
                if (isRepeating) { deleteSelectedOrChar(); vibrate(); postDelayed(this, REPEAT_DELAY) }
            }
        }
        longPressRunnable = Runnable { isRepeating = true; deleteSelectedOrChar(); postDelayed(repeat, REPEAT_DELAY) }
        postDelayed(longPressRunnable!!, LONG_PRESS_DELAY)
    }

    private fun stopRepeat() {
        longPressRunnable?.let { removeCallbacks(it) }
        longPressRunnable = null; isRepeating = false
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private fun vibrate() {
        if (!prefs.vibrateEnabled) return
        try {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(prefs.vibrateDurationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    private fun blend(c1: Int, c2: Int, r: Float): Int {
        val ir = 1f - r
        return Color.rgb(
            (Color.red(c1)   * ir + Color.red(c2)   * r).toInt(),
            (Color.green(c1) * ir + Color.green(c2) * r).toInt(),
            (Color.blue(c1)  * ir + Color.blue(c2)  * r).toInt()
        )
    }
}
