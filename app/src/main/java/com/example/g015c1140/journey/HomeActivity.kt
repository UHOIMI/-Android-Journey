package com.example.g015c1140.journey

import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        aiueoreview.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(aiueoreview)

        val pageControlRecyclerViewAdapter = PageControlRecyclerViewAdapter(this,this, getListData())
        aiueoreview.adapter = pageControlRecyclerViewAdapter

        val pageControlView = findViewById<PageControlView>(R.id.pageControlView)
        pageControlView.setRecyclerView(aiueoreview, layoutManager)
    }


    private fun getListData(): ArrayList<TimelinePlanData>{
        val list = arrayListOf<TimelinePlanData>()

        var timelinePlanData:TimelinePlanData
        for (i in 0 until 5){
            timelinePlanData = TimelinePlanData(
                    0,
                    "mm",
                    BitmapFactory.decodeResource(resources, R.drawable.no_image),
                    "テスト$i",
                    "title",
                    BitmapFactory.decodeResource(resources, R.drawable.no_image),
                    arrayListOf("supot 1","supot 2","supot 3"),
                    "00月00日",
                    "100"
            )
            list.add(timelinePlanData)
        }
        return list
    }
}
