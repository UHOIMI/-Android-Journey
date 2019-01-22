package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONArray
import java.text.SimpleDateFormat




class HomeActivity : AppCompatActivity() {

    private val LAYOUT_MANAGER = arrayListOf<LinearLayoutManager>()

    private val NEW_PLAN_LIST = arrayListOf<TimelinePlanData>()
    private lateinit var newPlanRecyclerViewAdapter: PlanPageControlRecyclerViewAdapter

    private var generation = ""
    private val GENERATION_PLAN_LIST = arrayListOf<TimelinePlanData>()
    private lateinit var generationPlanRecyclerViewAdapter: PlanPageControlRecyclerViewAdapter


    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_IN = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_OUT = SimpleDateFormat("MM月dd日")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(homeBottomNavigation)
        homeBottomNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        generation = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION, "none")
        homeUserGenerationTextView.text  = when(generation) {
            "0" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${generation}代"
        }

        if (sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "").contains("http")) {
            val giat = GetImageAsyncTask()
            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                    if (resultBmpString == "RESULT-OK") {
                        homeUserIconButton.setImageBitmap(resultBmpList!![0][0])
                    } else {
                        Toast.makeText(this@HomeActivity, "ヘッダー取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            giat.execute(arrayListOf(arrayListOf(sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, ""))))
        }

        homeFab.setOnClickListener {
            startActivity(Intent(this, PutSpotActivity::class.java))
        }

        for (_layoutCnt in 0 until 3) {
            LAYOUT_MANAGER.add(LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false))
        }
        areaRecyclerView.layoutManager = LAYOUT_MANAGER[0]
        newPlanRecyclerView.layoutManager = LAYOUT_MANAGER[1]
        userGenerationPlanRecyclerView.layoutManager = LAYOUT_MANAGER[2]

        val snapHelper = arrayListOf<PagerSnapHelper>()
        for (_layoutCnt in 0 until 2) {
            snapHelper.add(PagerSnapHelper())
        }
        snapHelper[0].attachToRecyclerView(newPlanRecyclerView)
        snapHelper[1].attachToRecyclerView(userGenerationPlanRecyclerView)

        /**************************************/
        val areaListData = getAreaListData()

        val areaRecyclerViewAdapter = AreaPageControlRecyclerViewAdapter(this, this, areaListData)
        newPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this, this, NEW_PLAN_LIST)
        generationPlanRecyclerViewAdapter = PlanPageControlRecyclerViewAdapter(this, this, GENERATION_PLAN_LIST)

        areaRecyclerView.adapter = areaRecyclerViewAdapter
        newPlanRecyclerView.adapter = newPlanRecyclerViewAdapter
        userGenerationPlanRecyclerView.adapter = generationPlanRecyclerViewAdapter

        val gtat = GetTimelineAsyncTask("", 0)
        gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?) {
                super.callback(result, timelineRecordJsonArray)
                when (result) {
                    "RESULT-OK" -> setPlanList(timelineRecordJsonArray!!, 1)
                    "RESULT-404" -> Toast.makeText(this@HomeActivity, "新着3件はありません", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@HomeActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                }
            }
        })
        gtat.execute("3")

        val gsat = GetSearchAsyncTask("", generation, "", "", "",0)
        gsat.setOnCallback(object : GetSearchAsyncTask.CallbackGetSearchAsyncTask() {
            override fun callback(result: String, searchRecordJsonArray: JSONArray?) {
                super.callback(result, searchRecordJsonArray)
                when (result) {
                    "RESULT-OK" -> setPlanList(searchRecordJsonArray!!,2)
                    "RESULT-404" -> Toast.makeText(this@HomeActivity, "${homeUserGenerationTextView.text}の新着はありません", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@HomeActivity, "search取得失敗", Toast.LENGTH_SHORT).show()
                }
            }
        })
        gsat.execute("3")
    }

    private fun setPlanList(timelineRecordJsonArray: JSONArray, listFlg: Int) {

        //favorite用
        val planIdList = arrayListOf<String>()

        //画像取得用
        val bmpList = arrayListOf<ArrayList<String>>()
        var bmpValueList: ArrayList<String>

        //spotTitle
        val spotTitleList = arrayListOf<ArrayList<String>>()
        var spotTitleValue: ArrayList<String>

        for (_jsonCnt in 0 until timelineRecordJsonArray.length()) {
            //favorite用
            planIdList.add(timelineRecordJsonArray.getJSONObject(_jsonCnt).getString("plan_id"))

            //画像取得用
            bmpValueList = arrayListOf()
            bmpValueList.add(timelineRecordJsonArray.getJSONObject(_jsonCnt).getJSONObject("user").getString("user_icon"))
            val spotJsonList = timelineRecordJsonArray.getJSONObject(_jsonCnt).getJSONArray("spots")

            loop@ for (_spotCnt in 0 until spotJsonList.length()) {
                when {
                    spotJsonList.getJSONObject(_spotCnt).getString("spot_image_a") != "" -> {
                        bmpValueList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_a"))
                        break@loop
                    }
                    spotJsonList.getJSONObject(_spotCnt).getString("spot_image_b") != "" -> {
                        bmpValueList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_b"))
                        break@loop
                    }
                    spotJsonList.getJSONObject(_spotCnt).getString("spot_image_c") != "" -> {
                        bmpValueList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_c"))
                        break@loop
                    }
                }
                if (spotJsonList.length() - 1 == _spotCnt) {
                    bmpValueList.add("")
                }
            }
            bmpList.add(bmpValueList)

            spotTitleValue = arrayListOf()
            for (_spotTitleCnt in 0 until spotJsonList.length()) {
                if (spotTitleValue.size < 2) {
                    spotTitleValue.add(spotJsonList.getJSONObject(_spotTitleCnt).getString("spot_title"))
                } else if (spotTitleValue.size == 2) {
                    spotTitleValue.add("他 ${spotJsonList.length() - 2}件")
                    break
                }
            }
            if (spotTitleValue.size != 3) {
                for (_addCnt in spotTitleValue.size..3)
                    spotTitleValue.add("")
            }
            spotTitleList.add(spotTitleValue)
        }

        //favorite
        val gpfat = GetPlanFavoriteAsyncTask(planIdList, "")
        gpfat.setOnCallback(object : GetPlanFavoriteAsyncTask.CallbackGetPlanFavoriteAsyncTask() {
            override fun callback(resultFavoriteArrayList: ArrayList<String>) {
                super.callback(resultFavoriteArrayList)
                if (resultFavoriteArrayList[resultFavoriteArrayList.size - 1] == "RESULT-OK") {
                    resultFavoriteArrayList.removeAt(resultFavoriteArrayList.size - 1)
                    //完了

                    /****************/
                    //画像
                    val giat = GetImageAsyncTask()
                    giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                        override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                            if (resultBmpString == "RESULT-OK") {
                                /****************/

                                var timelinePlanData: TimelinePlanData
                                for (_timelineCnt in 0 until timelineRecordJsonArray.length()) {
                                    val timelineData = timelineRecordJsonArray.getJSONObject(_timelineCnt)
                                    timelinePlanData = TimelinePlanData()

                                    timelinePlanData.planId = timelineData.getLong("plan_id")
                                    if (resultBmpList!![_timelineCnt].isNotEmpty()) {
                                        if (resultBmpList[_timelineCnt][0] != null) {
                                            timelinePlanData.planUserIconImage = resultBmpList[_timelineCnt][0]
                                        } else {
                                            timelinePlanData.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                        }
                                        if (resultBmpList[_timelineCnt][1] != null) {
                                            timelinePlanData.planSpotImage = resultBmpList[_timelineCnt][1]
                                        } else {
                                            timelinePlanData.planSpotImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                        }
                                    } else {
                                        timelinePlanData.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                        timelinePlanData.planSpotImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                    }
                                    timelinePlanData.planUserName = timelineData.getJSONObject("user").getString("user_name")
                                    timelinePlanData.planTitle = timelineData.getString("plan_title")
                                    timelinePlanData.planSpotTitleList.addAll(spotTitleList[_timelineCnt])
                                    val planDate = timelineData.getString("plan_date")
                                    val dateIndex = planDate.indexOf(" ")
                                    timelinePlanData.planTime = DATE_FORMAT_OUT.format(DATE_FORMAT_IN.parse(planDate.substring(0, dateIndex)))
                                    timelinePlanData.planFavorite = resultFavoriteArrayList[_timelineCnt]
                                    timelinePlanData.userId = timelineData.getString("user_id")

                                    /****/
                                    when (listFlg) {
                                        1 -> NEW_PLAN_LIST.add(timelinePlanData)
                                        2 -> GENERATION_PLAN_LIST.add(timelinePlanData)
                                    }
                                    /***/
                                }
                                /****/
                                when (listFlg) {
                                    1 -> {
                                        newPlanRecyclerViewAdapter.notifyDataSetChanged()
                                        newPlanPageControlView.setRecyclerView(newPlanRecyclerView, LAYOUT_MANAGER[1])
                                    }
                                    2 -> {
                                        generationPlanRecyclerViewAdapter.notifyDataSetChanged()
                                        userGenerationPlanPageControlView.setRecyclerView(userGenerationPlanRecyclerView, LAYOUT_MANAGER[2])
                                    }

                                }
                                /***/
                            } else {
                                failedAsyncTask()
                                return
                            }
                        }
                    })
                    giat.execute(bmpList)
                } else {
                    failedAsyncTask()
                    return
                }
            }
        })
        gpfat.execute()
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("タイムライン取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }

    fun userIconButtonTapped(view: View) {
        startActivity(Intent(this, DetailUserActivity::class.java))
    }

    fun planPostButtonTapped(view: View) {
        startActivity(Intent(this, PostActivity::class.java))
    }

    fun newPlanButtonTapped(view: View) {
        startActivity(Intent(this, TimelineActivity::class.java))
    }

    fun userGenerationPlanButtonTapped(view: View){
        startActivity(Intent(this,TimelineActivity::class.java)
                .putExtra("SEARCH_FLG",true)
                .putExtra("SEARCH_VALUE_KEYWORD","")
                .putExtra("SEARCH_VALUE_GENERATION",generation)
                .putExtra("SEARCH_VALUE_AREA","")
                .putExtra("SEARCH_VALUE_PRICE","")
                .putExtra("SEARCH_VALUE_TRANSPORTATION",""))
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

    private fun getAreaListData(): ArrayList<HomeAreaData> {

        val hokkaidou = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.hokkaidou))
        hokkaidou.cornerRadius = 50f
        val touhoku = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.touhoku))
        touhoku.cornerRadius = 50f
        val kantou = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.kantou))
        kantou.cornerRadius = 50f
        val chuubu = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.chuubu))
        chuubu.cornerRadius = 50f
        val kinki = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.kinki))
        kinki.cornerRadius = 50f
        val chuugoku = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.chuugoku))
        chuugoku.cornerRadius = 50f
        val shikoku = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.shikoku))
        shikoku.cornerRadius = 50f
        val kyuusyuu = RoundedBitmapDrawableFactory.create(resources,BitmapFactory.decodeResource(resources, R.drawable.kyuusyuu))
        kyuusyuu.cornerRadius = 50f

        return arrayListOf(
                HomeAreaData("北海道地方のプラン", hokkaidou, "area=北海道"),
                HomeAreaData("東北地方のプラン", touhoku, "area=青森県&area=岩手県&area=秋田県&area=宮城県&area=山形県&area=福島県"),
                HomeAreaData("関東地方のプラン", kantou, "area=茨城県&area=栃木県&area=群馬県&area=埼玉県&area=千葉県&area=東京都&area=神奈川県"),
                HomeAreaData("中部地方のプラン", chuubu, "area=山梨県&area=長野県&area=新潟県&area=富山県&area=石川県&area=福井県&area=静岡県&area=愛知県&area=岐阜県"),
                HomeAreaData("近畿地方のプラン", kinki, "area=三重県&area=滋賀県&area=京都府&area=大阪府&area=兵庫県&area=奈良県&area=和歌山県"),
                HomeAreaData("中国地方のプラン", chuugoku, "area=鳥取県&area=島根県&area=岡山県&area=広島県&area=山口県"),
                HomeAreaData("四国地方のプラン", shikoku, "area=香川県&area=愛媛県&area=徳島県&area=高知県"),
                HomeAreaData("九州地方のプラン", kyuusyuu, "area=福島県&area=佐賀県&area=長崎県&area=熊本県&area=大分県&area=宮崎県&area=鹿児島県&area=沖縄県")
        )
    }
}
