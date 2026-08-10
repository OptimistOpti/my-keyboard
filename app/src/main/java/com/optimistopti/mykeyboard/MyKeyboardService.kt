package com.optimistopti.mykeyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.LinearLayout

class MyKeyboardService : InputMethodService() {
    private lateinit var keyboardView: MyKeyboardView
    private var themeReceiver: BroadcastReceiver? = null

    val currentInputConnectionCompat: InputConnection?
        get() = currentInputConnection

    override fun onCreate() {
        super.onCreate()
        themeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (::keyboardView.isInitialized) {
                    keyboardView.resetIconCache()
                    keyboardView.invalidate()
                }
            }
        }
        val filter = IntentFilter(ThemeActivity.ACTION_THEME_CHANGED)
        registerReceiver(themeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        themeReceiver?.let { unregisterReceiver(it) }
    }

    override fun onCreateInputView(): View {
        val layout = layoutInflater.inflate(R.layout.keyboard_main, null) as LinearLayout
        keyboardView = layout.findViewById(R.id.keyboard_view)
        keyboardView.setKeyboardService(this)
        return layout
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        keyboardView.reset()
    }

    fun commitText(text: String) { currentInputConnection?.commitText(text, 1) }

    fun deleteChar() { currentInputConnection?.deleteSurroundingText(1, 0) }

    fun deleteWord() {
        val conn = currentInputConnection ?: return
        val text = conn.getTextBeforeCursor(50, 0) ?: return
        val trimmed = text.trimEnd()
        val lastSpace = trimmed.lastIndexOf(' ')
        val toDelete = if (lastSpace >= 0) trimmed.length - lastSpace else trimmed.length
        conn.deleteSurroundingText(toDelete.coerceAtLeast(1), 0)
    }

    fun commitEmoji(emoji: String) { currentInputConnection?.commitText(emoji, 1) }

    fun performEnter() {
        super.sendDefaultEditorAction(true)
    }
}
