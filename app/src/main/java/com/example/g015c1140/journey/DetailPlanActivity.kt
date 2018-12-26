package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail_plan.*
import org.json.JSONObject


class DetailPlanActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var gMap : GoogleMap
    private var favoriteFlg = false
    private val SPOT_LIST = arrayListOf<DetailPlanSpotData>()
    private val SPOT_ADDRESS = arrayListOf<ArrayList<Double>>()
    private lateinit var detailPlanSpotListAdapter: DetailPlanSpotListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_plan)

        //ツールバーセット
        title = "プラン詳細"
        val toolbar = detailPlanToolbar
        setSupportActionBar(toolbar)

        //戻るボタンセット
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomNavigation: BottomNavigationView = findViewById(R.id.detailPlanNavigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        //map準備
        val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapAsync(this)

        //intent
        val intentString = intent.getStringArrayListExtra("PLAN_ID__USER_NAME")
        val planId = intentString[0]

        detailPlanUserNameTextView.text = intentString[1]
        val myApp: MyApplication = this.application as MyApplication
        detailPlanUserIconCircleView.setImageBitmap(myApp.getBmp_1())
        myApp.clearBmp_1()

        /************************************/
        val fab = findViewById<FloatingActionButton>(R.id.detailPlanFab)
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        //favorite
        val gpfat = GetPlanFavoriteAsyncTask(arrayListOf(planId),sharedPreferences.getString(Setting().USER_SHARED_PREF_ID,"none"))
        gpfat.setOnCallback(object : GetPlanFavoriteAsyncTask.CallbackGetPlanFavoriteAsyncTask() {
            override fun callback(resultFavoriteArrayList: ArrayList<String>) {
                super.callback(resultFavoriteArrayList)
                if (resultFavoriteArrayList[resultFavoriteArrayList.size - 1] == "RESULT-OK") {
                    resultFavoriteArrayList.removeAt(resultFavoriteArrayList.size - 1)
                    //完了
                    if ( resultFavoriteArrayList[0] == "favorite-yes"){
                        favoriteFlg = true
                        detailPlanFab.setImageResource(android.R.drawable.btn_star_big_on)
                    }

                    fab.setOnClickListener {
                        if (favoriteFlg){
                            Toast.makeText(this@DetailPlanActivity, "お気に入り削除", Toast.LENGTH_SHORT).show()
                            val dfat = DeletePlanFavoriteAsyncTask(planId, sharedPreferences.getString(Setting().USER_SHARED_PREF_TOKEN, "none"))
                            dfat.setOnCallback(object : DeletePlanFavoriteAsyncTask.CallbackDeletePlanFavoriteAsyncTask() {
                                override fun callback(resultFavoriteString: String) {
                                    super.callback(resultFavoriteString)
                                    // ここからAsyncTask処理後の処理を記述します。
                                    Log.d("test favoriteCallback", "非同期処理$resultFavoriteString")
                                    if (resultFavoriteString == "RESULT-OK") {
                                        //完了
                                        detailPlanFab.setImageResource(android.R.drawable.btn_star_big_off)
                                        favoriteFlg = false
                                    } else {
                                        AlertDialog.Builder(this@DetailPlanActivity).apply {
                                            setTitle("お気に入り削除に失敗しました")
                                            setMessage("もう一度実行してください")
                                            setPositiveButton("確認", null)
                                            show()
                                        }
                                    }
                                }
                            })
                            dfat.execute()

                        } else {
                            Toast.makeText(this@DetailPlanActivity, "お気に入り登録", Toast.LENGTH_SHORT).show()
                            val pfat = PostFavoriteAsyncTask(planId, sharedPreferences.getString(Setting().USER_SHARED_PREF_TOKEN, "none"))
                            pfat.setOnCallback(object : PostFavoriteAsyncTask.CallbackPostFavoriteAsyncTask() {
                                override fun callback(result: String) {
                                    super.callback(result)
                                    // ここからAsyncTask処理後の処理を記述します。
                                    Log.d("test favoriteCallback", "非同期処理$result")
                                    if (result == "RESULT-OK") {
                                        //完了
                                        detailPlanFab.setImageResource(android.R.drawable.btn_star_big_on)
                                        favoriteFlg = true
                                    } else {
                                        AlertDialog.Builder(this@DetailPlanActivity).apply {
                                            setTitle("お気に入り追加に失敗しました")
                                            setMessage("もう一度実行してください")
                                            setPositiveButton("確認", null)
                                            show()
                                        }
                                    }
                                }
                            })
                            pfat.execute()
                        }
                    }

                } else {
                    failedAsyncTask()
                    return
                }
            }
        })
        gpfat.execute()
        /************************************/

        detailPlanSpotListAdapter = DetailPlanSpotListAdapter(this)
        detailPlanSpotListAdapter.setDetailPlanSpotList(SPOT_LIST)
        detailPlanSpotListView.adapter = detailPlanSpotListAdapter

        //planApi
        val gpat = GetPlanAsyncTask(planId,true)
        gpat.setOnCallback(object : GetPlanAsyncTask.CallbackGetPlanAsyncTask() {
            override fun callback(resultPlanJson: JSONObject) {
                super.callback(resultPlanJson)
                if (resultPlanJson.getString("result") == "RESULT-OK") {
                    //完了
                    /****************/
                    detailPlanTitleTextView.text = resultPlanJson.getString("plan_title")
                    detailPlanPrefecturesTextView.text = resultPlanJson.getString("area")
                    detailPlanPriceTextView.text = resultPlanJson.getString("price").replace("~", "～")
                    detailPlanCommentTextView.text = resultPlanJson.getString("plan_comment")
                    val transportation = resultPlanJson.getString("transportation")

                    if (transportation[0] == '1') {
                        detailPlanWalkImageButton.setImageResource(R.drawable.s_walk_on)
                    }
                    if (transportation[2] == '1') {
                        detailPlanBicycleImageButton.setImageResource(R.drawable.s_bicycle_on)
                    }
                    if (transportation[4] == '1') {
                        detailPlanCarImageButton.setImageResource(R.drawable.s_car_on)
                    }
                    if (transportation[6] == '1') {
                        detailPlanBusImageButton.setImageResource(R.drawable.s_bus_on)
                    }
                    if (transportation[8] == '1') {
                        detailPlanTrainImageButton.setImageResource(R.drawable.s_train_on)
                    }
                    if (transportation[10] == '1') {
                        detailPlanAirplaneImageButton.setImageResource(R.drawable.s_airplane_on)
                    }
                    if (transportation[12] == '1') {
                        detailPlanBoatImageButton.setImageResource(R.drawable.s_boat_on)
                    }

/*                    val spotIdList = arrayListOf<String>()
                    if (resultPlanJson.getString("spot_id_a") != "null") {
                        spotIdList.add(resultPlanJson.getString("spot_id_a"))

                        if (resultPlanJson.getString("spot_id_b") != "null") {
                            spotIdList.add(resultPlanJson.getString("spot_id_b"))

                            if (resultPlanJson.getString("spot_id_c") != "null") {
                                spotIdList.add(resultPlanJson.getString("spot_id_c"))

                                if (resultPlanJson.getString("spot_id_d") != "null") {
                                    spotIdList.add(resultPlanJson.getString("spot_id_d"))

                                    if (resultPlanJson.getString("spot_id_e") != "null") {
                                        spotIdList.add(resultPlanJson.getString("spot_id_e"))

                                        if (resultPlanJson.getString("spot_id_f") != "null") {
                                            spotIdList.add(resultPlanJson.getString("spot_id_f"))

                                            if (resultPlanJson.getString("spot_id_g") != "null") {
                                                spotIdList.add(resultPlanJson.getString("spot_id_g"))

                                                if (resultPlanJson.getString("spot_id_h") != "null") {
                                                    spotIdList.add(resultPlanJson.getString("spot_id_h"))

                                                    if (resultPlanJson.getString("spot_id_i") != "null") {
                                                        spotIdList.add(resultPlanJson.getString("spot_id_i"))

                                                        if (resultPlanJson.getString("spot_id_j") != "null") {
                                                            spotIdList.add(resultPlanJson.getString("spot_id_j"))

                                                            if (resultPlanJson.getString("spot_id_k") != "null") {
                                                                spotIdList.add(resultPlanJson.getString("spot_id_k"))

                                                                if (resultPlanJson.getString("spot_id_l") != "null") {
                                                                    spotIdList.add(resultPlanJson.getString("spot_id_l"))

                                                                    if (resultPlanJson.getString("spot_id_m") != "null") {
                                                                        spotIdList.add(resultPlanJson.getString("spot_id_m"))

                                                                        if (resultPlanJson.getString("spot_id_n") != "null") {
                                                                            spotIdList.add(resultPlanJson.getString("spot_id_n"))

                                                                            if (resultPlanJson.getString("spot_id_o") != "null") {
                                                                                spotIdList.add(resultPlanJson.getString("spot_id_o"))

                                                                                if (resultPlanJson.getString("spot_id_p") != "null") {
                                                                                    spotIdList.add(resultPlanJson.getString("spot_id_p"))

                                                                                    if (resultPlanJson.getString("spot_id_q") != "null") {
                                                                                        spotIdList.add(resultPlanJson.getString("spot_id_q"))

                                                                                        if (resultPlanJson.getString("spot_id_r") != "null") {
                                                                                            spotIdList.add(resultPlanJson.getString("spot_id_r"))

                                                                                            if (resultPlanJson.getString("spot_id_s") != "null") {
                                                                                                spotIdList.add(resultPlanJson.getString("spot_id_s"))

                                                                                                if (resultPlanJson.getString("spot_id_t") != "null") {
                                                                                                    spotIdList.add(resultPlanJson.getString("spot_id_t"))
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }*/

                    val gsat = GetSpotAsyncTask( planId, true)
                    gsat.setOnCallback(object : GetSpotAsyncTask.CallbackGetSpotAsyncTask() {
                        override fun callback(resultSpotJsonList: ArrayList<JSONObject>?, resultIdFlg: Boolean) {
                            super.callback(resultSpotJsonList, resultIdFlg)
                            if (resultSpotJsonList!![resultSpotJsonList.size - 1].getString("result") == "RESULT-OK" && resultIdFlg) {
                                resultSpotJsonList.removeAt(resultSpotJsonList.size - 1)

                                //完了
                                val bmp = arrayListOf<String>()
                                var arJson: JSONObject
                                var address: ArrayList<Double>
                                for (_listCnt in 0 until resultSpotJsonList.size) {
                                    when {
                                        resultSpotJsonList[_listCnt].getString("spot_image_a").contains("http") -> bmp.add(resultSpotJsonList[_listCnt].getString("spot_image_a"))
                                        resultSpotJsonList[_listCnt].getString("spot_image_b").contains("http") -> bmp.add(resultSpotJsonList[_listCnt].getString("spot_image_b"))
                                        resultSpotJsonList[_listCnt].getString("spot_image_c").contains("http") -> bmp.add(resultSpotJsonList[_listCnt].getString("spot_image_c"))
                                        else -> bmp.add("")
                                    }

                                    //ピン用
                                    address = arrayListOf()
                                    arJson = resultSpotJsonList[_listCnt].getJSONObject("spot_address")
                                    address.add(arJson.getString("lat").toDouble())
                                    address.add(arJson.getString("lng").toDouble())
                                    SPOT_ADDRESS.add(address)
                                }

                                /****************/
                                val giat = GetImageAsyncTask()
                                giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                                    override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                        super.callback(resultBmpString, resultBmpList)
                                        if (resultBmpString == "RESULT-OK") {
                                            /****************/
                                            var detailPlanSpotData: DetailPlanSpotData
                                            for (_SpotCnt in 0 until resultSpotJsonList.size) {
                                                detailPlanSpotData = DetailPlanSpotData()
                                                detailPlanSpotData.spotId = resultSpotJsonList[_SpotCnt].getLong("spot_id")
                                                detailPlanSpotData.spotTitle = resultSpotJsonList[_SpotCnt].getString("spot_title")
                                                if (resultBmpList!![0][_SpotCnt] != null) {
                                                    detailPlanSpotData.spotImage = resultBmpList[0][_SpotCnt]
                                                } else {
                                                    detailPlanSpotData.spotImage = BitmapFactory.decodeResource(resources, R.drawable.no_image)
                                                }
                                                detailPlanSpotData.spotComment = resultSpotJsonList[_SpotCnt].getString("spot_comment")
                                                SPOT_LIST.add(detailPlanSpotData)
                                            }
                                            detailPlanSpotListAdapter.notifyDataSetChanged()
                                            mapPinEdit()
                                        } else {
                                            failedAsyncTask()
                                            return
                                        }
                                    }
                                })
                                giat.execute(arrayListOf(bmp))
                                /****************/
                            } else {
                                failedAsyncTask()
                            }
                        }
                    })
                    gsat.execute()
                    /****************/
                } else {
                    failedAsyncTask()
                }
            }
        })
        gpat.execute()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        gMap = googleMap
    }

    private fun mapPinEdit() {

        for (_sCnt in 0 until SPOT_LIST.size) {
            //Pin追加
            gMap.addMarker(MarkerOptions().position(LatLng(SPOT_ADDRESS[_sCnt][0], SPOT_ADDRESS[_sCnt][1])).title(SPOT_LIST[_sCnt].spotTitle)).showInfoWindow()
        }

        //camera移動
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(SPOT_ADDRESS[0][0], SPOT_ADDRESS[0][1]), 12.5f))
    }


    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("プラン取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }


    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
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
                startActivity(Intent(this, DetailUserActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
