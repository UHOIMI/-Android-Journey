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
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_home.*
import org.json.JSONArray
import java.net.URLEncoder
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

    private lateinit var metrics: DisplayMetrics
    private var scale: Double = 0.0


    private var myApp: MyApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        metrics = resources.displayMetrics
        scale = (80f * metrics.density).toDouble()


        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        myApp = this.application as MyApplication
        AdjustmentBottomNavigation().disableShiftMode(homeBottomNavigation)

        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        generation = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION, "none")
        homeUserGenerationTextView.text = when (generation) {
            "0" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${generation}代"
        }

        homeUserIconButton.setOnClickListener {
            userIconViewButtonTapped()
        }

        if (sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "").contains("http")) {
            var giat: GetImageAsyncTask? = GetImageAsyncTask()
            giat!!.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                    if (resultBmpString == "RESULT-OK") {

                        val resizeScale = if (resultBmpList!![0][0]!!.width >= resultBmpList[0][0]!!.height) {
                            scale / resultBmpList[0][0]!!.width
                        } else {// 縦長画像の場合
                            scale / resultBmpList[0][0]!!.height
                        }

                        resultBmpList[0][0] = Bitmap.createScaledBitmap(resultBmpList[0][0],
                                (resultBmpList[0][0]!!.width * resizeScale).toInt(),
                                (resultBmpList[0][0]!!.height * resizeScale).toInt(),
                                true)
                        homeUserIconButton.setImageBitmap(resultBmpList[0][0])

                    } else {
                        Toast.makeText(this@HomeActivity, "ヘッダー取得失敗", Toast.LENGTH_SHORT).show()
                    }
                    giat = null
                }
            })
            giat!!.execute(arrayListOf(arrayListOf(sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, ""))))
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

        var gtat: GetTimelineAsyncTask? = GetTimelineAsyncTask("", 0)
        gtat!!.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?) {
                super.callback(result, timelineRecordJsonArray)
                when (result) {
                    "RESULT-OK" -> setPlanList(timelineRecordJsonArray!!, 1)
                    "RESULT-404" -> Toast.makeText(this@HomeActivity, "新着3件はありません", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@HomeActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                }
                gtat = null
            }
        })
        gtat!!.execute("3")

        var gsat: GetSearchAsyncTask? = GetSearchAsyncTask("", generation, "", "", "", 0)
        gsat!!.setOnCallback(object : GetSearchAsyncTask.CallbackGetSearchAsyncTask() {
            override fun callback(result: String, searchRecordJsonArray: JSONArray?) {
                super.callback(result, searchRecordJsonArray)
                when (result) {
                    "RESULT-OK" -> setPlanList(searchRecordJsonArray!!, 2)
                    "RESULT-404" -> Toast.makeText(this@HomeActivity, "${homeUserGenerationTextView.text}の新着はありません", Toast.LENGTH_SHORT).show()
                    else -> Toast.makeText(this@HomeActivity, "search取得失敗", Toast.LENGTH_SHORT).show()
                }
                gsat = null
            }
        })
        gsat!!.execute("3")
    }

    override fun onResume() {
        super.onResume()
        homeBottomNavigation.setOnNavigationItemSelectedListener(null)
        myApp!!.setBnp(R.id.navigation_home)
        homeBottomNavigation.selectedItemId = R.id.navigation_home
        homeBottomNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)
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
        var gpfat: GetPlanFavoriteAsyncTask? = GetPlanFavoriteAsyncTask(planIdList, "")
        gpfat!!.setOnCallback(object : GetPlanFavoriteAsyncTask.CallbackGetPlanFavoriteAsyncTask() {
            override fun callback(resultFavoriteArrayList: ArrayList<String>) {
                super.callback(resultFavoriteArrayList)
                if (resultFavoriteArrayList[resultFavoriteArrayList.size - 1] == "RESULT-OK") {
                    resultFavoriteArrayList.removeAt(resultFavoriteArrayList.size - 1)
                    //完了

                    /****************/
                    //画像
                    var giat: GetImageAsyncTask? = GetImageAsyncTask()
                    giat!!.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
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

                                            val resizeScale = if (resultBmpList[_timelineCnt][0]!!.width >= resultBmpList[_timelineCnt][0]!!.height) {
                                                scale / resultBmpList[_timelineCnt][0]!!.width
                                            } else {// 縦長画像の場合
                                                scale / resultBmpList[_timelineCnt][0]!!.height
                                            }

                                            resultBmpList[_timelineCnt][0] = Bitmap.createScaledBitmap(resultBmpList[_timelineCnt][0],
                                                    (resultBmpList[_timelineCnt][0]!!.width * resizeScale).toInt(),
                                                    (resultBmpList[_timelineCnt][0]!!.height * resizeScale).toInt(),
                                                    true)
                                            timelinePlanData.planUserIconImage = resultBmpList[_timelineCnt][0]

                                        } else {
                                            timelinePlanData.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                        }
                                        if (resultBmpList[_timelineCnt][1] != null) {

                                            val resizeScale = if (resultBmpList[_timelineCnt][1]!!.width >= resultBmpList[_timelineCnt][1]!!.height) {
                                                scale / resultBmpList[_timelineCnt][1]!!.width
                                            } else {// 縦長画像の場合
                                                scale / resultBmpList[_timelineCnt][1]!!.height
                                            }

                                            resultBmpList[_timelineCnt][1] = Bitmap.createScaledBitmap(resultBmpList[_timelineCnt][1],
                                                    (resultBmpList[_timelineCnt][1]!!.width * resizeScale).toInt(),
                                                    (resultBmpList[_timelineCnt][1]!!.height * resizeScale).toInt(),
                                                    true)

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
                            giat = null
                        }
                    })
                    giat!!.execute(bmpList)
                } else {
                    failedAsyncTask()
                    return
                }
                gpfat = null
            }
        })
        gpfat!!.execute()
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("タイムライン取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }

    private fun userIconViewButtonTapped() {
        startActivity(Intent(this, DetailUserActivity::class.java))
    }

    fun planPostButtonTapped(view: View) {
        startActivity(Intent(this, PostActivity::class.java))
    }

    fun newPlanButtonTapped(view: View) {
        startActivity(Intent(this, TimelineActivity::class.java))
    }

    fun userGenerationPlanButtonTapped(view: View) {
        startActivity(Intent(this, TimelineActivity::class.java)
                .putExtra("SEARCH_FLG", true)
                .putExtra("SEARCH_VALUE_KEYWORD", "")
                .putExtra("SEARCH_VALUE_GENERATION", generation)
                .putExtra("SEARCH_VALUE_AREA", "")
                .putExtra("SEARCH_VALUE_PRICE", "")
                .putExtra("SEARCH_VALUE_TRANSPORTATION", ""))
    }

    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this, SearchPlanActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this, TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this, DetailUserActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun getAreaListData(): ArrayList<HomeAreaData> {

        val hokkaidou = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.hokkaidou))
        hokkaidou.cornerRadius = 50f
        val touhoku = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.touhoku))
        touhoku.cornerRadius = 50f
        val kantou = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.kantou))
        kantou.cornerRadius = 50f
        val chuubu = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.chuubu))
        chuubu.cornerRadius = 50f
        val kinki = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.kinki))
        kinki.cornerRadius = 50f
        val chuugoku = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.chuugoku))
        chuugoku.cornerRadius = 50f
        val shikoku = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.shikoku))
        shikoku.cornerRadius = 50f
        val kyuusyuu = RoundedBitmapDrawableFactory.create(resources, BitmapFactory.decodeResource(resources, R.drawable.kyuusyuu))
        kyuusyuu.cornerRadius = 50f

        return arrayListOf(
                HomeAreaData("北海道地方のプラン", hokkaidou, "area=${URLEncoder.encode("北海道", "UTF-8")}"),
                HomeAreaData("東北地方のプラン", touhoku, "area=${URLEncoder.encode("青森県", "UTF-8")}&area=${URLEncoder.encode("岩手県", "UTF-8")}&area=${URLEncoder.encode("秋田県", "UTF-8")}&area=${URLEncoder.encode("宮城県", "UTF-8")}&area=${URLEncoder.encode("山形県", "UTF-8")}&area=${URLEncoder.encode("福島県", "UTF-8")}"),
                HomeAreaData("関東地方のプラン", kantou, "area=${URLEncoder.encode("茨城県", "UTF-8")}&area=${URLEncoder.encode("栃木県", "UTF-8")}&area=${URLEncoder.encode("群馬県", "UTF-8")}&area=${URLEncoder.encode("埼玉県", "UTF-8")}&area=${URLEncoder.encode("千葉県", "UTF-8")}&area=${URLEncoder.encode("東京都", "UTF-8")}&area=${URLEncoder.encode("神奈川県", "UTF-8")}"),
                HomeAreaData("中部地方のプラン", chuubu, "area=${URLEncoder.encode("山梨県", "UTF-8")}&area=${URLEncoder.encode("長野県", "UTF-8")}&area=${URLEncoder.encode("新潟県", "UTF-8")}&area=${URLEncoder.encode("富山県", "UTF-8")}&area=${URLEncoder.encode("石川県", "UTF-8")}&area=${URLEncoder.encode("福井県", "UTF-8")}&area=${URLEncoder.encode("静岡県", "UTF-8")}&area=${URLEncoder.encode("愛知県", "UTF-8")}&area=${URLEncoder.encode("岐阜県", "UTF-8")}"),
                HomeAreaData("近畿地方のプラン", kinki, "area=${URLEncoder.encode("三重県", "UTF-8")}&area=${URLEncoder.encode("滋賀県", "UTF-8")}&area=${URLEncoder.encode("京都府", "UTF-8")}&area=${URLEncoder.encode("大阪府", "UTF-8")}&area=${URLEncoder.encode("兵庫県", "UTF-8")}&area=${URLEncoder.encode("奈良県", "UTF-8")}&area=${URLEncoder.encode("和歌山県", "UTF-8")}"),
                HomeAreaData("中国地方のプラン", chuugoku, "area=${URLEncoder.encode("鳥取県", "UTF-8")}&area=${URLEncoder.encode("島根県", "UTF-8")}&area=${URLEncoder.encode("岡山県", "UTF-8")}&area=${URLEncoder.encode("広島県", "UTF-8")}&area=${URLEncoder.encode("山口県", "UTF-8")}"),
                HomeAreaData("四国地方のプラン", shikoku, "area=${URLEncoder.encode("香川県", "UTF-8")}&area=${URLEncoder.encode("愛媛県", "UTF-8")}&area=${URLEncoder.encode("徳島県", "UTF-8")}&area=${URLEncoder.encode("高知県", "UTF-8")}"),
                HomeAreaData("九州地方のプラン", kyuusyuu, "area=${URLEncoder.encode("福島県", "UTF-8")}&area=${URLEncoder.encode("佐賀県", "UTF-8")}&area=${URLEncoder.encode("長崎県", "UTF-8")}&area=${URLEncoder.encode("熊本県", "UTF-8")}&area=${URLEncoder.encode("大分県", "UTF-8")}&area=${URLEncoder.encode("宮崎県", "UTF-8")}&area=${URLEncoder.encode("鹿児島県", "UTF-8")}&area=${URLEncoder.encode("沖縄県", "UTF-8")}")
        )
    }
}
