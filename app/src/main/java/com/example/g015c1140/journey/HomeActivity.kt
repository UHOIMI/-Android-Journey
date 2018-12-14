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


        val layoutManager =  arrayListOf<LinearLayoutManager>()
        for(_layoutCnt in 0 until 4){
            layoutManager.add(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        }

        areaRecyclerView.layoutManager = layoutManager[0]
        newPlanRecyclerView.layoutManager = layoutManager[1]
        favoritePlanRecyclerView.layoutManager = layoutManager[2]
        userGenerationPlanRecyclerView.layoutManager = layoutManager[3]

        val snapHelper =  arrayListOf<PagerSnapHelper>()
        for(_layoutCnt in 0 until 4){
            snapHelper.add(PagerSnapHelper())
        }

        snapHelper[0].attachToRecyclerView(areaRecyclerView)
        snapHelper[1].attachToRecyclerView(newPlanRecyclerView)
        snapHelper[2].attachToRecyclerView(favoritePlanRecyclerView)
        snapHelper[3].attachToRecyclerView(userGenerationPlanRecyclerView)

        val planListData = getPlanListData()
        val areaListData = getAreaListData()

        val areaRecyclerViewAdapter = AreaPageControlRecyclerViewAdapter(this,this,areaListData)
        val newPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this,this, planListData)
        val favoritePlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this,this, planListData)
        val userGenerationPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this,this, planListData)

        areaRecyclerView.adapter = areaRecyclerViewAdapter
        newPlanRecyclerView.adapter = newPlanRecyclerViewAdapter
        favoritePlanRecyclerView.adapter = favoritePlanRecyclerViewAdapter
        userGenerationPlanRecyclerView.adapter = userGenerationPlanRecyclerViewAdapter

        areaPageControlView.setRecyclerView(areaRecyclerView, layoutManager[0])
        newPlanPageControlView.setRecyclerView(newPlanRecyclerView, layoutManager[1])
        favoritePlanPageControlView.setRecyclerView(favoritePlanRecyclerView, layoutManager[2])
        userGenerationPlanPageControlView.setRecyclerView(userGenerationPlanRecyclerView, layoutManager[3])
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
    private fun getPlanListData(): ArrayList<TimelinePlanData>{
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

    private fun getAreaListData(): ArrayList<HomeAreaData>{
        val areaList = arrayListOf<HomeAreaData>()

        var homeAreaData:HomeAreaData
        for (i in 0 until 8){
            homeAreaData = HomeAreaData(
                    "エリア $i",
                    BitmapFactory.decodeResource(resources, R.drawable.no_image),
                    "エリアAPI $i"
            )
            areaList.add(homeAreaData)
        }
        return areaList
    }

}
