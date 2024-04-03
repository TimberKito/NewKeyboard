package com.timber.soft.newkeyboard.activity

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.databinding.ActivityMainBinding
import com.timber.soft.newkeyboard.fragment.VPFragment
import com.timber.soft.newkeyboard.model.JsonDeserializer.parseJsonFromAssets
import com.timber.soft.newkeyboard.model.RootModel
import com.timber.soft.newkeyboard.tools.StatusBarTools.dpCovertPx

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fragmentList: ArrayList<Fragment>
    private val rootModelList: MutableList<RootModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 获取ConnectivityManager实例
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        // 检查网络连接状态
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            // 已连接到互联网
            Log.d("NetworkStatus", "Connected to the Internet")
        } else {
            // 未连接到互联网
            Log.d("NetworkStatus", "Not connected to the Internet")
        }

        // 设置Padding上边距留出沉浸式状态栏空间
        binding.root.setPadding(0, dpCovertPx(this), 0, 0)
        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE) or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }

        initDrawer()

        initTabLayOut()

        binding.viewpager.offscreenPageLimit = 3
        binding.viewpager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            override fun getPageTitle(position: Int): CharSequence {
                return rootModelList[position].className
            }
        }
        binding.tabLayout.setupWithViewPager(binding.viewpager)

    }

    private fun initTabLayOut() {
        val result = parseJsonFromAssets(this@MainActivity, "keyboard.json")
        if (result != null) {
            rootModelList.addAll(result)
        }

        rootModelList.shuffle()

        for (i in rootModelList) {
            binding.tabLayout.addTab(
                binding.tabLayout.newTab().setCustomView(R.layout.item_tab)
            )
        }

        fragmentList = arrayListOf()

        for (i in 0 until binding.tabLayout.tabCount) {
            val tabView = binding.tabLayout.getTabAt(i)?.customView
            if (tabView != null) {
                val rootModel = rootModelList[i]
                val textName = tabView.findViewById<TextView>(R.id.keyboard_kind_name)
                textName.text = rootModel.className
                fragmentList.add(VPFragment(rootModel))
            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(p0: TabLayout.Tab?) {
                setTabSize(p0)
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                p0?.customView = null
            }

            override fun onTabReselected(p0: TabLayout.Tab?) {
                // null
            }
        })
    }

    private fun setTabSize(p0: TabLayout.Tab?) {
        val textView = TextView(this)
        //字体样式
        val selectedSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 22f, resources.displayMetrics)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, selectedSize)
        textView.typeface = Typeface.defaultFromStyle(Typeface.BOLD) //加粗
        textView.gravity = Gravity.CENTER
        //选中的字体颜色
        textView.setTextColor(ContextCompat.getColor(this, R.color.theme_color))
        textView.text = p0!!.text
        p0.customView = textView
    }

    private fun initDrawer() {
        binding.layoutRate.setOnClickListener() {
            val url = getString(R.string.share_link) + packageName
            // 创建intent打开链接
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse(url))
            startActivity(intent)
        }
        // 绑定抽屉中的分享按钮
        binding.layoutShare.setOnClickListener() {
            // 商店中包的位置
            val url = getString(R.string.share_link) + packageName
            val intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/plain")
            intent.putExtra(Intent.EXTRA_TEXT, url)
            startActivity(intent)
        }
        // 绑定抽屉中的版本信息
        val versionName = getVersionName()
        binding.textAppVersion.text = versionName

        // 打开抽屉
        binding.imageMenu.setOnClickListener() {
            binding.drawerParent.openDrawer(GravityCompat.START)
        }

        binding.drawerParent.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                // 设置监听事件防止 Drawer 穿透
                drawerView.isClickable = true
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
    }

    private fun getVersionName(): String {
        val pInfo: PackageInfo
        try {
            pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return ""
        }
        return "Version: " + pInfo.versionName
    }
}