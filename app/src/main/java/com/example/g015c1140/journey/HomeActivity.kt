package com.example.g015c1140.journey

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(homeBottomNavigation)
        homeBottomNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)


        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        aiueoreview.layoutManager = layoutManager

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(aiueoreview)

        val pageControlRecyclerViewAdapter = PageControlRecyclerViewAdapter(this,this, getListData())
        aiueoreview.adapter = pageControlRecyclerViewAdapter

        val pageControlView = findViewById<PageControlView>(R.id.pageControlView)
        pageControlView.setRecyclerView(aiueoreview, layoutManager)
    }

    fun userIconButtonTapped(view: View){
        Toast.makeText(this,"あいこんたっぷど",Toast.LENGTH_SHORT).show()
    }

    fun planSpotButtonTapped(view: View){
        Toast.makeText(this,"ぷらんすぽっとたっぷど",Toast.LENGTH_SHORT).show()
    }

    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this,DetailUserActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //テスト用データ作成
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
