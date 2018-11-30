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
import org.json.JSONObject
import java.text.SimpleDateFormat

class TimelineActivity : AppCompatActivity() {

    // 1ページ辺りの項目数
    private var timelineCnt: Int = 0
    private val TIMELINE_LIST = arrayListOf<TimelinePlan>()
    private lateinit var timellineListAdapter: TimelinePlanListAdapter
    private var scrollFlg = false

    private var addTimelineList = mutableListOf<TimelinePlan>()
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_IN = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_OUT = SimpleDateFormat("MM月dd日")
    private var firstApi = true

    // フッターのプログレスバー（クルクル）
    var progressFooter: View? = null

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

        timellineListAdapter = TimelinePlanListAdapter(this)

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

        timellineListAdapter.setTimelinePlanList(TIMELINE_LIST)
        timelineListView.adapter = timellineListAdapter

        setTimeline(0, true)

        //下のクルクル
        timelineListView.addFooterView(getProgFooter())
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
                //テスト用
                /**************************/
                setTimelineListener()
                /**************************/

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                //テスト用
                /**************************/
                firstApi = true
                setTimeline(0, true)
                /**************************/

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
    private fun setTimeline(ofset: Int, refreshFlg: Boolean) {
        val gtat = GetTimelineAsyncTask(ofset)
        gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?, resultPlanIdList: ArrayList<String>?, resultPlanUserIdList: ArrayList<String>?, resultPlanSpotIdList: ArrayList<ArrayList<String>>?, resultPlanSpotCntList: ArrayList<Int>?) {
                if (result == "RESULT-OK") {
                    val guaat = GetUserAccountAsyncTask(resultPlanUserIdList!!)
                    guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                        override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                            super.callback(resultUserAccountList)
                            if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                                resultUserAccountList.removeAt(resultUserAccountList.size - 1)
                                //完了
                                /****************/
                                val gsat = GetSpotAsyncTask(0, resultPlanSpotIdList!!, true)
                                gsat.setOnCallback(object : GetSpotAsyncTask.CallbackGetSpotAsyncTask() {
                                    override fun callback(resultSpotJsonList: ArrayList<ArrayList<JSONObject>>?, resultArrayList: ArrayList<String>?, resultIdFlg: Boolean) {
                                        super.callback(resultSpotJsonList, resultArrayList, resultIdFlg)
                                        if (resultSpotJsonList!![resultSpotJsonList.size - 1][0].getString("result") == "RESULT-OK" && resultIdFlg) {
                                            resultSpotJsonList.removeAt(resultSpotJsonList.size - 1)
                                            //完了
                                            /****************/
                                            val gpfat = GetPlanFavoriteAsyncTask(resultPlanIdList!!)
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
                                                        /****************/
                                                        val giat = GetImageAsyncTask()
                                                        giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                                                            override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                                                if (resultBmpString == "RESULT-OK") {
                                                                    /****************/

                                                                    var timelinePlan: TimelinePlan
                                                                    for (_timalineCnt in 0 until timelineRecordJsonArray!!.length()) {
                                                                        timelinePlan = TimelinePlan()
                                                                        timelinePlan.planId = resultPlanIdList[_timalineCnt].toLong()
                                                                        if (resultBmpList!![_timalineCnt][0] != null) {
                                                                            timelinePlan.planUserIconImage = resultBmpList[_timalineCnt][0]
                                                                        } else {
                                                                            timelinePlan.planUserIconImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                                                        }
                                                                        timelinePlan.planUserName = resultUserAccountList[_timalineCnt].getString("user_name")
                                                                        timelinePlan.planTitle = timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("plan_title")
                                                                        if (resultBmpList[_timalineCnt][1] != null) {
                                                                            timelinePlan.planSpotImage = resultBmpList[_timalineCnt][1]
                                                                        } else {
                                                                            timelinePlan.planSpotImage = null
                                                                        }
                                                                        timelinePlan.planSpotTitleList.add(resultSpotJsonList[_timalineCnt][0].getString("spot_title"))
                                                                        if (resultPlanSpotCntList!![_timalineCnt] >= 2) {
                                                                            timelinePlan.planSpotTitleList.add(resultSpotJsonList[_timalineCnt][1].getString("spot_title"))
                                                                            if (resultPlanSpotCntList[_timalineCnt] >= 3) {
                                                                                timelinePlan.planSpotTitleList.add("他 ${resultPlanSpotCntList[_timalineCnt] - 2}件")
                                                                            } else {
                                                                                timelinePlan.planSpotTitleList.add("")
                                                                            }
                                                                        } else {
                                                                            timelinePlan.planSpotTitleList.add("")
                                                                            timelinePlan.planSpotTitleList.add("")
                                                                        }
                                                                        val dateIndex = timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("date").indexOf("T")
                                                                        timelinePlan.planTime = DATE_FORMAT_OUT.format(DATE_FORMAT_IN.parse(timelineRecordJsonArray.getJSONObject(_timalineCnt).getString("date").substring(0, dateIndex)))
                                                                        timelinePlan.planFavorite = resultFavoriteArrayList[_timalineCnt]
                                                                        addTimelineList.add(timelinePlan)
                                                                    }
                                                                    /****************/

                                                                    if (refreshFlg) {
                                                                        if (!firstApi) {
                                                                            refreshLoop@ for (_atlCnt in 0 until addTimelineList.size) {
                                                                                if (TIMELINE_LIST[0].planId == addTimelineList[_atlCnt].planId) {
                                                                                    if (_atlCnt == 0){
                                                                                        addTimelineList.clear()
                                                                                        break@refreshLoop
                                                                                    }
                                                                                    addTimelineList = addTimelineList.subList(0, _atlCnt)
                                                                                    break@refreshLoop
                                                                                }
                                                                            }
                                                                        }
                                                                        TIMELINE_LIST.addAll(0, addTimelineList)
                                                                    }else {
                                                                        TIMELINE_LIST.addAll(addTimelineList)
                                                                    }

                                                                    addTimelineList.clear()
                                                                    timellineListAdapter.notifyDataSetChanged()
                                                                    timelineCnt += timelineRecordJsonArray.length()

                                                                    if(timelineSwipeRefresh.isRefreshing)
                                                                        timelineSwipeRefresh.isRefreshing = false

                                                                    if (firstApi) {
                                                                        /**************************/
//                                                                        setTimelineListener()
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
                                            /****************/
                                        } else {
                                            failedAsyncTask()
                                            return
                                        }
                                    }
                                })
                                gsat.execute()
                                /****************/
                            } else {
                                failedAsyncTask()
                                return
                            }
                        }
                    })
                    guaat.execute()
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

/*
                // 最初とスクロール完了したとき
                if (totalItemCount - visibleItemCount == firstVisibleItem) {

                    // アイテムの数 フッター分の1を引く
                    val ItemCount = totalItemCount - 1

                    // アダプターにアイテムを追加します
//                    setTimeline(timelineCnt, false)
                }
*/
            }

            // ListViewがスクロール中かどうか状態を返すメソッドです
            override fun onScrollStateChanged(list: AbsListView, state: Int) {
                Log.d("xxx", String.format("onScrollStateChanged scrollState=%d firstVisiblePos=%d", state, list.firstVisiblePosition))
                if (scrollFlg && state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // do something

                    Log.d("xxx", "あああああああああああああああああああああああああああああああああああああ")
                    setTimeline(timelineCnt, false)

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