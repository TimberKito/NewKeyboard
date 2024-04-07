package com.timber.soft.newkeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener
import android.os.Build
import android.os.SystemClock
import android.view.KeyboardShortcutGroup
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import com.timber.soft.newkeyboard.databinding.ViewInputBinding
import com.timber.soft.newkeyboard.tools.AppVal

class AppInputMethodService : InputMethodService(), OnKeyboardActionListener {

    private lateinit var binding: ViewInputBinding
    private val views =
        intArrayOf(R.xml.keyboard_letter, R.xml.keyboard_number, R.xml.keyboard_symbol)
    private var mouble = false
    private var laTime = -3L

    /**
     * 大小写转换
     */
    private fun keyCase(toBig: Boolean, keyboard: Keyboard) {
        for (key in keyboard.keys) {
            if (!key.label.isNullOrEmpty()) {
                if (key.label.length == 1) {
                    var strin: Char = if (toBig) {
                        key.label.toString()[0].uppercaseChar()
                    } else {
                        key.label.toString()[0].lowercaseChar()
                    }
                    key.run {
                        label = strin.toString()
                        codes[0] = strin.code
                    }
                }
            }
        }
    }

    /**
     * 切换键盘
     */
    private fun changeXml(mode: Int) {
        binding.myCustomInput.run {
            when (mode) {
                0 -> {
                    xmlMode = AppVal.xml0
                    keyboard = Keyboard(context, views[0])
                }

                2 -> {
                    xmlMode = AppVal.xml2
                    keyboard = Keyboard(context, views[2])

                }

                1 -> {
                    xmlMode = AppVal.xml1
                    keyboard = Keyboard(context, views[1])
                }
            }
        }
    }

    /**
     * 构建键盘视图
     */
    override fun onCreateInputView(): View {
        binding = ViewInputBinding.inflate(layoutInflater, null, false)
        binding.myCustomInput.setOnKeyboardActionListener(this)
        binding.myCustomInput.run {
            keyboard = Keyboard(this@AppInputMethodService, views[0])
            isEnabled = true
        }
        return binding.root
    }

    /**
     * 向用户展示键盘时候调用
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onWindowShown() {
        super.onWindowShown()
        binding.myCustomInput.upUi(this@AppInputMethodService)
    }

    /**
     * Called when the user presses a key.
     *
     * 监听特定的按钮
     */
    override fun onPress(primaryCode: Int) {
        mouble = false
        if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            if (300 > SystemClock.elapsedRealtime() - laTime) {
                mouble = true
            }
            laTime = SystemClock.elapsedRealtime()
        }
    }

    /**
     *  Send a key press to the listener.
     *
     *  监听用户点击的键盘按键
     */
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {

        when (primaryCode) {
            Keyboard.KEYCODE_SHIFT -> {
                binding.myCustomInput.run {
                    val myKeyboard = keyboard
                    when (shiftStatus) {
                        AppVal.Shift_S -> {
                            shiftStatus = if (mouble) {
                                AppVal.Shift_B_lo
                            } else {
                                AppVal.Shift_B
                            }
                            keyCase(true, myKeyboard)
                            keyboard = myKeyboard
                        }

                        AppVal.Shift_B_lo -> {
                            shiftStatus = AppVal.Shift_S
                            keyCase(false, myKeyboard)
                            keyboard = myKeyboard
                        }

                        AppVal.Shift_B -> {
                            shiftStatus = if (mouble) {
                                AppVal.Shift_B_lo
                            } else {
                                keyCase(false, myKeyboard)
                                AppVal.Shift_S
                            }
                            keyboard = myKeyboard
                        }
                    }
                }
            }

            // 点击完成
            Keyboard.KEYCODE_DONE -> {
                currentInputConnection.performEditorAction(EditorInfo.IME_ACTION_DONE)
            }

            Keyboard.KEYCODE_MODE_CHANGE -> {
                binding.myCustomInput.run {
                    if (xmlMode == AppVal.xml0) {
                        changeXml(1)
                    } else {
                        changeXml(0)
                    }
                }
            }

            AppVal.SHIFT_NUMBER -> {
                changeXml(2)
            }

            Keyboard.KEYCODE_DELETE -> {
                currentInputConnection.deleteSurroundingText(1, 0)
            }

            AppVal.SHIFT_SYMBOL -> {
                changeXml(1)
            }

            else -> {
                currentInputConnection.commitText(primaryCode.toChar().toString(), 1)
                binding.myCustomInput.keyboard = binding.myCustomInput.apply {
                    if (shiftStatus == AppVal.Shift_B) {
                        shiftStatus = AppVal.Shift_S
                        keyCase(false, binding.myCustomInput.keyboard)
                    }
                }.keyboard
            }

        }

    }

    override fun onText(text: CharSequence?) {

    }

    override fun swipeLeft() {

    }

    override fun swipeRight() {

    }

    override fun swipeDown() {

    }

    override fun swipeUp() {

    }

    override fun onRelease(primaryCode: Int) {

    }
}