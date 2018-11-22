package com.example.g015c1140.journey

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_timeline.*
import layout.TimelinePlanListAdapter



class TimelineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        //タイトル名セット
        title = "タイムライン"

        val toolbar = toolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomavigation: BottomNavigationView = findViewById(R.id.navigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomavigation)
        bottomavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        //引っ張って更新用
        timelineSwipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        timelineSwipeRefresh.setOnRefreshListener {
            // 引っ張って離した時に呼ばれます。
            Toast.makeText(this,"更新開始",Toast.LENGTH_SHORT).show()
            try {
            } catch (e: InterruptedException) {
                Toast.makeText(this,"erorr",Toast.LENGTH_SHORT).show()
            }

        }

        val planList = arrayListOf<TimelinePlan>()
        val timelinePlanListAdapter = TimelinePlanListAdapter(this)
        timelinePlanListAdapter.setTimelinePlanList(planList)
        timelineListView.adapter = timelinePlanListAdapter

        for (_cnt in 0 until 100){
            val timelinePlan = TimelinePlan()
            timelinePlan.planUserName = "Name $_cnt"
            timelinePlan.planTitle = "Title $_cnt"
            timelinePlan.planSpotNameList = arrayListOf("SName1 $_cnt","SName2 $_cnt", "SName3 $_cnt")
            timelinePlan.planTime = "${_cnt}月${_cnt}日"
            timelinePlan.planFavorite = "$_cnt"
            timelinePlan.planSpotImage = BitmapFactory.decodeResource(resources,R.drawable.no_image)
            timelinePlan.planUserIconImage = BitmapFactory.decodeResource(resources,R.drawable.no_image)
            planList.add(timelinePlan)
        }
        timelinePlanListAdapter.notifyDataSetChanged()
    }

    //ToolBarのボタン処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //戻るボタンタップ時
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                listUpdateFinish()
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

    private fun listUpdateFinish(){
        timelineSwipeRefresh.isRefreshing = false
    }
}
