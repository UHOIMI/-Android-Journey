package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_timeline.*
import layout.TimelinePlanListAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat

class TimelineActivity : AppCompatActivity() {

    /*********/
    //searchかhomeの年代から遷移したか確認する用
    private var searchFlg = false
    private var searchValueKeyword = ""
    private var searchValueGeneration = ""
    private var searchValueArea = ""
    private var searchValuePrice = ""
    private var searchValueTransportation = ""

    //searchから遷移したか確認する用
    private var areaFlg = false
    //地方などにつかう用
    private var areaApiString = ""

    private var favoriteFlg = false

    private var postedFlag = false
    private var postedUserId = ""
    /*********/

    // 1ページ辺りの項目数
    private var timelineCnt: Int = 0
    private val TIMELINE_LIST = arrayListOf<TimelinePlanData>()
    private lateinit var timelineListAdapter: TimelinePlanListAdapter
    //下のクルクル用
    private var scrollFlg = false

    private var addTimelineList = mutableListOf<TimelinePlanData>()
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_IN = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_OUT = SimpleDateFormat("MM月dd日")
    private var firstApi = true

    // フッターのプログレスバー（クルクル）
    private var progressFooter: View? = null
    private var bottomRefreshFlg = true

    private var myApp: MyApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        /*******************/
        searchFlg = intent.getBooleanExtra("SEARCH_FLG", false)

        areaFlg = intent.getBooleanExtra("AREA_FLG", false)

        favoriteFlg = intent.getBooleanExtra("FAVORITE_FLG", false)

        postedFlag = intent.getBooleanExtra("POSTED_FLG", false)

        title = when {
            searchFlg -> {
                searchValueKeyword = intent.getStringExtra("SEARCH_VALUE_KEYWORD")
                searchValueGeneration = intent.getStringExtra("SEARCH_VALUE_GENERATION")
                searchValueArea = intent.getStringExtra("SEARCH_VALUE_AREA")
                searchValuePrice = intent.getStringExtra("SEARCH_VALUE_PRICE")
                searchValueTransportation = intent.getStringExtra("SEARCH_VALUE_TRANSPORTATION")
                "検索結果一覧"
            }
            areaFlg -> {
                areaApiString = intent.getStringExtra("AREA_STRING")
                intent.getStringExtra("AREA_NAME")
            }
            favoriteFlg -> {
                timelineSwipeRefresh.isEnabled = false
                "お気に入り一覧"
            }
            postedFlag -> {
                timelineSwipeRefresh.isEnabled = false
                postedUserId = intent.getStringExtra("USER_ID")
                "過去の投稿一覧"
            }
            else -> "新着プラン一覧"
        }
        /*******************/

        val toolbar = toolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        myApp = this.application as MyApplication
        AdjustmentBottomNavigation().disableShiftMode(navigation)

        timelineListAdapter = TimelinePlanListAdapter(this, this)

        if (!favoriteFlg && !postedFlag) {
            //引っ張って更新用
            timelineSwipeRefresh.setColorSchemeResources(R.color.colorPrimary)
            timelineSwipeRefresh.setOnRefreshListener {
                // 引っ張って離した時に呼ばれます。
                setTimeline(0, true)
                try {
                } catch (e: InterruptedException) {
                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                }
            }
        }

        timelineListAdapter.setTimelinePlanList(TIMELINE_LIST)
        timelineListView.adapter = timelineListAdapter

        setTimeline(0, true)

        //下のクルクル
        timelineListView.addFooterView(getProgFooter())

        timelineListView.setOnItemClickListener { _, _, position, _ ->
            // 項目をタップしたら
            if (!(TIMELINE_LIST.isEmpty() || (TIMELINE_LIST.size == position))) {
                Toast.makeText(this, "list tapped", Toast.LENGTH_SHORT).show()

                myApp!!.setBmp_1((TIMELINE_LIST[position].planUserIconImage!!))

                startActivity(
                        Intent(this, DetailPlanActivity::class.java)
                                .putStringArrayListExtra("PLAN-ID_USER-ID_USER-NAME", arrayListOf(TIMELINE_LIST[position].planId.toString(), TIMELINE_LIST[position].userId, TIMELINE_LIST[position].planUserName))
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        navigation.setOnNavigationItemSelectedListener(null)
        if (favoriteFlg) {
            myApp!!.setBnp(R.id.navigation_favorite)
            navigation.selectedItemId = R.id.navigation_favorite
        } else {
            navigation.selectedItemId = myApp!!.getBnp()
        }
        navigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)
    }

    @SuppressLint("InflateParams")
    private fun getProgFooter(): View? {
        if (progressFooter == null) {
            progressFooter = layoutInflater.inflate(R.layout.listview_footer, null)
        }
        return progressFooter
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
                startActivity(Intent(this, HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this, SearchPlanActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                if (!favoriteFlg)
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

    //refreshFlgが true:新しく投稿されたプラン取得 　false:昔のプラン取得
    private fun setTimeline(offset: Int, refreshFlg: Boolean) {

        if (!searchFlg && !favoriteFlg) {
            val gtat = GetTimelineAsyncTask(areaApiString, offset)
            gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
                override fun callback(result: String, timelineRecordJsonArray: JSONArray?) {
                    super.callback(result, timelineRecordJsonArray)
                    when (result) {
                        "RESULT-OK" -> setTimelineList(timelineRecordJsonArray!!, refreshFlg)
                        "RESULT-404" -> deleteFooterProgress()
                        else -> Toast.makeText(this@TimelineActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            gtat.execute(null, postedUserId)
        } else if (searchFlg) {
            val gsat = GetSearchAsyncTask(searchValueKeyword, searchValueGeneration, searchValueArea, searchValuePrice, searchValueTransportation, offset)
            gsat.setOnCallback(object : GetSearchAsyncTask.CallbackGetSearchAsyncTask() {
                override fun callback(result: String, searchRecordJsonArray: JSONArray?) {
                    super.callback(result, searchRecordJsonArray)
                    when (result) {
                        "RESULT-OK" -> setTimelineList(searchRecordJsonArray!!, refreshFlg)
                        "RESULT-404" -> deleteFooterProgress()
                        else -> Toast.makeText(this@TimelineActivity, "search取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            gsat.execute()
        } else if (favoriteFlg) {
            val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
            val gufat = GetUserFavoriteAsyncTask(sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, ""), offset)
            gufat.setOnCallback(object : GetUserFavoriteAsyncTask.CallbackGetUserFavoriteAsyncTask() {
                override fun callback(result: String, favoriteRecordJSONArray: JSONArray?) {
                    super.callback(result, favoriteRecordJSONArray)
                    if (result != "RESULT-NG") {
                        if (result == "RESULT-OK") {

                            val postUserIdList = arrayListOf<String>()
                            for (_timelineCnt in 0 until favoriteRecordJSONArray!!.length()){
                                postUserIdList.add( favoriteRecordJSONArray.getJSONObject(_timelineCnt).getString("user_id") )
                            }

                            val guaat = GetUserAccountAsyncTask(postUserIdList)
                            guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                                override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                                    super.callback(resultUserAccountList)
                                    // ここからAsyncTask処理後の処理を記述します。
                                    Log.d("test GetUserAccCallback", "非同期処理${resultUserAccountList[resultUserAccountList.size - 1]}")
                                    if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                                        resultUserAccountList.removeAt(resultUserAccountList.size - 1)
                                        var jsonUser: JSONObject

                                        for (_timelineCnt in 0 until favoriteRecordJSONArray.length()){
                                            jsonUser = JSONObject()
                                            jsonUser.put("user_name", resultUserAccountList[_timelineCnt].getString("user_name"))
                                            jsonUser.put("user_icon", resultUserAccountList[_timelineCnt].getString("user_icon"))
                                            favoriteRecordJSONArray.getJSONObject(_timelineCnt).put("user", jsonUser)
                                        }
                                        setTimelineList(favoriteRecordJSONArray, refreshFlg)
                                    } else {
                                        Toast.makeText(this@TimelineActivity, "search取得失敗", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                            guaat.execute()
                        } else {
                            deleteFooterProgress()
                        }
                    } else {
                        Toast.makeText(this@TimelineActivity, "search取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            gufat.execute()
        }
    }

    private fun deleteFooterProgress() {
        timelineListView.removeFooterView(progressFooter)
        timelineListView.setOnScrollListener(null)
        Toast.makeText(this@TimelineActivity, "プランがありません", Toast.LENGTH_SHORT).show()
    }

    private fun setTimelineList(resultRecordJsonArray: JSONArray, refreshFlg: Boolean) {

        if (refreshFlg && !firstApi) {
            refreshLoop@ for (_jsonCnt in 0 until resultRecordJsonArray.length()) {
                if (TIMELINE_LIST[0].planId == resultRecordJsonArray.getJSONObject(_jsonCnt).getLong("plan_id")) {
                    //削除
                    for (_removeCnt in _jsonCnt until resultRecordJsonArray.length()) {
                        resultRecordJsonArray.remove(_jsonCnt)
                    }
                    break@refreshLoop
                }
            }
        }

        if (resultRecordJsonArray.length() != 0) {
            //favorite用
            val planIdList = arrayListOf<String>()

            //画像取得用
            val bmpList = arrayListOf<ArrayList<String>>()
            var bmpValueList: ArrayList<String>

            //spotTitle
            val spotTitleList = arrayListOf<ArrayList<String>>()
            var spotTitleValue: ArrayList<String>

            for (_jsonCnt in 0 until resultRecordJsonArray.length()) {
                //favorite用
                planIdList.add(resultRecordJsonArray.getJSONObject(_jsonCnt).getString("plan_id"))

                //画像取得用
                bmpValueList = arrayListOf()
                bmpValueList.add(resultRecordJsonArray.getJSONObject(_jsonCnt).getJSONObject("user").getString("user_icon"))
                val spotJsonList = resultRecordJsonArray.getJSONObject(_jsonCnt).getJSONArray("spots")

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
                        /************/
                        //タイムラインエリアにはspotTitleがない
                        spotTitleValue.add(spotJsonList.getJSONObject(_spotTitleCnt).getString("spot_title"))
                        /************/

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
                                    for (_timelineCnt in 0 until resultRecordJsonArray.length()) {
                                        val timelineData = resultRecordJsonArray.getJSONObject(_timelineCnt)
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
                                        addTimelineList.add(timelinePlanData)
                                    }
                                    /****************/

                                    if (refreshFlg) {
                                        TIMELINE_LIST.addAll(0, addTimelineList)
                                    } else {
                                        TIMELINE_LIST.addAll(addTimelineList)
                                    }

                                    addTimelineList.clear()
                                    timelineListAdapter.notifyDataSetChanged()
                                    timelineCnt += resultRecordJsonArray.length()

                                    if (timelineSwipeRefresh.isRefreshing)
                                        timelineSwipeRefresh.isRefreshing = false

                                    bottomRefreshFlg = true

                                    if (firstApi) {
                                        setTimelineListener()
                                        firstApi = false
                                    }

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

        } else {
            if (timelineSwipeRefresh.isRefreshing)
                timelineSwipeRefresh.isRefreshing = false
        }

    }

    private fun setTimelineListener() {
        timelineListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            // スクロール中の処理
            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

                Log.d("xxx", String.format("onScroll firstVisibleItem=%d  visibleItemCount=%d  TIMELINE_LIST.size=%d", firstVisibleItem, visibleItemCount, TIMELINE_LIST.size))
                scrollFlg = TIMELINE_LIST.size + 1 == firstVisibleItem + visibleItemCount
            }

            // ListViewがスクロール中かどうか状態を返すメソッドです
            override fun onScrollStateChanged(list: AbsListView, state: Int) {
                Log.d("xxx", String.format("onScrollStateChanged scrollState=%d firstVisiblePos=%d", state, list.firstVisiblePosition))
                if (scrollFlg && state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (bottomRefreshFlg) {
                        bottomRefreshFlg = false
                        Log.d("xxx", "あああああああああああああああああああああああああああああああああああああ")
                        setTimeline(timelineCnt, false)
                    }
                }
            }
        })
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("タイムライン取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }
}
