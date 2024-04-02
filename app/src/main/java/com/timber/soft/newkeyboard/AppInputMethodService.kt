package com.timber.soft.newkeyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener

class AppInputMethodService :InputMethodService(),OnKeyboardActionListener{


    /**
     * 向用户展示
     */
    override fun onWindowShown() {
        super.onWindowShown()
    }



    /**
     * Called when the user presses a key.
     *
     * 监听特定的按钮
     */
    override fun onPress(primaryCode: Int) {
        TODO("Not yet implemented")
    }

    /**
     *  Send a key press to the listener.
     *
     *  监听用户点击的键盘按键
     */
    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        TODO("Not yet implemented")
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