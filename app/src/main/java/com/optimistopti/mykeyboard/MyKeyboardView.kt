package com.optimistopti.mykeyboard

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.sqrt

class MyKeyboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var service: MyKeyboardService? = null
    private var currentLayout = Layout.RUSSIAN
    private var isShifted = false
    private var isCapsLock = false
    private var lastShiftTime = 0L
    private var showStickers = false

    // Swipe
    private var touchStartX = 0f; private var touchStartY = 0f
    private var swipePath = mutableListOf<PointF>()
    private var isSwipeMode = false

    // Colors
    private val bgColor = Color.parseColor("#1A1A2E")
    private val keyColor = Color.parseColor("#16213E")
    private val keyPressedColor = Color.parseColor("#0F3460")
    private val specialKeyColor = Color.parseColor("#0F3460")
    private val accentColor = Color.parseColor("#E94560")
    private val textColor = Color.WHITE

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER }
    private val swipePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80E94560"); style = Paint.Style.STROKE; strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    private val keys = mutableListOf<Key>()
    private val stickerKeys = mutableListOf<Key>()
    private var pressedKey: Key? = null

    enum class Layout { RUSSIAN, ENGLISH, NUMBERS }
    enum class KeyType { CHAR, BACKSPACE, SHIFT, ENTER, SPACE, LAYOUT_SWITCH, STICKER }
    data class Key(var x: Float, var y: Float, var w: Float, var h: Float, val label: String, val type: KeyType = KeyType.CHAR)

    private val ruRows = listOf(
        listOf("й","ц","у","к","е","н","г","ш","щ","з","х"),
        listOf("ф","ы","в","а","п","р","о","л","д","ж","э"),
        listOf("⇧","я","ч","с","м","и","т","ь","б","ю","⌫"),
        listOf("☺","123","пробел","↵")
    )
    private val ruRowsUp = listOf(
        listOf("Й","Ц","У","К","Е","Н","Г","Ш","Щ","З","Х"),
        listOf("Ф","Ы","В","А","П","Р","О","Л","Д","Ж","Э"),
        listOf("⇧","Я","Ч","С","М","И","Т","Ь","Б","Ю","⌫"),
        listOf("☺","123","ПРОБЕЛ","↵")
    )
    private val enRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("⇧","z","x","c","v","b","n","m","⌫"),
        listOf("☺","123","space","↵")
    )
    private val enRowsUp = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("⇧","Z","X","C","V","B","N","M","⌫"),
        listOf("☺","123","SPACE","↵")
    )
    private val numRows = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","*","(",")","_","+"),
        listOf("-","/",":",";","'","\"",",",".","!","?"),
        listOf("ABC","пробел","↵")
    )
    private val stickers = listOf(
        "😀","😂","🥰","😎","🤔","😴","🤩","😭",
        "👍","👎","🙏","🤝","✌️","🤞","💪","👏",
        "❤️","🔥","⭐","🎉","💯","✅","❌","🚀",
        "😡","🥺","😏","🤪","😬","🥳","😒","😮"
    )

    fun setKeyboardService(s: MyKeyboardService) { service = s }
    fun reset() { isShifted = false; isCapsLock = false; showStickers = false; invalidate() }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) { super.onSizeChanged(w,h,oldw,oldh); buildKeys() }

    private fun buildKeys() {
        keys.clear(); stickerKeys.clear()
        val rows = when (currentLayout) {
            Layout.RUSSIAN -> if (isShifted||isCapsLock) ruRowsUp else ruRows
            Layout.ENGLISH -> if (isShifted||isCapsLock) enRowsUp else enRows
            Layout.NUMBERS -> numRows
        }
        val pad = 4f; val rh = height.toFloat() / rows.size
        rows.forEachIndexed { ri, row ->
            val y = ri * rh + pad; val h = rh - pad * 2
            when {
                ri == rows.size - 1 -> buildBottom(row, y, h, pad)
                ri == rows.size - 2 && currentLayout != Layout.NUMBERS -> buildShiftRow(row, y, h, pad)
                else -> {
                    val kw = (width.toFloat() - pad*(row.size+1)) / row.size
                    row.forEachIndexed { i, l -> keys.add(Key(pad + i*(kw+pad), y, kw, h, l)) }
                }
            }
        }
        val sc = 8; val sr = stickers.size/sc
        val sw = width.toFloat()/sc; val sh = height.toFloat()/(sr+1)
        stickers.forEachIndexed { i, s -> stickerKeys.add(Key((i%sc)*sw+pad, (i/sc)*sh+pad, sw-pad*2, sh-pad*2, s)) }
        stickerKeys.add(Key(pad, sr*sh+pad, width-pad*2, sh-pad*2, "← Назад", KeyType.STICKER))
    }

    private fun buildShiftRow(row: List<String>, y: Float, h: Float, pad: Float) {
        val sw = width * 0.13f; val cc = row.size - 2
        val cw = (width - sw*2 - pad*(cc+3)) / cc; var x = pad
        row.forEach { l ->
            when (l) {
                "⇧" -> { keys.add(Key(x,y,sw,h,"⇧",KeyType.SHIFT)); x+=sw+pad }
                "⌫" -> { keys.add(Key(x,y,sw,h,"⌫",KeyType.BACKSPACE)); x+=sw+pad }
                else -> { keys.add(Key(x,y,cw,h,l)); x+=cw+pad }
            }
        }
    }

    private fun buildBottom(row: List<String>, y: Float, h: Float, pad: Float) {
        val spW = width * 0.42f; val sW = (width - spW - pad*(row.size+1)) / (row.size-1); var x = pad
        row.forEach { l ->
            when {
                l.contains("пробел",true)||l.contains("space",true) -> { keys.add(Key(x,y,spW,h,"space",KeyType.SPACE)); x+=spW+pad }
                l=="☺" -> { keys.add(Key(x,y,sW,h,"☺",KeyType.STICKER)); x+=sW+pad }
                l=="123"||l=="ABC" -> { keys.add(Key(x,y,sW,h,l,KeyType.LAYOUT_SWITCH)); x+=sW+pad }
                l=="↵" -> { keys.add(Key(x,y,sW,h,"↵",KeyType.ENTER)); x+=sW+pad }
                else -> { keys.add(Key(x,y,sW,h,l)); x+=sW+pad }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(bgColor)
        if (showStickers) { drawStickers(canvas); return }
        val r = 10f
        keys.forEach { k ->
            val rect = RectF(k.x, k.y, k.x+k.w, k.y+k.h)
            keyPaint.color = when {
                k==pressedKey -> keyPressedColor
                k.type==KeyType.SHIFT&&(isShifted||isCapsLock) -> accentColor
                k.type in listOf(KeyType.BACKSPACE,KeyType.ENTER,KeyType.SHIFT,KeyType.LAYOUT_SWITCH,KeyType.STICKER) -> specialKeyColor
                else -> keyColor
            }
            canvas.drawRoundRect(rect, r, r, keyPaint)
            textPaint.textSize = if(k.type==KeyType.STICKER) k.h*0.5f else k.h*0.42f
            textPaint.color = textColor
            val lbl = if(k.type==KeyType.SPACE) (if(currentLayout==Layout.RUSSIAN)"пробел" else "space") else k.label
            canvas.drawText(lbl, k.x+k.w/2, k.y+k.h*0.65f, textPaint)
        }
        if (isSwipeMode && swipePath.size > 1) {
            val path = Path(); path.moveTo(swipePath[0].x, swipePath[0].y)
            for (i in 1 until swipePath.size) path.lineTo(swipePath[i].x, swipePath[i].y)
            canvas.drawPath(path, swipePaint)
        }
    }

    private fun drawStickers(canvas: Canvas) {
        val r = 14f
        stickerKeys.forEach { k ->
            keyPaint.color = if(k.type==KeyType.STICKER) accentColor else keyColor
            canvas.drawRoundRect(RectF(k.x,k.y,k.x+k.w,k.y+k.h), r, r, keyPaint)
            textPaint.textSize = if(k.type==KeyType.STICKER) k.h*0.35f else k.h*0.55f
            textPaint.color = textColor
            canvas.drawText(k.label, k.x+k.w/2, k.y+k.h*0.65f, textPaint)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val x = e.x; val y = e.y
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX=x; touchStartY=y; swipePath.clear(); swipePath.add(PointF(x,y)); isSwipeMode=false
                pressedKey = findKey(x,y,if(showStickers)stickerKeys else keys); invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                if (dist(touchStartX,touchStartY,x,y)>30f && !showStickers) {
                    isSwipeMode=true; swipePath.add(PointF(x,y))
                    pressedKey=findKey(x,y,keys); invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (showStickers) findKey(x,y,stickerKeys)?.let { handleSticker(it) }
                else if (isSwipeMode) handleSwipe()
                else (pressedKey?:findKey(x,y,keys))?.let { handleKey(it) }
                pressedKey=null; isSwipeMode=false; swipePath.clear(); invalidate()
            }
        }
        return true
    }

    private fun findKey(x: Float, y: Float, list: List<Key>) = list.firstOrNull { x>=it.x&&x<=it.x+it.w&&y>=it.y&&y<=it.y+it.h }

    private fun handleKey(k: Key) {
        when (k.type) {
            KeyType.CHAR -> { service?.commitText(k.label); if(isShifted&&!isCapsLock){isShifted=false;buildKeys()} }
            KeyType.BACKSPACE -> service?.deleteChar()
            KeyType.SPACE -> service?.commitText(" ")
            KeyType.ENTER -> service?.commitText("\n")
            KeyType.STICKER -> { showStickers=!showStickers }
            KeyType.LAYOUT_SWITCH -> {
                currentLayout = if(currentLayout==Layout.NUMBERS) Layout.RUSSIAN else Layout.NUMBERS
                buildKeys()
            }
            KeyType.SHIFT -> {
                val now=System.currentTimeMillis()
                if(now-lastShiftTime<400){isCapsLock=!isCapsLock;isShifted=isCapsLock}
                else{isShifted=!isShifted;isCapsLock=false}
                lastShiftTime=now; buildKeys()
            }
        }
    }

    private fun handleSticker(k: Key) {
        if(k.type==KeyType.STICKER) showStickers=false else service?.commitEmoji(k.label)
    }

    private fun handleSwipe() {
        if(swipePath.size<2) return
        val word=StringBuilder()
        swipePath.forEach { pt ->
            findKey(pt.x,pt.y,keys)?.let { if(it.type==KeyType.CHAR&&it.label.length==1&&(word.isEmpty()||word.last().toString()!=it.label)) word.append(it.label) }
        }
        if(word.length>1) service?.commitText(word.toString()+" ")
    }

    private fun dist(x1:Float,y1:Float,x2:Float,y2:Float):Float { val dx=x2-x1;val dy=y2-y1; return sqrt(dx*dx+dy*dy) }
}
