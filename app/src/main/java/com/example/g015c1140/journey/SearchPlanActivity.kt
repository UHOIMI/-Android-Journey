package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_search_plan.*
import org.json.JSONArray
import java.util.*

class SearchPlanActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    private lateinit var realmData: List<SearchKeywordRealmData>
    private lateinit var searchTextValue: ArrayList<String>
    private lateinit var searchTextListAdapter: ArrayAdapter<String>

    //交通手段ボタン用
    private lateinit var transportationImageButton: MutableList<ImageButton>

    //交通手段ボタンフラグ用
    private val TRANSPORTATION_IMAGE_FLG = mutableListOf(0, 0, 0, 0, 0, 0, 0)

    private var myApp:MyApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_plan)

        //タイトル名セット
        title = "検索"

        val toolbar = searchToolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        myApp = this.application as MyApplication
        AdjustmentBottomNavigation().disableShiftMode(searchNavigation)
        searchNavigation.selectedItemId = R.id.navigation_search
        searchNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        // 項目をタップしたときの処理
        searchTextListView.setOnItemClickListener { _, _, position, _ ->
            // 一番上の項目をタップしたら
            searchTextEditText.setText(realmData[position].keyword)
        }

        // 項目を長押ししたときの処理
        searchTextListView.setOnItemLongClickListener { _, _, position, _ ->
            // 長押しで削除
            AlertDialog.Builder(this).apply {
                setTitle("キーワード削除")
                setMessage(" ${realmData[position].keyword} を削除しますか？")
                setPositiveButton("削除") { _, _ ->
                    // 削除をタップしたときの処理
                    mRealm.executeTransaction {
                        val searchKeywordRealmData = mRealm.where(SearchKeywordRealmData::class.java).equalTo("id", realmData[position].id).findAll()
                        searchKeywordRealmData.deleteFromRealm(0)
                    }
                    searchTextValue.removeAt(position)
                    setSearchKeyword()
                }
                setNegativeButton("戻る", null)
                show()
            }
            return@setOnItemLongClickListener true
        }

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        searchTextEditText.onFocusChangeListener = OnFocusChangeListener { _, focus ->
            if (focus) {
                Toast.makeText(applicationContext, "Got the focus", Toast.LENGTH_LONG).show()
                setSearchKeyword()
                searchDetailLinear.visibility = View.INVISIBLE
                searchListLinear.visibility = View.VISIBLE
            } else {
                Toast.makeText(applicationContext, "Lost the focus", Toast.LENGTH_LONG).show()
                searchDetailLinear.visibility = View.VISIBLE
                searchListLinear.visibility = View.INVISIBLE
            }
        }

        //交通手段ボタン設定
        transportationImageButton = mutableListOf(findViewById(R.id.walkImageButton), findViewById(R.id.bicycleImageButton), findViewById(R.id.carImageButton), findViewById(R.id.busImageButton), findViewById(R.id.trainImageButton), findViewById(R.id.airplaneImageButton), findViewById(R.id.boatImageButton))
    }

    override fun onResume() {
        super.onResume()
        myApp!!.setBnp(R.id.navigation_search)
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

    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this,HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this,TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this, DetailUserActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun setSearchKeyword() {
        //値セット
        realmData = mRealm.where(SearchKeywordRealmData::class.java).findAll().reversed()
        searchTextValue = arrayListOf()
        val keywordLintLimit = if (realmData.size > 5) {
            5
        } else {
            realmData.size
        }
        for (_realmCnt in 0 until keywordLintLimit) {
            searchTextValue.add(realmData[_realmCnt].keyword)
        }
        searchTextListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, searchTextValue)
        searchTextListView.adapter = searchTextListAdapter
    }

    fun searchButtonTapped(view: View) {
        searchButton.isClickable = false

        if (searchTextEditText.text.toString().replace(" ", "").replace("　", "") != "") {
            mRealm.executeTransaction {
                val keywordData = mRealm.createObject(SearchKeywordRealmData::class.java, UUID.randomUUID().toString())
                keywordData.keyword = searchTextEditText.text.toString()
                mRealm.copyToRealm(keywordData)
            }
        }

        searchTextEditText.clearFocus()

        if ((searchTextEditText.text.toString().replace(" ", "").replace("　", "") == "") &&
                (moneySpinner.selectedItemPosition == 0) &&
                (generationSpinner.selectedItemPosition == 0) &&
                (TRANSPORTATION_IMAGE_FLG.toString().replace(" ", "").substring(1, 14) == "0,0,0,0,0,0,0") &&
                (prefecturesSpinner.selectedItemPosition == 0)
        ) {
            AlertDialog.Builder(this).apply {
                setTitle("検索条件が指定されていません")
                setMessage("最低1つは条件を指定してください")
                setPositiveButton("確認", null)
                show()
                searchButton.isClickable = true
            }
        } else {

            val generation =
                    if (generationSpinner.selectedItemPosition == 0) {
                        ""
                    } else {
                        when (generationSpinner.selectedItem.toString()) {
                            "10歳以下" -> "10"
                            "100歳以上" -> "100"
                            else -> generationSpinner.selectedItem.toString().replace("代", "")
                        }
                    }

            val area =
                    when (prefecturesSpinner.selectedItemPosition == 0) {
                        true -> ""
                        else -> prefecturesSpinner.selectedItem.toString()
                    }

            val money =
                    when (moneySpinner.selectedItemPosition == 0) {
                        true -> ""
                        else -> moneySpinner.selectedItem.toString()
                    }

            val transportation =
                    when (TRANSPORTATION_IMAGE_FLG.toString().replace(" ", "").substring(1, 14) == "0,0,0,0,0,0,0") {
                        true -> ""
                        else -> TRANSPORTATION_IMAGE_FLG.toString().replace(" ", "").substring(1, 14)
                    }

            val gsat = GetSearchAsyncTask(searchTextEditText.text.toString(), generation, area, money, transportation, 0)
            gsat.setOnCallback(object : GetSearchAsyncTask.CallbackGetSearchAsyncTask() {
                override fun callback(result: String, searchRecordJsonArray: JSONArray?) {
                    super.callback(result, searchRecordJsonArray)

                    when (result) {

                        "RESULT-404" -> {
                            AlertDialog.Builder(this@SearchPlanActivity).apply {
                                setTitle("該当する結果がありません")
                                setPositiveButton("確認", null)
                                show()
                                searchButton.isClickable = true
                            }
                        }

                        "RESULT-OK" -> {
                            searchButton.isClickable = true
                            startActivity(Intent(this@SearchPlanActivity, TimelineActivity::class.java)
                                    .putExtra("SEARCH_FLG", true)
                                    .putExtra("SEARCH_VALUE_KEYWORD", searchTextEditText.text.toString())
                                    .putExtra("SEARCH_VALUE_GENERATION", generation)
                                    .putExtra("SEARCH_VALUE_AREA", area)
                                    .putExtra("SEARCH_VALUE_PRICE", money)
                                    .putExtra("SEARCH_VALUE_TRANSPORTATION", transportation))
                        }

                        else -> {
                            Toast.makeText(this@SearchPlanActivity, "timeline取得失敗", Toast.LENGTH_SHORT).show()
                            searchButton.isClickable = true
                        }
                    }
                }
            })
            gsat.execute()
        }
    }

    @SuppressLint("PrivateResource")
    fun onTransportationButtonTapped(view: View) {

        when (view.id) {
            R.id.walkImageButton -> {
                TRANSPORTATION_IMAGE_FLG[0] = if (TRANSPORTATION_IMAGE_FLG[0] == 1) {
                    transportationImageButton[0].setImageResource(R.drawable.s_walk_off)
                    0
                } else {
                    transportationImageButton[0].setImageResource(R.drawable.s_walk_on)
                    1
                }
            }

            R.id.bicycleImageButton -> {
                TRANSPORTATION_IMAGE_FLG[1] = if (TRANSPORTATION_IMAGE_FLG[1] == 1) {
                    transportationImageButton[1].setImageResource(R.drawable.s_bicycle_off)
                    0
                } else {
                    transportationImageButton[1].setImageResource(R.drawable.s_bicycle_on)
                    1
                }
            }

            R.id.carImageButton -> {
                TRANSPORTATION_IMAGE_FLG[2] = if (TRANSPORTATION_IMAGE_FLG[2] == 1) {
                    transportationImageButton[2].setImageResource(R.drawable.s_car_off)
                    0
                } else {
                    transportationImageButton[2].setImageResource(R.drawable.s_car_on)
                    1
                }

            }

            R.id.busImageButton -> {
                TRANSPORTATION_IMAGE_FLG[3] = if (TRANSPORTATION_IMAGE_FLG[3] == 1) {
                    transportationImageButton[3].setImageResource(R.drawable.s_bus_off)
                    0
                } else {
                    transportationImageButton[3].setImageResource(R.drawable.s_bus_on)
                    1
                }

            }

            R.id.trainImageButton -> {
                TRANSPORTATION_IMAGE_FLG[4] = if (TRANSPORTATION_IMAGE_FLG[4] == 1) {
                    transportationImageButton[4].setImageResource(R.drawable.s_train_off)
                    0
                } else {
                    transportationImageButton[4].setImageResource(R.drawable.s_train_on)
                    1
                }
            }

            R.id.airplaneImageButton -> {
                TRANSPORTATION_IMAGE_FLG[5] = if (TRANSPORTATION_IMAGE_FLG[5] == 1) {
                    transportationImageButton[5].setImageResource(R.drawable.s_airplane_off)
                    0
                } else {
                    transportationImageButton[5].setImageResource(R.drawable.s_airplane_on)
                    1
                }
            }

            R.id.boatImageButton -> {
                TRANSPORTATION_IMAGE_FLG[6] = if (TRANSPORTATION_IMAGE_FLG[6] == 1) {
                    transportationImageButton[6].setImageResource(R.drawable.s_boat_off)
                    0
                } else {
                    transportationImageButton[6].setImageResource(R.drawable.s_boat_on)
                    1
                }

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}
