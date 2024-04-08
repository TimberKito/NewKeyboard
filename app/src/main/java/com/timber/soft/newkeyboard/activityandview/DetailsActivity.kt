package com.timber.soft.newkeyboard.activityandview

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.databinding.ActivityDetailsBinding
import com.timber.soft.newkeyboard.listener.ApplyListener
import com.timber.soft.newkeyboard.tools.DataModel
import com.timber.soft.newkeyboard.tools.AppVal
import com.timber.soft.newkeyboard.tools.StatusBarTools
import net.sf.sevenzipjbinding.ArchiveFormat
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile

class DetailsActivity : AppCompatActivity(), ApplyListener {

    private lateinit var binding: ActivityDetailsBinding
    private lateinit var inputManager: InputMethodManager
    private lateinit var previewUrl: String
    private lateinit var zipPath: String
    private lateinit var dataModel: DataModel
    private lateinit var sp: SharedPreferences

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

        inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        dataModel = intent.getSerializableExtra("KEY_EXTRA") as DataModel

        previewUrl = dataModel.thumb
        val cacheDir = cacheDir
        val dataModelUrl = dataModel.zipUrl
        zipPath = "$cacheDir/$dataModelUrl"


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
                .error(R.drawable.png_loading_err).transform(transformation)
                .into(binding.themeImage)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 初始化键值对
        sp = getSharedPreferences(
            AppVal.SHARE_NAME, Context.MODE_PRIVATE
        )
    }

    private fun applyTheme() {
        // 检查是否启动键盘并设置
        if (!isEnable() || !isChoose()) {
            Toast.makeText(this, getString(R.string.text_promote), Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ApplyActivity::class.java)
            startActivity(intent)
            return
        }

        binding.coverView.visibility = View.VISIBLE

        val file = File(zipPath)
        // 判断缓存中是否存在文件
        if (file.exists()) {
            val allThemePath: String = getAllThemePath(dataModel.title)
            val edit = sp.edit()
            // 存放键值对
            edit.run {
                putString(AppVal.KEY_ALL_PATH, allThemePath)
                apply()
            }
        } else {
            getZipData(
                dataModel.title, dataModel.zipUrl, this@DetailsActivity, this@DetailsActivity
            )
        }
    }

    /**
     * 从指定的URL下载zip文件，并在下载完成后处理该文件。
     *
     * @param title 下载文件的标题
     * @param url 下载文件的URL
     * @param con 上下文Context
     * @param listener 处理文件完成后的回调接口
     */
    private fun getZipData(title: String, url: String, con: Context, listener: ApplyListener) {
        Glide.with(con).asFile().load(url).addListener(object : RequestListener<File> {
            //失败回调
            override fun onLoadFailed(
                e: GlideException?, model: Any?, target: Target<File>, isFirstResource: Boolean
            ): Boolean {
                // 回调失败
                listener.applyListener(false, "")
                return false
            }

            //成功回调
            override fun onResourceReady(
                resource: File,
                model: Any,
                target: Target<File>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                val fileInputStream = FileInputStream(resource)
                dealFile(title, url, fileInputStream, listener)
                return false
            }

        }).preload()
    }

    private fun dealFile(
        title: String, url: String, input: FileInputStream, listener: ApplyListener
    ) {
        val zipPath = "${cacheDir}/${title}_ZIP"
        val unPath = "${cacheDir}/${title}"

        // 将下载的zip文件保存到zipPath路径
        val zipBoolean = writeNewFile(input, zipPath)
        // 随机读写
        val randomAccessFileInStream =
            RandomAccessFileInStream(RandomAccessFile(File(zipPath), "r"))

        val openInArchive = SevenZip.openInArchive(
            ArchiveFormat.SEVEN_ZIP, randomAccessFileInStream
        )

        var out: RandomAccessFileOutStream? = null

        if (zipBoolean) {
            try {
                var filePath: String = ""

                /**
                 * 存档文件中的每个文件项。对于每个文件项，判断是否为文件（非文件夹），
                 * 如果是文件，则将其解压缩到目标路径unPath下的相应位置；
                 * 如果是文件夹，则创建对应的文件夹。
                 */
                openInArchive.simpleInterface.archiveItems.forEach { item ->
                    if (!item.isFolder) {
                        val file = File(unPath, item.path)
                        out = RandomAccessFileOutStream(RandomAccessFile(file, "rw"))
                        item.extractSlow(out)
                        filePath = file.path
                    } else {
                        File(unPath, item.path).mkdirs()
                    }
                }
                //调用接口返回成功
                listener.applyListener(true, filePath)
            } catch (ex: Exception) {
                listener.applyListener(false, "")
            } finally {
                openInArchive.close()
                randomAccessFileInStream.close()
                out?.close()
            }
        }
    }

    private fun writeNewFile(input: InputStream, filePath: String): Boolean {
        var stream: FileOutputStream? = null
        var outStream: ByteArrayOutputStream? = null
        try {
            val outStream = ByteArrayOutputStream()
            val bytes = ByteArray(4096)
            var length = 0
            while (input.read(bytes).also {
                    length = it
                } != -1) {
                outStream.write(bytes, 0, length)
            }
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            }
            stream = FileOutputStream(file)
            stream.run {
                write(outStream.toByteArray())
            }
            outStream.close()
            stream.close()
            return true
        } catch (ex: Exception) {
            outStream?.close()
            stream?.close()
            return false
        }

    }

    private fun getAllThemePath(zip: String): String {
        val result = sp.getString(zip, "")
        return result!!
    }

    /**
     * 检查是否设置键盘
     */
    private fun isChoose(): Boolean {
        Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD).let { id ->
            return id.startsWith(packageName)
        }
    }

    /**
     * 检查是否启用键盘
     */
    private fun isEnable(): Boolean {
        for (info: InputMethodInfo in inputManager.enabledInputMethodList) {
            if (info.id.startsWith(packageName)) {
                return true
            }
        }
        return false
    }

    /**
     * 点击设置后的回调接口
     */
    override fun applyListener(isSuccess: Boolean, str: String) {
        binding.coverView.visibility = View.GONE
        if (isSuccess) {
            val lastIndexOf: Int = str.lastIndexOf(AppVal.res_path)
            val substring = str.subSequence(0, lastIndexOf + AppVal.res_path.length).toString()
            val edit = sp.edit()
            edit.run {
                putString(AppVal.KEY_ALL_PATH, substring)
                apply()
            }
            edit.run {
                putString(dataModel.title, substring)
                apply()
            }
            Toast.makeText(this, getString(R.string.succ_apply), Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.fail_apply), Toast.LENGTH_LONG).show()
        }
    }

}