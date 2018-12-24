package com.example.g015c1140.journey

import android.content.Context
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

        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        homeUserGenerationTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION, "none")

        val layoutManager = arrayListOf<LinearLayoutManager>()
        for (_layoutCnt in 0 until 4) {
            layoutManager.add(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        }

        areaRecyclerView.layoutManager = layoutManager[0]
        newPlanRecyclerView.layoutManager = layoutManager[1]
        favoritePlanRecyclerView.layoutManager = layoutManager[2]
        userGenerationPlanRecyclerView.layoutManager = layoutManager[3]

        val snapHelper = arrayListOf<PagerSnapHelper>()
        for (_layoutCnt in 0 until 3) {
            snapHelper.add(PagerSnapHelper())
        }
        snapHelper[0].attachToRecyclerView(newPlanRecyclerView)
        snapHelper[1].attachToRecyclerView(favoritePlanRecyclerView)
        snapHelper[2].attachToRecyclerView(userGenerationPlanRecyclerView)

        homeFab.setOnClickListener {
            Toast.makeText(this,"homeFab", Toast.LENGTH_SHORT).show()
        }

        val planListData = getPlanListData()
        val areaListData = getAreaListData()

        val areaRecyclerViewAdapter = AreaPageControlRecyclerViewAdapter(this, this, areaListData)
        val newPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this, this, planListData)
        val favoritePlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this, this, planListData)
        val userGenerationPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this, this, planListData)

        areaRecyclerView.adapter = areaRecyclerViewAdapter
        newPlanRecyclerView.adapter = newPlanRecyclerViewAdapter
        favoritePlanRecyclerView.adapter = favoritePlanRecyclerViewAdapter
        userGenerationPlanRecyclerView.adapter = userGenerationPlanRecyclerViewAdapter

        newPlanPageControlView.setRecyclerView(newPlanRecyclerView, layoutManager[1])
        favoritePlanPageControlView.setRecyclerView(favoritePlanRecyclerView, layoutManager[2])
        userGenerationPlanPageControlView.setRecyclerView(userGenerationPlanRecyclerView, layoutManager[3])
    }

    fun userIconButtonTapped(view: View) {
        Toast.makeText(this, "あいこんたっぷど", Toast.LENGTH_SHORT).show()
    }

    fun planSpotButtonTapped(view: View) {
        Toast.makeText(this, "ぷらんすぽっとたっぷど", Toast.LENGTH_SHORT).show()
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
                startActivity(Intent(this, DetailUserActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //テスト用データ作成
    private fun getPlanListData(): ArrayList<TimelinePlanData> {
        val list = arrayListOf<TimelinePlanData>()

        var timelinePlanData: TimelinePlanData
        for (i in 0 until 5) {
            timelinePlanData = TimelinePlanData(
                    0,
                    "mm",
                    BitmapFactory.decodeResource(resources, R.drawable.no_image),
                    "テスト$i",
                    "title",
                    BitmapFactory.decodeResource(resources, R.drawable.no_image),
                    arrayListOf("supot 1", "supot 2", "supot 3"),
                    "00月00日",
                    "100"
            )
            list.add(timelinePlanData)
        }
        return list
    }

    private fun getAreaListData(): ArrayList<HomeAreaData> {
        return arrayListOf(
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.hokkaidou), "area=北海道"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.touhoku), "area=青森県&area=岩手県&area=秋田県&area=宮城県&area=山形県&area=福島県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.kantou), "area=茨城県&area=栃木県&area=群馬県&area=埼玉県&area=千葉県&area=東京都&area=神奈川県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.chuubu), "area=山梨県&area=長野県&area=新潟県&area=富山県&area=石川県&area=福井県&area=静岡県&area=愛知県&area=岐阜県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.kinki), "area=三重県&area=滋賀県&area=京都府&area=大阪府&area=兵庫県&area=奈良県&area=和歌山県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.chuugoku), "area=鳥取県&area=島根県&area=岡山県&area=広島県&area=山口県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.shikoku), "area=香川県&area=愛媛県&area=徳島県&area=高知県"),
                HomeAreaData(BitmapFactory.decodeResource(resources, R.drawable.kyuusyuu), "area=福島県&area=佐賀県&area=長崎県&area=熊本県&area=大分県&area=宮崎県&area=鹿児島県&area=沖縄県")
        )
    }

}
