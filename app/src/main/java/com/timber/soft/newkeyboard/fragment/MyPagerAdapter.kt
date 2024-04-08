package com.timber.soft.newkeyboard.fragment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.tools.DataModel
import com.timber.soft.newkeyboard.tools.RootModel

class MyPagerAdapter(
    private val context: Context,
    private val model: RootModel,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MyPagerAdapter.PreViewHolder>() {
    private val dataModels = model.list

    inner class PreViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        /**
         * 640*444
         */
        fun loadPreImg(context: Context, thumb: String, imgItemView: ImageView) {
            try {
                Glide.with(context).load(thumb)
                    // 缓存
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    // 加载占位图
                    .apply(
                        RequestOptions().placeholder(R.drawable.png_loading)
                    )
                    // 淡入动画
                    .transition(DrawableTransitionOptions.withCrossFade())
                    // 加载失败占位图
                    .error(R.drawable.png_loading_err).into(imgItemView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 获取需要渲染图片的item
        val imgItemView: ImageView = itemView.findViewById(R.id.image_item)

        // 获取图片根节点
        val rootItemLayout = itemView.findViewById<LinearLayout>(R.id.root_layout)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): PreViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_pre_img, parent, false)
        return PreViewHolder(view)
    }

    override fun onBindViewHolder(holder: PreViewHolder, position: Int) {
        val dataModel = dataModels[position % dataModels.size]
        holder.loadPreImg(context, dataModel.thumb, holder.imgItemView)

        holder.rootItemLayout.setOnClickListener() {
            listener.onItemClick(position, dataModel)
        }

    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, dataModel: DataModel)
    }

}