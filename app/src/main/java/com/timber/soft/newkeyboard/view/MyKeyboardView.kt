package com.timber.soft.newkeyboard.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Build
import android.util.AttributeSet
import android.util.Xml
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.tools.AppVal
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.StringReader

@Suppress("DEPRECATION")
class MyKeyboardView @JvmOverloads constructor(
    var myContext: Context,
    attributeSet: AttributeSet? = null,
    style: Int = 0
) : KeyboardView(myContext, attributeSet, style) {

    inner class MyConfig {
        lateinit var functionBackgroundDraw: Drawable
        lateinit var spBackgroundDraw: Drawable
        lateinit var normalBackgroundDraw: Drawable
        var icShittLock: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.svg_shift_lit)
        var icDel: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.svg_dele)
        var allBg: Drawable? =
            ContextCompat.getDrawable(context, R.mipmap.main_bg)
        var icBshift: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.svg_shift_lit)
        var icSshift: Drawable? =
            ContextCompat.getDrawable(context, R.drawable.svg_shift_lit)

        @RequiresApi(Build.VERSION_CODES.M)
        var keycolor: Int = context.resources.getColor(R.color.white, null)

        private val sp: SharedPreferences = context.getSharedPreferences(
            AppVal.SHARE_NAME,
            Context.MODE_PRIVATE
        )

        private fun getbgic(con: Context, filePath: String): Drawable? {
            if (!File(filePath).exists()) {
                return null
            }
            return BitmapDrawable(con.resources, BitmapFactory.decodeFile(filePath))
        }

        private fun getStatus(draw: Drawable, drawPress: Drawable): StateListDrawable {
            return StateListDrawable().apply {
                addState(intArrayOf(android.R.attr.state_pressed), drawPress)
                addState(intArrayOf(), draw)
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        private fun gettextcolor(colorXmlPath: String) {
            val file = File(colorXmlPath)
            if (!file.exists()) return
            val xmlP = Xml.newPullParser()

            xmlP.setInput(StringReader(file.readText()))
            xmlP.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)

            var eventT = xmlP.eventType
            while (eventT != XmlPullParser.END_DOCUMENT) {
                if (eventT == XmlPullParser.START_TAG && (xmlP.name == "color" || xmlP.name == "item")) {
                    val value = xmlP.getAttributeValue(null, "name")
                    if (value != null && value == AppVal.title_color) {
                        keycolor = Color.parseColor(xmlP.nextText())
                    }
                }
                eventT = xmlP.next()
            }


        }


        init {
            val default =
                ContextCompat.getDrawable(context, R.drawable.png_keybg)
            val press = ContextCompat.getDrawable(
                context,
                R.drawable.png_keybg_press
            )
            if (press != null) {
                if (default != null) {
                    val listDrawable = StateListDrawable().apply {
                        addState(intArrayOf(android.R.attr.state_pressed), press)
                        addState(intArrayOf(), default)
                    }
                    functionBackgroundDraw = listDrawable
                    normalBackgroundDraw = listDrawable
                    spBackgroundDraw = listDrawable
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.M)
        fun updateConfig(con: Context) {
            sp.getString(AppVal.KEY_ALL_PATH, "")?.let {
                getbgic(
                    con,
                    it.plus(AppVal.parent_path).plus(AppVal.title_nor_Bg)
                )?.let { drawBG ->
                    getbgic(
                        con,
                        it.plus(AppVal.parent_path).plus(AppVal.title_nor_Bg_press)
                    )?.let { drawPressBG ->
                        normalBackgroundDraw = getStatus(drawBG, drawPressBG)
                    }

                }
                gettextcolor(it.plus(AppVal.color_path))
                getbgic(con, it.plus(AppVal.parent_path).plus(AppVal.title_sp_Bg))?.let { drawBG ->
                    getbgic(
                        con,
                        it.plus(AppVal.parent_path).plus(AppVal.title_sp_Bg_press)
                    )?.let { drawPressBG ->
                        spBackgroundDraw = getStatus(drawBG, drawPressBG)
                    }

                }
                getbgic(con, it.plus(AppVal.xx_path).plus(AppVal.title_bg))?.run {
                    allBg = this
                }

                getbgic(
                    con,
                    it.plus(AppVal.parent_path).plus(AppVal.title_fun_Bg)
                )?.let { drawBG ->
                    getbgic(
                        con,
                        it.plus(AppVal.parent_path).plus(AppVal.title_func_bg_press)
                    )?.let { drawPressBG ->
                        functionBackgroundDraw = getStatus(drawBG, drawPressBG)
                    }

                }


                getbgic(con, it.plus(AppVal.parent_path).plus(AppVal.title_shitf_ic))?.let {
                    icSshift = it
                    icBshift = it
                }
                getbgic(
                    con,
                    it.plus(AppVal.parent_path).plus(AppVal.title_del_ic)
                )?.let { drawBG ->
                    icDel = getStatus(drawBG, drawBG)


                }
                getbgic(con, it.plus(AppVal.parent_path).plus(AppVal.title_shitf_ic_lock))?.let {
                    icShittLock = it
                }

            }
        }
    }

    var config = MyConfig()
    var shiftStatus = AppVal.Shift_S
    var xmlMode = AppVal.xml0

    @RequiresApi(Build.VERSION_CODES.M)
    private var myPaint: Paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = myContext.resources.displayMetrics.scaledDensity * 16f
        color = config.keycolor
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SuspiciousIndentation")
    private fun andDraw(
        myKey: Keyboard.Key,
        keyBG: Drawable,
        icon: Drawable?,
        canvas: Canvas,
    ) {
        myKey.run {
            keyBG.run {
                bounds = Rect(
                    x.plus(paddingLeft),
                    y.plus(paddingTop),
                    width.plus(x.plus(paddingLeft)),
                    height.plus(y.plus(paddingTop))
                )
                state = currentDrawableState
                draw(canvas)
            }
        }
        myKey.run {
            icon?.apply {
                myKey.icon = this

                var icon_w = myKey.icon.intrinsicWidth.toFloat()
                var icon_wr = icon_w / myKey.width.toFloat()
                var icon_h = myKey.icon.intrinsicHeight.toFloat()
                var icon_hr = icon_h / myKey.height.toFloat()


                var tep1 = 0f
                var tep2 = 0f
                if (icon_wr > icon_hr) {
                    tep2 = icon_wr
                    tep1 = icon_wr.coerceAtLeast(0.5f)

                } else {
                    tep2 = icon_hr
                    tep1 = icon_hr.coerceAtLeast(0.5f)

                }
                icon_h = (icon_h / tep2) * tep1
                icon_w = (icon_w / tep2) * tep1
                myKey.icon.let {
                    it.bounds = Rect().apply {

                        top =
                            (myKey.y + paddingTop + (myKey.height - icon_h) / 2f).toInt()
                        left =
                            (myKey.x + paddingLeft + (myKey.width - icon_w) / 2f).toInt()
                        bottom = (top + icon_h).toInt()
                        right = (left + icon_w).toInt()

                    }
                    it.draw(canvas)
                }
            }

            myPaint.color = config.keycolor
            if (!label.isNullOrEmpty()) {
                val y1 = y.plus(paddingRight).plus((height.div(2f)))
                    .plus((myPaint.textSize.minus(myPaint.descent())).div(2f))
                val x1 = x.plus(paddingLeft).plus((width.div(2f)))
                canvas.drawText(label.toString(), x1, y1, myPaint)
            }
        }
    }

    private fun getCurIc(): Drawable? {
        return when (shiftStatus) {
            AppVal.Shift_B_lo -> config.icShittLock
            AppVal.Shift_B -> config.icBshift
            AppVal.Shift_S -> config.icSshift
            else -> config.icSshift
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        keyboard.keys.forEach {
            when (it.codes[0]) {
                Keyboard.KEYCODE_SHIFT -> {
                    andDraw(
                        it,
                        config.functionBackgroundDraw,
                        getCurIc(),
                        canvas
                    )
                }

                AppVal.SHIFT_NUMBER, AppVal.SHIFT_SYMBOL -> {
                    andDraw(it, config.functionBackgroundDraw, null, canvas)
                }

                Keyboard.KEYCODE_DELETE -> {
                    andDraw(
                        it,
                        config.functionBackgroundDraw,
                        config.icDel,
                        canvas
                    )
                }

                Keyboard.KEYCODE_MODE_CHANGE, Keyboard.KEYCODE_DONE -> {
                    andDraw(
                        it,
                        config.functionBackgroundDraw,
                        null,
                        canvas
                    )
                }

                else -> {
                    andDraw(it, config.normalBackgroundDraw, null, canvas)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun upUi(con: Context) {
        config.updateConfig(con)
        background = config.allBg
        invalidate()
    }

}