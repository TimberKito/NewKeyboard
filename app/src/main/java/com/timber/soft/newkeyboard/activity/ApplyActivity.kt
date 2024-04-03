package com.timber.soft.newkeyboard.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.timber.soft.newkeyboard.databinding.ActivityApplyBinding
import com.timber.soft.newkeyboard.tools.StatusBarTools

class ApplyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyBinding
    private lateinit var inputManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // 设置Padding上边距留出沉浸式状态栏空间
        binding.root.setPadding(0, StatusBarTools.dpCovertPx(this), 0, 0)
        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }
        inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        binding.applyBack.setOnClickListener() {
            finish()
        }
        binding.idStep1.setOnClickListener() {
            applyKeyboard()
        }
        binding.idStep2.setOnClickListener() {
            chooseKeyboard()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUi()
    }

    private fun chooseKeyboard() {
        inputManager.showInputMethodPicker()
    }

    private fun applyKeyboard() {
        Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            startActivity(this)
        }
    }

    private fun updateUi() {
        binding.idStep1.setSelected(isEnable())
        binding.idStep2.setSelected(isChoose())
    }

    private fun isChoose(): Boolean {
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).let { id ->
            return id.startsWith(packageName)
        } ?: return false
    }

    private fun isEnable(): Boolean {
        for (info: InputMethodInfo in inputManager.enabledInputMethodList) {
            if (info.id.startsWith(packageName)) {
                return true
            }
        }
        return false
    }

    inner class StepReceive : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUi()
        }
    }

}