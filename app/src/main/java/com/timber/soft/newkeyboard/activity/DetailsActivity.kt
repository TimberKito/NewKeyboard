package com.timber.soft.newkeyboard.activity

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.databinding.ActivityDetailsBinding
import com.timber.soft.newkeyboard.model.DataModel
import com.timber.soft.newkeyboard.tools.StatusBarTools

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var previewUrl: String
    private lateinit var zipPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
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

        val dataModel = intent.getSerializableExtra("KEY_EXTRA") as DataModel
        previewUrl = dataModel.thumb
        zipPath = dataModel.zipUrl



        binding.detailsBack.setOnClickListener(View.OnClickListener {
            finish()
        })
        binding.themeSet.setOnClickListener(View.OnClickListener {
            applyTheme()
        })
        binding.themeName.text = dataModel.title



        binding.themePreloading.visibility = View.VISIBLE
        val radius = 80 // 圆角半径，单位为像素
        val transformation = RoundedCorners(radius)
        try {
            Glide.with(this@DetailsActivity).load(previewUrl)
                // 缓存
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.themePreloading.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.themePreloading.visibility = View.GONE
                        return false
                    }
                })
                // 淡入动画
                .transition(DrawableTransitionOptions.withCrossFade())
                // 加载失败占位图
                .error(R.drawable.png_loading_err)
                .transform(transformation)
                .into(binding.themeImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    private fun applyTheme() {

    }

}