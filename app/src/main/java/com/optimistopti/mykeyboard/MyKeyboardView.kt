package com.optimistopti.mykeyboard

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
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
    private var showClipboard = false
    private val clipItems = mutableListOf<String>()
    private var bgBitmap: android.graphics.Bitmap? = null

    private var bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lastBgUri: String = ""

    // Touch tracking
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var swipePath = mutableListOf<PointF>()
    private var isSwipeMode = false
    private var swipeOnSpace = false
    private var swipeStartedOnBackspace = false

    // Long press
    private var longPressRunnable: Runnable? = null
    private val longPressDelay = 400L
    private val repeatDelay = 50L
    private var isLongPressing = false

    // Popup
    private var popupKey: Key? = null

    // Paints
    private val keyPaint      = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val shadowPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint     = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val hintPaint     = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
    }
    private val swipePaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 7f
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }
    private val popupPaint     = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val keys        = mutableListOf<Key>()
    private val stickerKeys = mutableListOf<Key>()
    private val clipKeys    = mutableListOf<Key>()
    private var pressedKey: Key? = null

    enum class KeyType {
        CHAR, BACKSPACE, SHIFT, ENTER, SPACE, NUMBERS,
        STICKER, LANG, COMMA, PERIOD, CLIP_ITEM, CLIP_CLOSE,
        ARROW_LEFT, ARROW_RIGHT  // kept for potential future use
    }
    data class Key(
        var x: Float, var y: Float, var w: Float, var h: Float,
        val label: String, val type: KeyType = KeyType.CHAR,
        val altLabel: String = ""
    )

    private val allLangs get() = prefs.enabledLanguages.split(",")

    private val numberRow   = listOf("1","2","3","4","5","6","7","8","9","0")
    private val numberHints = listOf("!","@","#","\$","%","^","&","*","(",")")

    // Bottom row tokens: ?123 | , | SPACE | . | ENTER
    // (comma left of space, period right of space)
    private val ruRows = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val ruRowsUp = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","Ы","В","А","П","Р","О","Л","Д","Ж","Э"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val ukRows = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","і","в","а","п","р","о","л","д","ж","є"),
        listOf("SHIFT","я","ч","с","м","и","т","ь","б","ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val ukRowsUp = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","І","В","А","П","Р","О","Л","Д","Ж","Є"),
        listOf("SHIFT","Я","Ч","С","М","И","Т","Ь","Б","Ю","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val enRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("SHIFT","z","x","c","v","b","n","m","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val enRowsUp = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("SHIFT","Z","X","C","V","B","N","M","BACK"),
        listOf("?123","COMMA","SPACE","PERIOD","ENTER")
    )
    private val numRows = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","*","(",")","_","+"),
        listOf("=","/",":",";","'","\"",",",".","!","?"),
        listOf("ABC","SPACE","ENTER")
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
        currentLangIndex = langs.indexOf(prefs.primaryLanguage).coerceAtLeast(0)
        isShifted = false; isCapsLock = false
        showStickers = false; showNumbers = false; showClipboard = false
        stopLongPress()
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
        keys.clear(); stickerKeys.clear(); clipKeys.clear()
        val pad = prefs.keyPaddingDp.toFloat()
        val baseRows = currentRows()
        val rows = if (prefs.showNumberRow && !showNumbers) listOf(numberRow) + baseRows else baseRows
        val displayRows = if (showNumbers) numRows else rows
        val rh = height.toFloat() / displayRows.size

        displayRows.forEachIndexed { ri, row ->
            val y = ri * rh + pad; val h = rh - pad * 2
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
            stickerKeys.add(Key((i%cols)*sw+pad, (i/cols)*sh+pad, sw-pad*2, sh-pad*2, s))
        }
        stickerKeys.add(Key(pad, sRows*sh+pad, width-pad*2, sh-pad*2, "← Назад", KeyType.CLIP_CLOSE))

        // Clipboard
        buildClipboardKeys(pad)
    }

    private fun buildNormal(row: List<String>, y: Float, h: Float, pad: Float) {
        val kw = (width.toFloat() - pad*(row.size+1)) / row.size
        row.forEachIndexed { i, l -> keys.add(Key(pad + i*(kw+pad), y, kw, h, l)) }
    }

    private fun buildNumberRow(row: List<String>, y: Float, h: Float, pad: Float) {
        val kw = (width.toFloat() - pad*(row.size+1)) / row.size
        row.forEachIndexed { i, l ->
            keys.add(Key(pad + i*(kw+pad), y, kw, h, l, altLabel = numberHints.getOrElse(i){""}))
        }
    }

    private fun buildShiftRow(row: List<String>, y: Float, h: Float, pad: Float) {
        val sw = width * 0.125f; val cc = row.size - 2
        val cw = (width - sw*2 - pad*(cc+3)) / cc; var x = pad
        row.forEach { l ->
            when (l) {
                "SHIFT" -> { keys.add(Key(x,y,sw,h,"⇧",KeyType.SHIFT)); x+=sw+pad }
                "BACK"  -> { keys.add(Key(x,y,sw,h,"⌫",KeyType.BACKSPACE)); x+=sw+pad }
                else    -> { keys.add(Key(x,y,cw,h,l)); x+=cw+pad }
            }
        }
    }

    private fun buildBottom(row: List<String>, y: Float, h: Float, pad: Float) {
        // ?123 | , | < ЯЗЫК > | . | ↵
        val numW   = width * 0.115f
        val smallW = width * 0.085f
        val enterW = width * 0.130f
        val spaceW = width - numW - smallW*2 - enterW - pad*5

        var x = pad
        row.forEach { l ->
            when (l) {
                "?123","ABC" -> { keys.add(Key(x,y,numW,h,l,KeyType.NUMBERS)); x+=numW+pad }
                "COMMA"      -> { keys.add(Key(x,y,smallW,h,",",KeyType.COMMA)); x+=smallW+pad }
                "SPACE"      -> { keys.add(Key(x,y,spaceW,h,"space",KeyType.SPACE)); x+=spaceW+pad }
                "PERIOD"     -> { keys.add(Key(x,y,smallW,h,".",KeyType.PERIOD)); x+=smallW+pad }
                "ENTER","↵"  -> { keys.add(Key(x,y,enterW,h,"↵",KeyType.ENTER)); x+=enterW+pad }
                else         -> { keys.add(Key(x,y,numW,h,l,KeyType.NUMBERS)); x+=numW+pad }
            }
        }
    }

    private fun buildClipboardKeys(pad: Float) {
        val closeH = height * 0.14f
        val itemH  = height * 0.20f
        clipKeys.add(Key(pad, pad, width-pad*2, closeH-pad*2, "✕  Буфер обмена", KeyType.CLIP_CLOSE))
        if (clipItems.isEmpty()) {
            clipKeys.add(Key(pad, closeH+pad, width-pad*2, itemH-pad*2, "Буфер пуст"))
        } else {
            clipItems.take(4).forEachIndexed { i, text ->
                val y = closeH + i*itemH + pad
                clipKeys.add(Key(pad, y, width-pad*2, itemH-pad*2,
                    if (text.length > 45) text.take(45)+"…" else text, KeyType.CLIP_ITEM))
            }
        }
    }

    // ─── Draw ────────────────────────────────────────────────────────────────



    private fun loadBgIfNeeded() {
        val uri = prefs.bgImageUri
        if (uri == lastBgUri) return
        lastBgUri = uri
        bgBitmap = if (uri.isEmpty()) null else {
            try {
                val input = context.contentResolver.openInputStream(Uri.parse(uri))
                val raw = BitmapFactory.decodeStream(input)
                input?.close()
                // Scale to keyboard size
                if (raw != null && width > 0 && height > 0)
                    android.graphics.Bitmap.createScaledBitmap(raw, width, height, true)
                else raw
            } catch (_: Exception) { null }
        }
    }
    override fun onDraw(canvas: Canvas) {
        val r = prefs.keyRadius
        loadBgIfNeeded()
        canvas.drawColor(prefs.bgColor())
        // Draw background image if set
        bgBitmap?.let { bm ->
            bgPaint.alpha = (prefs.bgImageOpacity * 255 / 100).coerceIn(0, 255)
            canvas.drawBitmap(bm, 0f, 0f, bgPaint)
        }
        when {
            showClipboard -> { drawClipboard(canvas); return }
            showStickers  -> { drawStickers(canvas); return }
        }

        keys.forEach { k ->
            val pressed = k == pressedKey
            val rect = RectF(k.x, k.y, k.x+k.w, k.y+k.h)
            val isSpecial = k.type in listOf(
                KeyType.BACKSPACE, KeyType.SHIFT, KeyType.ENTER, KeyType.NUMBERS)
            val isSmall = k.type in listOf(
                KeyType.COMMA, KeyType.PERIOD, KeyType.ARROW_LEFT, KeyType.ARROW_RIGHT)

            if (!pressed) {
                shadowPaint.color = prefs.shadowColor()
                canvas.drawRoundRect(RectF(rect.left+1,rect.top+2,rect.right+1,rect.bottom+2), r, r, shadowPaint)
            }

            keyPaint.color = when {
                pressed -> blend(
                    if (isSpecial||isSmall) prefs.specialKeyColor() else prefs.keyColor(),
                    prefs.accentColor, 0.25f)
                k.type == KeyType.SHIFT && (isShifted||isCapsLock) -> prefs.accentColor
                k.type == KeyType.ENTER -> prefs.accentColor
                isSpecial || isSmall -> prefs.specialKeyColor()
                else -> prefs.keyColor()
            }
            canvas.drawRoundRect(rect, r, r, keyPaint)

            // Label
            val accentKey = k.type == KeyType.ENTER ||
                            (k.type == KeyType.SHIFT && (isShifted||isCapsLock))
            textPaint.color = if (accentKey) prefs.accentTextColor() else prefs.textColor()

            val label: String
            val fontSize: Float
            when (k.type) {
                KeyType.SPACE -> {
                    val lang = allLangs.getOrElse(currentLangIndex) { "ru" }
                    val langName = when (lang) { "en" -> "English"; "uk" -> "Українська"; else -> "Русский" }
                    label = "‹ $langName ›"
                    fontSize = k.h * 0.26f
                }
                KeyType.SHIFT -> {
                    label = ""  // drawn as bitmap below
                    fontSize = k.h * 0.46f
                }
                KeyType.ENTER -> {
                    label = ""  // drawn as bitmap below
                    fontSize = k.h * 0.44f
                }
                KeyType.ARROW_LEFT, KeyType.ARROW_RIGHT -> {
                    label = k.label; fontSize = k.h * 0.36f
                }
                KeyType.NUMBERS -> { label = k.label; fontSize = k.h * 0.32f }
                KeyType.COMMA, KeyType.PERIOD -> { label = k.label; fontSize = k.h * 0.46f }
                KeyType.BACKSPACE -> { label = ""; fontSize = k.h * 0.44f }
                else -> { label = k.label; fontSize = k.h * 0.42f }
            }
            textPaint.textSize = fontSize
            canvas.drawText(label, k.x+k.w/2, k.y+k.h*0.64f, textPaint)

            // Draw icons for special keys using KeyIcon helper
            when (k.type) {
                KeyType.BACKSPACE -> KeyIcon.draw(canvas, KeyIcon.BACKSPACE, k, prefs.textColor())
                KeyType.ENTER     -> KeyIcon.draw(canvas, KeyIcon.ENTER, k, prefs.accentTextColor())
                KeyType.SHIFT     -> {
                    val res = if (isCapsLock) KeyIcon.SHIFT_LOCKED else KeyIcon.SHIFT
                    val col = if (isShifted || isCapsLock) prefs.accentTextColor() else prefs.textColor()
                    KeyIcon.draw(canvas, res, k, col)
                }
                else -> {}
            }

            if (k.altLabel.isNotEmpty() && prefs.showTopHints) {
                hintPaint.color = prefs.hintTextColor()
                hintPaint.textSize = k.h * 0.22f
                canvas.drawText(k.altLabel, k.x+k.w*0.80f, k.y+k.h*0.28f, hintPaint)
            }
        }

        // Swipe trail
        if (isSwipeMode && swipePath.size > 1 && prefs.swipeEnabled && !swipeOnSpace && !swipeStartedOnBackspace) {
            swipePaint.color = (prefs.accentColor and 0x00FFFFFF) or (0x99 shl 24)
            val path = Path()
            path.moveTo(swipePath[0].x, swipePath[0].y)
            for (i in 1 until swipePath.size) path.lineTo(swipePath[i].x, swipePath[i].y)
            canvas.drawPath(path, swipePaint)
        }

        if (prefs.popupEnabled) {
            popupKey?.let { k ->
                val pw = k.w*1.5f; val ph = k.h*1.6f
                val px = (k.x+k.w/2-pw/2).coerceIn(4f, width-pw-4f)
                val py = (k.y-ph-4f).coerceAtLeast(4f)
                popupPaint.color = prefs.accentColor
                canvas.drawRoundRect(RectF(px,py,px+pw,py+ph), prefs.keyRadius*1.5f, prefs.keyRadius*1.5f, popupPaint)
                popupTextPaint.color = 0xFFFFFFFF.toInt()
                popupTextPaint.textSize = ph * 0.55f
                canvas.drawText(k.label, px+pw/2, py+ph*0.68f, popupTextPaint)
            }
        }
    }

    private fun drawStickers(canvas: Canvas) {
        val r = prefs.keyRadius
        stickerKeys.forEach { k ->
            val isClose = k.type == KeyType.CLIP_CLOSE
            keyPaint.color = if (isClose) prefs.accentColor else prefs.keyColor()
            canvas.drawRoundRect(RectF(k.x,k.y,k.x+k.w,k.y+k.h), r, r, keyPaint)
            textPaint.color = if (isClose) 0xFFFFFFFF.toInt() else prefs.textColor()
            textPaint.textSize = if (isClose) k.h*0.30f else k.h*0.52f
            canvas.drawText(k.label, k.x+k.w/2, k.y+k.h*0.65f, textPaint)
        }
    }

    private fun drawClipboard(canvas: Canvas) {
        val r = prefs.keyRadius
        clipKeys.forEach { k ->
            val rect = RectF(k.x, k.y, k.x+k.w, k.y+k.h)
            keyPaint.color = when (k.type) {
                KeyType.CLIP_CLOSE -> prefs.accentColor
                KeyType.CLIP_ITEM  -> prefs.keyColor()
                else               -> prefs.surfaceColor()
            }
            canvas.drawRoundRect(rect, r, r, keyPaint)
            textPaint.color = if (k.type == KeyType.CLIP_CLOSE) 0xFFFFFFFF.toInt() else prefs.textColor()
            textPaint.textSize = k.h * 0.28f
            val maxW = k.w - 24f
            var txt = k.label
            while (txt.isNotEmpty() && textPaint.measureText(txt) > maxW) txt = txt.dropLast(1)
            canvas.drawText(txt, k.x+k.w/2, k.y+k.h*0.60f, textPaint)
        }
    }

    // ─── Touch ───────────────────────────────────────────────────────────────
    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = x; touchStartY = y
                swipePath.clear(); swipePath.add(PointF(x,y))
                isSwipeMode = false; swipeOnSpace = false; swipeStartedOnBackspace = false
                isLongPressing = false

                val keyList = when {
                    showClipboard -> clipKeys
                    showStickers  -> stickerKeys
                    else          -> keys
                }
                pressedKey = findKey(x, y, keyList)
                val k = pressedKey

                swipeOnSpace = k?.type == KeyType.SPACE
                swipeStartedOnBackspace = k?.type == KeyType.BACKSPACE

                // Popup only for regular char keys
                popupKey = if (k?.type == KeyType.CHAR && !showStickers && !showClipboard) k else null

                // Long press on backspace → start repeating delete
                if (k?.type == KeyType.BACKSPACE) {
                    startLongPressDelete()
                }

                vibrate()
                invalidate()
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = x - touchStartX
                val adx = abs(dx)

                // Space swipe → language switch, don't allow swipe trail
                if (swipeOnSpace) {
                    invalidate(); return true
                }

                // Backspace swipe-delete: drag left on backspace key
                if (swipeStartedOnBackspace) {
                    if (adx > 60f && dx < 0) {
                        stopLongPress()
                        service?.deleteWord()
                        swipeStartedOnBackspace = false
                        pressedKey = null
                        invalidate()
                    }
                    return true
                }

                // Regular swipe typing
                if (adx > 20f && prefs.swipeEnabled && !showStickers && !showClipboard) {
                    isSwipeMode = true; popupKey = null
                    swipePath.add(PointF(x, y))
                    pressedKey = findKey(x, y, keys)
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopLongPress()
                popupKey = null
                val dx = x - touchStartX

                when {
                    showClipboard -> findKey(x,y,clipKeys)?.let { handleClipKey(it) }
                    showStickers  -> findKey(x,y,stickerKeys)?.let { handleSticker(it) }
                    swipeOnSpace && abs(dx) > 35f -> switchLanguage(dx > 0)
                    swipeStartedOnBackspace -> {
                        // short tap on backspace — delete selected or one char
                        deleteSelectedOrChar()
                    }
                    isSwipeMode && prefs.swipeEnabled -> handleSwipe()
                    else -> (pressedKey ?: findKey(x,y,keys))?.let { handleKey(it) }
                }

                pressedKey = null; isSwipeMode = false; swipeOnSpace = false
                swipeStartedOnBackspace = false; swipePath.clear(); invalidate()
            }
        }
        return true
    }

    // ─── Long press delete ────────────────────────────────────────────────────
    private fun startLongPressDelete() {
        val repeatRunnable = object : Runnable {
            override fun run() {
                if (isLongPressing) {
                    deleteSelectedOrChar()
                    vibrate()
                    postDelayed(this, repeatDelay)
                }
            }
        }
        longPressRunnable = Runnable {
            isLongPressing = true
            deleteSelectedOrChar()
            postDelayed(repeatRunnable, repeatDelay)
        }
        postDelayed(longPressRunnable!!, longPressDelay)
    }

    private fun stopLongPress() {
        longPressRunnable?.let { removeCallbacks(it) }
        longPressRunnable = null
        isLongPressing = false
    }

    // Deletes selected text if any, otherwise one character
    private fun deleteSelectedOrChar() {
        val conn = service?.currentInputConnectionCompat
        if (conn != null) {
            val selected = conn.getSelectedText(0)
            if (!selected.isNullOrEmpty()) {
                conn.commitText("", 1)
            } else {
                service?.deleteChar()
            }
        } else {
            service?.deleteChar()
        }
    }

    // ─── Actions ─────────────────────────────────────────────────────────────
    private fun switchLanguage(forward: Boolean) {
        val langs = allLangs
        currentLangIndex = if (forward) (currentLangIndex+1) % langs.size
                           else (currentLangIndex-1+langs.size) % langs.size
        prefs.primaryLanguage = langs[currentLangIndex]
        rebuildKeys(); invalidate(); vibrate()
    }

    private fun findKey(x: Float, y: Float, list: List<Key>) =
        list.firstOrNull { x >= it.x && x <= it.x+it.w && y >= it.y && y <= it.y+it.h }

    private fun handleKey(k: Key) {
        when (k.type) {
            KeyType.CHAR -> {
                service?.commitText(k.label)
                if (isShifted && !isCapsLock) { isShifted=false; rebuildKeys(); invalidate() }
            }
            KeyType.COMMA  -> {
                service?.commitText(",")
                if (isShifted && !isCapsLock) { isShifted=false; rebuildKeys(); invalidate() }
            }
            KeyType.PERIOD -> {
                service?.commitText(".")
                if (isShifted && !isCapsLock) { isShifted=false; rebuildKeys(); invalidate() }
            }
            KeyType.BACKSPACE  -> deleteSelectedOrChar()
            KeyType.SPACE      -> service?.commitText(" ")
            KeyType.ENTER      -> service?.performEnter()
            KeyType.STICKER    -> { showStickers=true; invalidate() }
            KeyType.NUMBERS    -> { showNumbers=!showNumbers; rebuildKeys(); invalidate() }
            KeyType.LANG       -> switchLanguage(true)
            KeyType.ARROW_LEFT -> {
                val conn = service?.currentInputConnectionCompat
                conn?.sendKeyEvent(android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
                conn?.sendKeyEvent(android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_LEFT))
            }
            KeyType.ARROW_RIGHT -> {
                val conn = service?.currentInputConnectionCompat
                conn?.sendKeyEvent(android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
                conn?.sendKeyEvent(android.view.KeyEvent(
                    android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_DPAD_RIGHT))
            }
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                if (now-lastShiftTime < 400) { isCapsLock=!isCapsLock; isShifted=isCapsLock }
                else { isShifted=!isShifted; isCapsLock=false }
                lastShiftTime=now; rebuildKeys(); invalidate()
            }
            else -> {}
        }
    }

    private fun handleSticker(k: Key) {
        if (k.type == KeyType.CLIP_CLOSE) { showStickers=false; invalidate() }
        else service?.commitEmoji(k.label)
    }

    private fun handleClipKey(k: Key) {
        when (k.type) {
            KeyType.CLIP_CLOSE -> { showClipboard=false; invalidate() }
            KeyType.CLIP_ITEM -> {
                val idx = clipKeys.filter { it.type==KeyType.CLIP_ITEM }.indexOf(k)
                if (idx>=0 && idx<clipItems.size) service?.commitText(clipItems[idx])
                showClipboard=false; invalidate()
            }
            else -> {}
        }
    }

    fun openClipboard() {
        clipItems.clear()
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = cm.primaryClip
            if (clip != null) {
                for (i in 0 until minOf(clip.itemCount, 4)) {
                    val t = clip.getItemAt(i).coerceToText(context).toString()
                    if (t.isNotBlank()) clipItems.add(t)
                }
            }
        } catch (_: Exception) {}
        showClipboard=true; rebuildKeys(); invalidate()
    }

    private fun handleSwipe() {
        if (swipePath.size < 3) return
        val word = StringBuilder()
        swipePath.forEach { pt ->
            findKey(pt.x, pt.y, keys)?.let {
                if (it.type==KeyType.CHAR && it.label.length==1 &&
                    (word.isEmpty() || word.last().toString()!=it.label)) word.append(it.label)
            }
        }
        if (word.length > 1) service?.commitText(word.toString()+" ")
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

    private fun blend(c1: Int, c2: Int, r: Float): Int {
        val ir = 1f-r
        return Color.rgb(
            (Color.red(c1)*ir+Color.red(c2)*r).toInt(),
            (Color.green(c1)*ir+Color.green(c2)*r).toInt(),
            (Color.blue(c1)*ir+Color.blue(c2)*r).toInt())
    }
