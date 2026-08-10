package com.optimistopti.mykeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator

class SetupActivity : BaseSettingsActivity() {

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_setup)
    }

    override fun onResume() {
        super.onResume()
        updateSteps()
    }

    private fun updateSteps() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val isEnabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        val isSelected = (imm.currentInputMethodSubtype != null &&
            imm.enabledInputMethodList.firstOrNull { it.packageName == packageName } != null) ||
            isKeyboardSelected(imm)

        // Step 1 — enable
        val step1Icon = findViewById<TextView>(R.id.step1_icon)
        val step1Desc = findViewById<TextView>(R.id.step1_desc)
        val btn1      = findViewById<MaterialButton>(R.id.btn_step1)
        step1Icon.text = if (isEnabled) "✅" else "1️⃣"
        step1Desc.text = if (isEnabled) "Клавиатура включена" else "Включить KeyBoard You в настройках"
        btn1.text      = if (isEnabled) "Готово" else "Включить"
        btn1.isEnabled = !isEnabled
        btn1.setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }

        // Step 2 — select
        val step2Icon = findViewById<TextView>(R.id.step2_icon)
        val step2Desc = findViewById<TextView>(R.id.step2_desc)
        val btn2      = findViewById<MaterialButton>(R.id.btn_step2)
        step2Icon.text = if (isSelected) "✅" else "2️⃣"
        step2Desc.text = if (isSelected) "KeyBoard You выбрана по умолчанию" else "Выбрать KeyBoard You активной клавиатурой"
        btn2.text      = if (isSelected) "Готово" else "Выбрать"
        btn2.isEnabled = isEnabled && !isSelected
        btn2.setOnClickListener { imm.showInputMethodPicker() }

        // Progress
        val progress = findViewById<LinearProgressIndicator>(R.id.setup_progress)
        progress.progress = when {
            isEnabled && isSelected -> 100
            isEnabled               -> 50
            else                    -> 0
        }

        // Continue button
        val btnContinue = findViewById<MaterialButton>(R.id.btn_continue)
        btnContinue.isEnabled = isEnabled
        btnContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun isKeyboardSelected(imm: InputMethodManager): Boolean {
        // Check via default method ID
        val defMethod = android.provider.Settings.Secure.getString(
            contentResolver, android.provider.Settings.Secure.DEFAULT_INPUT_METHOD)
        return defMethod?.contains(packageName) == true
    }
}
