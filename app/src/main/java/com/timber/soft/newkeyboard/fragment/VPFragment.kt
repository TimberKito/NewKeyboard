package com.timber.soft.newkeyboard.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import com.timber.soft.newkeyboard.R
import com.timber.soft.newkeyboard.adapter.MyPagerAdapter
import com.timber.soft.newkeyboard.model.DataModel
import com.timber.soft.newkeyboard.model.RootModel

class VPFragment(private val rootModel: RootModel) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pre_recyclerview, container, false)

        val infoRecyclerView: RecyclerView = view.findViewById(R.id.info_recycler_view)

        infoRecyclerView.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)

        val pagerAdapter = MyPagerAdapter(
            requireContext(), rootModel,
            object : MyPagerAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, dataModel: DataModel) {
//                    val intent = Intent(requireContext(), SetImgActivity::class.java)
//                    intent.putExtra("KEY_EXTRA", dataModel)

                    Log.e("onClick", "item has been click!")
                    Toast.makeText(
                        context,
                        "item has been click!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        infoRecyclerView.adapter = pagerAdapter
        return view

    }

}