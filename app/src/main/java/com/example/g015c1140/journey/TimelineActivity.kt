package com.example.g015c1140.journey

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat

class TimelineActivity : AppCompatActivity() {

    /*********/
    //searchから遷移したか確認する用
    private var searchFlg = false

    //searchから遷移したか確認する用
    private var areaFlg = false
    //地方などにつかう用
    private var areaApiString = ""
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        /*******************/
        searchFlg = intent.getBooleanExtra("SEARCH_FLG", false)

        areaFlg = intent.getBooleanExtra("AREA_FLG", false)

        title = if (searchFlg) {
            //タイトル名セット
            "検索結果"
        } else if (areaFlg){
            areaApiString = intent.getStringExtra("AREA_STRING")
            intent.getStringExtra("AREA_NAME")
        } else{
            "タイムライン"
        }
        /*******************/

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

        timelineListAdapter = TimelinePlanListAdapter(this, this)

        //引っ張って更新用
        timelineSwipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        timelineSwipeRefresh.setOnRefreshListener {
            // 引っ張って離した時に呼ばれます。
            setTimeline(0, true)
            try {
            } catch (e: InterruptedException) {
                Toast.makeText(this, "erorr", Toast.LENGTH_SHORT).show()
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

                val myApp = this.application as MyApplication
                myApp.setBmp_1((TIMELINE_LIST[position].planUserIconImage!!))

                startActivity(
                        Intent(this, DetailPlanActivity::class.java)
                                .putStringArrayListExtra("PLAN-ID_USER-ID_USER-NAME", arrayListOf(TIMELINE_LIST[position].planId.toString(), TIMELINE_LIST[position].userId, TIMELINE_LIST[position].planUserName))
                )
            }
        }
    }

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

    //refreshFlgが true:新しく投稿されたプラン取得 　false:昔のプラン取得
/*
    private fun setTimeline(ofset: Int, refreshFlg: Boolean) {
        val gtat = GetTimelineAsyncTask(ofset)
        gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?, resultPlanIdList: ArrayList<String>?, resultPlanUserIdList: ArrayList<String>?, resultPlanSpotIdList: ArrayList<ArrayList<String>>?, resultPlanSpotCntList: ArrayList<Int>?) {
                if (result == "RESULT-OK") {

                    if (refreshFlg) {
                        if (!firstApi) {
                            refreshLoop@ for (_jsonCnt in 0 until timelineRecordJsonArray!!.length()) {
                                if (TIMELINE_LIST[0].planId == timelineRecordJsonArray.getJSONObject(_jsonCnt).getLong("plan_id")) {
                                    //削除
                                    for (_removeCnt in _jsonCnt until timelineRecordJsonArray.length()) {
                                        timelineRecordJsonArray.remove(_jsonCnt)
                                        resultPlanIdList!!.removeAt(_jsonCnt)
                                        resultPlanUserIdList!!.removeAt(_jsonCnt)
                                        resultPlanSpotIdList!!.removeAt(_jsonCnt)
                                        resultPlanSpotCntList!!.removeAt(_jsonCnt)
                                    }
                                    break@refreshLoop
                                }
                            }
                        }
                    }

                    if (resultPlanIdList!!.isNotEmpty()) {
                        val guaat = GetUserAccountAsyncTask(resultPlanUserIdList!!)
                        guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                            override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                                super.callback(resultUserAccountList)
                                if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                                    resultUserAccountList.removeAt(resultUserAccountList.size - 1)
                                    //完了
                                    val gsat = GetSpotAsyncTask(0, resultPlanSpotIdList!!, true)
                                    gsat.setOnCallback(object : GetSpotAsyncTask.CallbackGetSpotAsyncTask() {
                                        override fun callback(resultSpotJsonList: ArrayList<ArrayList<JSONObject>>?, resultArrayList: ArrayList<String>?, resultIdFlg: Boolean) {
                                            super.callback(resultSpotJsonList, resultArrayList, resultIdFlg)
                                            if (resultSpotJsonList!![resultSpotJsonList.size - 1][0].getString("result") == "RESULT-OK" && resultIdFlg) {
                                                resultSpotJsonList.removeAt(resultSpotJsonList.size - 1)
                                                //完了
                                                val gpfat = GetPlanFavoriteAsyncTask(resultPlanIdList)
                                                gpfat.setOnCallback(object : GetPlanFavoriteAsyncTask.CallbackGetPlanFavoriteAsyncTask() {
                                                    override fun callback(resultFavoriteArrayList: ArrayList<String>) {
                                                        super.callback(resultFavoriteArrayList)
                                                        if (resultFavoriteArrayList[resultFavoriteArrayList.size - 1] == "RESULT-OK") {
                                                            resultFavoriteArrayList.removeAt(resultFavoriteArrayList.size - 1)
                                                            //完了
                                                            val bmpList = arrayListOf<ArrayList<String>>()
                                                            var bmpValueList: ArrayList<String>
                                                            for (_iconCnt in 0 until resultUserAccountList.size) {
                                                                bmpValueList = arrayListOf()
                                                                bmpValueList.add(resultUserAccountList[_iconCnt].getString("user_icon"))
                                                                loop@ for (_spotCnt in 0 until resultSpotJsonList[_iconCnt].size) {
                                                                    when {
                                                                        resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_a") != "" -> {
                                                                            bmpValueList.add(resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_a"))
                                                                            break@loop
                                                                        }
                                                                        resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_b") != "" -> {
                                                                            bmpValueList.add(resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_b"))
                                                                            break@loop
                                                                        }
                                                                        resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_c") != "" -> {
                                                                            bmpValueList.add(resultSpotJsonList[_iconCnt][_spotCnt].getString("spot_image_c"))
                                                                            break@loop
                                                                        }
                                                                    }
                                                                    if (resultSpotJsonList[_iconCnt].size - 1 == _spotCnt) {
                                                                        bmpValueList.add("")
                                                                    }
                                                                }
                                                                bmpList.add(bmpValueList)
                                                            }
                                                            val giat = GetImageAsyncTask()
                                                            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                                                                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                                                    if (resultBmpString == "RESULT-OK") {
                                                                        var timelinePlanData: TimelinePlanData
                                                                        for (_timalineCnt in 0 until timelineRecordJsonArray!!.length()) {
                                                                            timelinePlanData = TimelinePlanData()
                                                                            timelinePlanData.planId = resultPlanIdList[_timalineCnt].toLong()
                                                                            if (resultBmpList!![_timalineCnt][0] != null) {
                                                                                timelinePlanData.planUserIconImage = resultBmpList[_timalineCnt][0]
                                                                            } else {
                                                                                timelinePlanData.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                                                            }
                                                                            timelinePlanData.planUserName = resultUserAccountList[_timalineCnt].getString("user_name")
                                                                            timelinePlanData.planTitle = timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("plan_title")
                                                                            if (resultBmpList[_timalineCnt][1] != null) {
                                                                                timelinePlanData.planSpotImage = resultBmpList[_timalineCnt][1]
                                                                            } else {
                                                                                timelinePlanData.planSpotImage = null
                                                                            }
                                                                            timelinePlanData.planSpotTitleList.add(resultSpotJsonList[_timalineCnt][0].getString("spot_title"))
                                                                            if (resultPlanSpotCntList!![_timalineCnt] >= 2) {
                                                                                timelinePlanData.planSpotTitleList.add(resultSpotJsonList[_timalineCnt][1].getString("spot_title"))
                                                                                if (resultPlanSpotCntList[_timalineCnt] >= 3) {
                                                                                    timelinePlanData.planSpotTitleList.add("他 ${resultPlanSpotCntList[_timalineCnt] - 2}件")
                                                                                } else {
                                                                                    timelinePlanData.planSpotTitleList.add("")
                                                                                }
                                                                            } else {
                                                                                timelinePlanData.planSpotTitleList.add("")
                                                                                timelinePlanData.planSpotTitleList.add("")
                                                                            }
                                                                            val dateIndex = timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("date").indexOf(" ")
                                                                            timelinePlanData.planTime = DATE_FORMAT_OUT.format(DATE_FORMAT_IN.parse(timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("date").substring(0, dateIndex)))
                                                                            timelinePlanData.planFavorite = resultFavoriteArrayList[_timalineCnt]
                                                                            timelinePlanData.userId = resultPlanUserIdList[_timalineCnt]
                                                                            addTimelineList.add(timelinePlanData)
                                                                        }
                                                                        if (refreshFlg) {
                                                                            TIMELINE_LIST.addAll(0, addTimelineList)
                                                                        } else {
                                                                            TIMELINE_LIST.addAll(addTimelineList)
                                                                        }

                                                                        addTimelineList.clear()
                                                                        timelineListAdapter.notifyDataSetChanged()
                                                                        timelineCnt += timelineRecordJsonArray.length()

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
                                                failedAsyncTask()
                                                return
                                            }
                                        }
                                    })
                                    gsat.execute()
                                } else {
                                    failedAsyncTask()
                                    return
                                }
                            }
                        })
                        guaat.execute()
                    } else {
                        if (timelineSwipeRefresh.isRefreshing)
                            timelineSwipeRefresh.isRefreshing = false
                    }
                } else {
                    Toast.makeText(this@TimelineActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                }
            }
        })
        gtat.execute()
    }
*/


//refreshFlgが true:新しく投稿されたプラン取得 　false:昔のプラン取得
    private fun setTimeline(ofset: Int, refreshFlg: Boolean) {
        val gtat = GetTimelineAsyncTask(areaApiString, ofset)
        gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?) {
                super.callback(result, timelineRecordJsonArray)
                if (result == "RESULT-OK") {

                    if (refreshFlg) {
                        if (!firstApi) {
                            refreshLoop@ for (_jsonCnt in 0 until timelineRecordJsonArray!!.length()) {
                                if (TIMELINE_LIST[0].planId == timelineRecordJsonArray.getJSONObject(_jsonCnt).getLong("plan_id")) {
                                    //削除
                                    for (_removeCnt in _jsonCnt until timelineRecordJsonArray.length()) {
                                        timelineRecordJsonArray.remove(_jsonCnt)
                                    }
                                    break@refreshLoop
                                }
                            }
                        }
                    }

                    if (timelineRecordJsonArray!!.length() != 0) {
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
                        val gpfat = GetPlanFavoriteAsyncTask(planIdList,"")
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
                                                            timelinePlanData.planSpotImage = null
                                                        }
                                                    } else {
                                                        timelinePlanData.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                                        timelinePlanData.planSpotImage = null
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
                                                timelineCnt += timelineRecordJsonArray.length()

                                                if (timelineSwipeRefresh.isRefreshing)
                                                    timelineSwipeRefresh.isRefreshing = false

                                                bottomRefreshFlg = true

                                                if (firstApi) {
                                                    /**************************/
//                                                    setTimelineListener()
                                                    /**************************/
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
                } else {
                    Toast.makeText(this@TimelineActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                }
            }
        })
        gtat.execute()
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
