package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_detail_user.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat

class DetailUserActivity : AppCompatActivity() {

    private var headerFlg = 0
    private var iconFlg = 0
    private var anotherUserFlg = false

    val IMAGE_OK = 100

    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_IN = SimpleDateFormat("yyyy-MM-dd")
    @SuppressLint("SimpleDateFormat")
    private val DATE_FORMAT_OUT = SimpleDateFormat("MM月dd日")

    private var mPlanId = ""
    private var mUserId = ""
    private var mUserName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_user)

        //spot = intent.getSerializableExtra("SPOT") as SpotData

        //ツールバーセット
        title = "ユーザ詳細"

        val toolbar = detailUserToolbar
        setSupportActionBar(toolbar)

        //戻るボタンセット
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomNavigation)
        navigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        detailUserShowAllPlanButton.setOnClickListener {
            //perform your action here
            Toast.makeText(this, "すべて表示タップ", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, TimelineActivity::class.java).putExtra("POSTED_FLG", true).putExtra("USER_ID", mUserId))
        }

        anotherUserFlg = intent.getBooleanExtra("ANOTHER_USER", false)
    }

    override fun onResume() {
        super.onResume()

        detailUserLastPlanLinearLayout.visibility = View.GONE
        detailUserShowAllPlanButton.visibility = View.INVISIBLE
        detailUserLastPlanText.visibility = View.GONE

        if (!anotherUserFlg) {
            val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)

            mUserId = sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, "")
            mUserName = sharedPreferences.getString(Setting().USER_SHARED_PREF_NAME, "")

            setUser(
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_NAME, "名前が存在しません"),
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION, "年代が存在しません"),
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_GENDER, "性別が存在しません"),
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_COMMENT, "コメントが存在しません"),
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_HEADERIMAGE, ""),
                    sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "")
            )

            /*            val headerString =
            if (headerString != "") {
                val giat = GetImageAsyncTask()
                giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                    override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                        if (resultBmpString == "RESULT-OK") {
                            detailUserHeaderImageView.setImageBitmap(resultBmpList!![0][0])
                            headerFlg = IMAGE_OK
                        } else {
                            Toast.makeText(this@DetailUserActivity, "ヘッダー取得失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                giat.execute(arrayListOf(arrayListOf(headerString)))
            }

            val iconString =
            if (iconString != "") {
                val giat = GetImageAsyncTask()
                giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                    override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                        if (resultBmpString == "RESULT-OK") {
                            detailUserIconCircleView.setImageBitmap(resultBmpList!![0][0])
                            iconFlg = IMAGE_OK
                        } else {
                            Toast.makeText(this@DetailUserActivity, "アイコン取得失敗", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                giat.execute(arrayListOf(arrayListOf(iconString)))
            }

            detailUserNameTextView.text =
            detailUserGenderTextView.text =
            val generation =
            detailUserGenerationTextView.text = when (generation) {
                "10" -> "10歳以下"
                "100" -> "100歳以上"
                else -> "$generation 代"
            }
            detailUserCommentTextView.text = */

        } else {
            detailUserSpotListButton.visibility = View.GONE
            //他のユーザー情報
            val guaat = GetUserAccountAsyncTask(arrayListOf(intent.getStringExtra("USER_ID")))
            guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                    super.callback(resultUserAccountList)
                    if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                        resultUserAccountList.removeAt(resultUserAccountList.size - 1)
                        //完了
                        /****************/
                        mUserId = resultUserAccountList[0].getString("user_id")
                        mUserName = resultUserAccountList[0].getString("user_name")

                        setUser(
                                resultUserAccountList[0].getString("user_name"),
                                resultUserAccountList[0].getString("generation"),
                                resultUserAccountList[0].getString("gender"),
                                resultUserAccountList[0].getString("comment"),
                                resultUserAccountList[0].getString("user_header"),
                                resultUserAccountList[0].getString("user_icon")
                        )

                        /****************/
                    } else {

                        AlertDialog.Builder(this@DetailUserActivity).apply {
                            setTitle("ユーザー情報取得に失敗しました")
                            setPositiveButton("確認", null)
                            show()
                        }

                    }
                }
            })
            guaat.execute()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!anotherUserFlg)
            menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.saveButton -> {
            Toast.makeText(this, "編集ボタン", Toast.LENGTH_LONG).show()

            val myApp = this.application as MyApplication
            if (headerFlg == IMAGE_OK) {
                myApp.setBmp_1((detailUserHeaderImageView.drawable as BitmapDrawable).bitmap)
            }
            if (iconFlg == IMAGE_OK) {
                myApp.setBmp_2((detailUserIconCircleView.drawable as BitmapDrawable).bitmap)
            }

            startActivity(Intent(this, EditUserActivity::class.java).putExtra("headerFlg", headerFlg).putExtra("iconFlg", iconFlg))
            true
        }
        //戻るボタンタップ時
        android.R.id.home -> {
            Toast.makeText(this, "もどーるぼたんたっぷど", Toast.LENGTH_SHORT).show()
            finish()
            true
        }
        else -> {
            false
        }
    }

    fun spotListButtonTapped(view: View) {
        startActivity(Intent(this, SpotListActivity::class.java))
    }

    private fun setUser(userName: String, generation: String, gender: String, comment: String, header: String, icon: String) {
        if (header.contains("http")) {
            val giat = GetImageAsyncTask()
            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                    if (resultBmpString == "RESULT-OK") {
                        detailUserHeaderImageView.setImageBitmap(resultBmpList!![0][0])
                        headerFlg = IMAGE_OK
                    } else {
                        Toast.makeText(this@DetailUserActivity, "ヘッダー取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            giat.execute(arrayListOf(arrayListOf(header)))
        }

        if (icon.contains("http")) {
            val giat = GetImageAsyncTask()
            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                    if (resultBmpString == "RESULT-OK") {
                        detailUserIconCircleView.setImageBitmap(resultBmpList!![0][0])
                        iconFlg = IMAGE_OK
                    } else {
                        Toast.makeText(this@DetailUserActivity, "アイコン取得失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            })
            giat.execute(arrayListOf(arrayListOf(icon)))
        }

        detailUserNameTextView.text = userName
        detailUserGenerationTextView.text = when (generation) {
            "0" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "$generation 代"
        }
        detailUserGenderTextView.text = gender
        detailUserCommentTextView.text = comment


        val gtat = GetTimelineAsyncTask("", 0)
        gtat.setOnCallback(object : GetTimelineAsyncTask.CallbackGetTimelineAsyncTask() {
            override fun callback(result: String, timelineRecordJsonArray: JSONArray?) {
                super.callback(result, timelineRecordJsonArray)
                if (result == "RESULT-OK") {

                    //画像取得用
                    val bmpList = arrayListOf<String>()

                    //spotTitle
                    var spotTitleValue: ArrayList<String> = arrayListOf()

//                        for (_jsonCnt in 0 until timelineRecordJsonArray!!.length()) {
                    //favorite用
                    mPlanId = timelineRecordJsonArray!!.getJSONObject(0).getString("plan_id")

                    //画像取得用
                    bmpList.add(timelineRecordJsonArray.getJSONObject(0).getJSONObject("user").getString("user_icon"))
                    val spotJsonList = timelineRecordJsonArray.getJSONObject(0).getJSONArray("spots")

                    loop@ for (_spotCnt in 0 until spotJsonList.length()) {
                        when {
                            spotJsonList.getJSONObject(_spotCnt).getString("spot_image_a") != "" -> {
                                bmpList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_a"))
                                break@loop
                            }
                            spotJsonList.getJSONObject(_spotCnt).getString("spot_image_b") != "" -> {
                                bmpList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_b"))
                                break@loop
                            }
                            spotJsonList.getJSONObject(_spotCnt).getString("spot_image_c") != "" -> {
                                bmpList.add(spotJsonList.getJSONObject(_spotCnt).getString("spot_image_c"))
                                break@loop
                            }
                        }
                        if (spotJsonList.length() - 1 == _spotCnt) {
                            bmpList.add("")
                        }
                    }

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
//                        }

                    //favorite
                    val gpfat = GetPlanFavoriteAsyncTask(arrayListOf(mPlanId), "")
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

//                                            var timelinePlanData = TimelinePlanData()
//                                                for (_timelineCnt in 0 until timelineRecordJsonArray.length()) {
                                            val timelineData = timelineRecordJsonArray.getJSONObject(0)

                                            if (resultBmpList!![0].isNotEmpty()) {
                                                if (resultBmpList[0][0] != null) {
                                                    detailUserLastPlanIconCircleImage.setImageBitmap(resultBmpList[0][0])
                                                } else {
                                                    detailUserLastPlanIconCircleImage.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.no_image))
                                                }
                                                if (resultBmpList[0][1] != null) {
                                                    detailUserLastPlanSpotImageView.setImageBitmap(resultBmpList[0][1])
                                                } else {
                                                    detailUserLastPlanSpotImageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.no_image))

                                                }
                                            } else {
                                                detailUserLastPlanIconCircleImage.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.no_image))
                                                detailUserLastPlanSpotImageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.no_image))
                                            }
                                            detailUserLastPlanNameTextView.text = mUserName
                                            detailUserLastPlanTitleTextView.text = timelineData.getString("plan_title")
                                            detailUserLastPlanSpotName1TextView.text = spotTitleValue[0]
                                            detailUserLastPlanSpotName2TextView.text = spotTitleValue[1]
                                            detailUserLastPlanSpotName3TextView.text = spotTitleValue[2]
                                            val planDate = timelineData.getString("plan_date")
                                            val dateIndex = planDate.indexOf(" ")
                                            detailUserLastPlanTimeTextView.text = DATE_FORMAT_OUT.format(DATE_FORMAT_IN.parse(planDate.substring(0, dateIndex)))
                                            detailUserLastPlanFavoriteTextView.text = resultFavoriteArrayList[0]

                                            detailUserLastPlanLinearLayout.visibility = View.VISIBLE
                                            detailUserShowAllPlanButton.visibility = View.VISIBLE

                                        } else {
                                            failedAsyncTask()
                                            return
                                        }
                                    }
                                })
                                giat.execute(arrayListOf(bmpList))
                            } else {
                                failedAsyncTask()
                                return
                            }
                        }
                    })
                    gpfat.execute()
                } else if (result == "RESULT-404") {
                    detailUserLastPlanText.visibility = View.VISIBLE
                    return
                } else {
                    Toast.makeText(this@DetailUserActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                }
            }
        })
        gtat.execute("1", mUserId)
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("タイムライン取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }

    fun detailUserLastPlanIconTapped(view: View) {
        startActivity(Intent(this, DetailUserActivity::class.java).putExtra("ANOTHER_USER", true).putExtra("USER_ID", mUserId))
    }

    fun detailUserLastPlanSpotTapped(view: View) {
        val myApp = this.application as MyApplication
        myApp.setBmp_1((detailUserLastPlanIconCircleImage.drawable as BitmapDrawable).bitmap)
        startActivity(
                Intent(this, DetailPlanActivity::class.java)
                        .putStringArrayListExtra("PLAN-ID_USER-ID_USER-NAME", arrayListOf(mPlanId, mUserId, mUserName))
        )
    }

    //ボトムバータップ時
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //spotNameTextView.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                //spotNameTextView.setText(R.string.title_search)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                //spotNameTextView.setText(R.string.title_favorite)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                //spotNameTextView.setText(R.string.title_setting)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
