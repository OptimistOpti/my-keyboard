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

    private var currentLangIndex = 0
    private var isShifted = false
    private var isCapsLock = false
    private var lastShiftTime = 0L
    private var showStickers = false
    private var showNumbers = false

    // Swipe tracking
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchStartTime = 0L
    private var swipePath = mutableListOf<PointF>()
    private var isSwipeMode = false
    private var lastSwipeX = 0f

    // Popup
    private var popupKey: Key? = null

    // Paints
    private val keyPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val swipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val popupPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val keys        = mutableListOf<Key>()
    private val stickerKeys = mutableListOf<Key>()
    private var pressedKey: Key? = null

    enum class KeyType { CHAR, BACKSPACE, SHIFT, ENTER, SPACE, NUMBERS, STICKER, LANG }
    data class Key(var x: Float, var y: Float, var w: Float, var h: Float,
                   val label: String, val type: KeyType = KeyType.CHAR,
                   val altLabel: String = "")

    private val allLangs get() = prefs.enabledLanguages.split(",")

    // Number row
    private val numberRow = listOf("1","2","3","4","5","6","7","8","9","0")
    private val numberHints = listOf("!","@","#","\$","%","^","&","*","(",  ")")

    private val ruRows = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("LANG","?123","пробел","ENTER")
    )
    private val ruRowsUp = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","Ы","В","А","П","Р","О","Л","Д","Ж","Э"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("LANG","?123","ПРОБЕЛ","ENTER")
    )
    private val ukRows = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","і","в","а","п","р","о","л","д","ж","є"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("LANG","?123","пробел","ENTER")
    )
    private val ukRowsUp = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","І","В","А","П","Р","О","Л","Д","Ж","Є"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("LANG","?123","ПРОБЕЛ","ENTER")
    )
    private val enRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("SHIFT","z","x","c","v","b","n","m","BACK"),
        listOf("LANG","?123","space","ENTER")
    )
    private val enRowsUp = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("SHIFT","Z","X","C","V","B","N","M","BACK"),
        listOf("LANG","?123","SPACE","ENTER")
    )
    private val numRows = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","*","(",")","_","+"),
        listOf("=","/",":",";","'","\"",",",".","!","?"),
        listOf("ABC","пробел","↵")
    )
    private val stickers = listOf(
        "😀","😂","🥰","😎","🤔","😴","🤩","😭",
        "👍","👎","🙏","🤝","✌️","🤞","💪","👏",
        "❤️","🔥","⭐","🎉","💯","✅","❌","🚀",
        "😡","🥺","😏","🤪","🫡","🥳","😒","🫶",
        "🐶","🐱","🦊","🐻","🎵","🎮","🍕","☕"
    )

    fun setKeyboardService(s: MyKeyboardService) { service = s }

    fun reset() {
        val langs = allLangs
        val primary = prefs.primaryLanguage
        currentLangIndex = langs.indexOf(primary).coerceAtLeast(0)
        isShifted = false; isCapsLock = false
        showStickers = false; showNumbers = false
        rebuildKeys(); invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh); rebuildKeys()
    }

    private fun currentRows(): List<List<String>> {
        val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
        return when (lang) {
            "uk" -> if (isShifted || isCapsLock) ukRowsUp else ukRows
            "en" -> if (isShifted || isCapsLock) enRowsUp else enRows
            else -> if (isShifted || isCapsLock) ruRowsUp else ruRows
        }
    }

    private fun rebuildKeys() {
        keys.clear(); stickerKeys.clear()
        val pad = prefs.keyPaddingDp.toFloat()
        val baseRows = currentRows()

        // Optionally prepend number row
        val rows = if (prefs.showNumberRow && !showNumbers)
            listOf(numberRow) + baseRows
        else baseRows

        val totalRows = if (showNumbers) numRows.size else rows.size
        val rh = height.toFloat() / totalRows

        val displayRows = if (showNumbers) numRows else rows

        displayRows.forEachIndexed { ri, row ->
            val y = ri * rh + pad
            val h = rh - pad * 2
            val isNumRowLine = prefs.showNumberRow && !showNumbers && ri == 0

            when {
                ri == displayRows.size - 1 -> buildBottom(row, y, h, pad)
                ri == displayRows.size - 2 && !showNumbers -> buildShiftRow(row, y, h, pad)
                isNumRowLine -> buildNumberRow(row, y, h, pad)
                else -> buildNormal(row, y, h, pad)
            }
        }

        // Stickers
        val cols = 8; val sRows = stickers.size / cols
        val sw = width.toFloat() / cols; val sh = height.toFloat() / (sRows + 1)
        stickers.forEachIndexed { i, s ->
            stickerKeys.add(Key((i % cols) * sw + pad, (i / cols) * sh + pad,
                sw - pad * 2, sh - pad * 2, s))
        }
        stickerKeys.add(Key(pad, sRows * sh + pad, width - pad * 2, sh - pad * 2,
            "← Назад", KeyType.STICKER))
    }

    private fun buildNormal(row: List<String>, y: Float, h: Float, pad: Float) {
        val kw = (width.toFloat() - pad * (row.size + 1)) / row.size
        row.forEachIndexed { i, l ->
            keys.add(Key(pad + i * (kw + pad), y, kw, h, l))
        }
    }

    private fun buildNumberRow(row: List<String>, y: Float, h: Float, pad: Float) {
        val kw = (width.toFloat() - pad * (row.size + 1)) / row.size
        row.forEachIndexed { i, l ->
            val alt = numberHints.getOrElse(i) { "" }
            keys.add(Key(pad + i * (kw + pad), y, kw, h, l, altLabel = alt))
        }
    }

    private fun buildShiftRow(row: List<String>, y: Float, h: Float, pad: Float) {
        val sw = width * 0.125f; val cc = row.size - 2
        val cw = (width - sw * 2 - pad * (cc + 3)) / cc; var x = pad
        row.forEach { l ->
            when (l) {
                "SHIFT" -> { keys.add(Key(x, y, sw, h, "⇧", KeyType.SHIFT)); x += sw + pad }
                "BACK"  -> { keys.add(Key(x, y, sw, h, "⌫", KeyType.BACKSPACE)); x += sw + pad }
                else    -> { keys.add(Key(x, y, cw, h, l)); x += cw + pad }
            }
        }
    }

    private fun buildBottom(row: List<String>, y: Float, h: Float, pad: Float) {
        val spW = width * 0.44f
        val sideW = (width - spW - pad * (row.size + 1)) / (row.size - 1)
        var x = pad
        val hasEmoji = !showNumbers
        row.forEach { l ->
            when {
                l.contains("пробел", true) || l.contains("space", true) -> {
                    keys.add(Key(x, y, spW, h, "space", KeyType.SPACE)); x += spW + pad
                }
                l == "LANG"  -> { keys.add(Key(x, y, sideW, h, "🌐", KeyType.LANG)); x += sideW + pad }
                l == "?123"  -> { keys.add(Key(x, y, sideW, h, "?123", KeyType.NUMBERS)); x += sideW + pad }
                l == "ABC"   -> { keys.add(Key(x, y, sideW, h, "ABC", KeyType.NUMBERS)); x += sideW + pad }
                l == "ENTER" || l == "↵" -> {
                    keys.add(Key(x, y, sideW, h, "↵", KeyType.ENTER)); x += sideW + pad
                }
                else -> { keys.add(Key(x, y, sideW, h, l)); x += sideW + pad }
            }
        }
    }

    // ─── Draw ─────────────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        val r = prefs.keyRadius
        canvas.drawColor(prefs.bgColor())
        if (showStickers) { drawStickers(canvas); return }

        keys.forEach { k ->
            val pressed = k == pressedKey
            val rect = RectF(k.x, k.y, k.x + k.w, k.y + k.h)
            val isSpecial = k.type in listOf(KeyType.BACKSPACE, KeyType.SHIFT, KeyType.ENTER,
                KeyType.NUMBERS, KeyType.LANG, KeyType.STICKER)

            // Shadow
            if (!pressed) {
                shadowPaint.color = prefs.shadowColor()
                canvas.drawRoundRect(RectF(rect.left+1, rect.top+2, rect.right+1, rect.bottom+2),
                    r, r, shadowPaint)
            }

            // Key bg
            keyPaint.color = when {
                pressed -> blend(if (isSpecial) prefs.specialKeyColor() else prefs.keyColor(),
                    prefs.accentColor, 0.25f)
                k.type == KeyType.SHIFT && (isShifted || isCapsLock) -> prefs.accentColor
                k.type == KeyType.ENTER -> prefs.accentColor
                isSpecial -> prefs.specialKeyColor()
                else -> prefs.keyColor()
            }
            canvas.drawRoundRect(rect, r, r, keyPaint)

            // Label
            val label = when (k.type) {
                KeyType.SPACE -> {
                    val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
                    when (lang) { "en" -> "space"; "uk" -> "пробіл"; else -> "пробел" }
                }
                else -> k.label
            }
            val accentKey = k.type == KeyType.ENTER ||
                            (k.type == KeyType.SHIFT && (isShifted || isCapsLock))
            textPaint.color = if (accentKey) prefs.accentTextColor() else prefs.textColor()
            textPaint.textSize = when (k.type) {
                KeyType.SPACE -> k.h * 0.28f
                KeyType.LANG  -> k.h * 0.50f
                KeyType.SHIFT, KeyType.BACKSPACE -> k.h * 0.46f
                else -> k.h * 0.42f
            }
            canvas.drawText(label, k.x + k.w / 2, k.y + k.h * 0.64f, textPaint)

            // Alt hint (top-right)
            if (k.altLabel.isNotEmpty() && prefs.showTopHints) {
                hintPaint.color = prefs.hintTextColor()
                hintPaint.textSize = k.h * 0.22f
                canvas.drawText(k.altLabel, k.x + k.w * 0.80f, k.y + k.h * 0.28f, hintPaint)
            }
        }

        // Swipe trail
        if (isSwipeMode && swipePath.size > 1 && prefs.swipeEnabled) {
            swipePaint.color = (prefs.accentColor and 0x00FFFFFF) or (0x99 shl 24)
            val path = Path()
            path.moveTo(swipePath[0].x, swipePath[0].y)
            for (i in 1 until swipePath.size) path.lineTo(swipePath[i].x, swipePath[i].y)
            canvas.drawPath(path, swipePaint)
        }

        // Popup bubble
        popupKey?.let { k ->
            if (prefs.popupEnabled) {
                val pw = k.w * 1.5f; val ph = k.h * 1.6f
                val px = (k.x + k.w / 2 - pw / 2).coerceIn(4f, width - pw - 4f)
                val py = (k.y - ph - 4f).coerceAtLeast(4f)
                popupPaint.color = prefs.accentColor
                canvas.drawRoundRect(RectF(px, py, px + pw, py + ph), r * 1.5f, r * 1.5f, popupPaint)
                popupTextPaint.color = 0xFFFFFFFF.toInt()
                popupTextPaint.textSize = ph * 0.55f
                canvas.drawText(k.label, px + pw / 2, py + ph * 0.68f, popupTextPaint)
            }
        }
    }

    private fun drawStickers(canvas: Canvas) {
        val r = prefs.keyRadius
        stickerKeys.forEach { k ->
            keyPaint.color = if (k.type == KeyType.STICKER) prefs.accentColor else prefs.keyColor()
            canvas.drawRoundRect(RectF(k.x, k.y, k.x + k.w, k.y + k.h), r, r, keyPaint)
            textPaint.color = if (k.type == KeyType.STICKER) 0xFFFFFFFF.toInt() else prefs.textColor()
            textPaint.textSize = if (k.type == KeyType.STICKER) k.h * 0.32f else k.h * 0.52f
            canvas.drawText(k.label, k.x + k.w / 2, k.y + k.h * 0.65f, textPaint)
        }
    }

    // ─── Touch ────────────────────────────────────────────────────────────────
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = x; touchStartY = y; touchStartTime = System.currentTimeMillis()
                swipePath.clear(); swipePath.add(PointF(x, y))
                isSwipeMode = false; lastSwipeX = x
                pressedKey = findKey(x, y, if (showStickers) stickerKeys else keys)
                popupKey = if (pressedKey?.type == KeyType.CHAR && !showStickers) pressedKey else null
                vibrate()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = abs(x - touchStartX); val dy = abs(y - touchStartY)

                // Swipe-delete on backspace: drag left on space bar
                if (prefs.swipeDeleteEnabled && pressedKey?.type == KeyType.BACKSPACE && dx > 60f) {
                    service?.deleteWord(); pressedKey = null; invalidate(); return true
                }

                if (dx > 20f && prefs.swipeEnabled && !showStickers) {
                    isSwipeMode = true; popupKey = null
                    swipePath.add(PointF(x, y))
                    pressedKey = findKey(x, y, keys)
                    invalidate()
                }
                lastSwipeX = x
            }
            MotionEvent.ACTION_UP -> {
                popupKey = null
                if (showStickers) {
                    findKey(x, y, stickerKeys)?.let { handleSticker(it) }
                } else if (isSwipeMode && prefs.swipeEnabled) {
                    handleSwipe()
                } else {
                    (pressedKey ?: findKey(x, y, keys))?.let { handleKey(it) }
                }
                pressedKey = null; isSwipeMode = false; swipePath.clear(); invalidate()
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float, list: List<Key>) =
        list.firstOrNull { x >= it.x && x <= it.x + it.w && y >= it.y && y <= it.y + it.h }

    private fun handleKey(k: Key) {
        when (k.type) {
            KeyType.CHAR -> {
                service?.commitText(k.label)
                if (isShifted && !isCapsLock) { isShifted = false; rebuildKeys(); invalidate() }
            }
            KeyType.BACKSPACE -> service?.deleteChar()
            KeyType.SPACE     -> service?.commitText(" ")
            KeyType.ENTER     -> service?.commitText("\n")
            KeyType.STICKER   -> { showStickers = true; invalidate() }
            KeyType.NUMBERS   -> { showNumbers = !showNumbers; rebuildKeys(); invalidate() }
            KeyType.LANG      -> {
                val langs = allLangs
                currentLangIndex = (currentLangIndex + 1) % langs.size
                prefs.primaryLanguage = langs[currentLangIndex]
                rebuildKeys(); invalidate()
            }
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                if (now - lastShiftTime < 400) { isCapsLock = !isCapsLock; isShifted = isCapsLock }
                else { isShifted = !isShifted; isCapsLock = false }
                lastShiftTime = now; rebuildKeys(); invalidate()
            }
        }
    }

    private fun handleSticker(k: Key) {
        if (k.type == KeyType.STICKER) { showStickers = false; invalidate() }
        else service?.commitEmoji(k.label)
    }

    private fun handleSwipe() {
        if (swipePath.size < 3) return
        val word = StringBuilder()
        swipePath.forEach { pt ->
            findKey(pt.x, pt.y, keys)?.let {
                if (it.type == KeyType.CHAR && it.label.length == 1 &&
                    (word.isEmpty() || word.last().toString() != it.label))
                    word.append(it.label)
            }
        }
        if (word.length > 1) service?.commitText(word.toString() + " ")
    }

    private fun vibrate() {
        if (!prefs.vibrateEnabled) return
        try {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createOneShot(
                prefs.vibrateDurationMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {}
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        val dx = x2 - x1; val dy = y2 - y1; return sqrt(dx * dx + dy * dy)
    }

    private fun blend(c1: Int, c2: Int, r: Float): Int {
        val ir = 1f - r
        return Color.rgb(
            (Color.red(c1) * ir + Color.red(c2) * r).toInt(),
            (Color.green(c1) * ir + Color.green(c2) * r).toInt(),
            (Color.blue(c1) * ir + Color.blue(c2) * r).toInt()
        )
    }
}
