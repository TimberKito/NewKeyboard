package com.timber.soft.newkeyboard.tools

import android.content.Context

object StatusBarTools {
    fun dpCovertPx(context: Context): Int {
        // 获取当前设备的屏幕密度，并赋值给变量 scale
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}