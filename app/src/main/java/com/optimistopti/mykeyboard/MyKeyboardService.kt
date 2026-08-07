package com.optimistopti.mykeyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout

class MyKeyboardService : InputMethodService() {
    private lateinit var keyboardView: MyKeyboardView

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
    fun commitEmoji(emoji: String) { currentInputConnection?.commitText(emoji, 1) }
}
