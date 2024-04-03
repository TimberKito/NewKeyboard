package com.timber.soft.newkeyboard.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.timber.soft.newkeyboard.databinding.ActivityApplyBinding
import com.timber.soft.newkeyboard.tools.StatusBarTools

class ApplyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplyBinding

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


        binding.applyBack.setOnClickListener() {
            finish()
        }


    }
}