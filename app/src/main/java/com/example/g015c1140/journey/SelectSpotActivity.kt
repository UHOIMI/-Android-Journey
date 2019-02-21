package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_select_spot.*
import java.lang.Double.parseDouble
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SelectSpotActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm

    //Realmにあるリスト用
    private var realmSavedSpotList = arrayListOf<SpotData>()

    //インテントするリスト用
    private var newIntentSpotList = arrayListOf<SpotData>()

    @SuppressLint("SimpleDateFormat")
    private val DF = SimpleDateFormat("yyyy/MM/dd hh:MM")

    //spotListから生成する個人が追加した表示用のスポット名用 Realm使用
    private val realmSpotNameList = ArrayList<String>()
    private val realmSpotList = ArrayList<ListSpot>()
    //登録した用
    private lateinit var userSpotListView: ListView

    //選択したリスト項目の名前を表示するリスト用
    private val newSpotNameList = ArrayList<String>()
    private val newSpotList = ArrayList<ListSpot>()
    private var realmSpotId: Long = -1
    private val newSpotRealmPositionList = arrayListOf<Long>()
    //選択した用
    private lateinit var selectSpotListView: ListView

    private lateinit var userSpotAdapter: SpotListAdapter
    private lateinit var selectSpotAdapter: ArrayAdapter<String>

    //インテント用スポットカウント
    private var spotCnt = 0

    private var nowSort = "昇順"

    private val RESULT_SUBACTIVITY = 1000

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_spot)

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        val realmList = mRealm.where(TestRea::class.java).findAll()

        for (_sd in realmList) {
            realmSpotId++
            realmSavedSpotList.add(SpotData(_sd.id, _sd.name, _sd.latitude, _sd.longitude, _sd.comment, _sd.image_A, _sd.image_B, _sd.image_C, _sd.datetime))
            realmSpotNameList.add(_sd.name + "\n" + DF.format(_sd.datetime))
            val ls = ListSpot()
            ls.id = realmSpotId
            ls.name = _sd.name
            ls.datetime = DF.format(_sd.datetime)
            realmSpotList.add(ls)
        }

        userSpotListView = findViewById(R.id.userSpotList)
        userSpotAdapter = SpotListAdapter(this)
        userSpotAdapter.setSpotList(realmSpotList)
        userSpotListView.adapter = userSpotAdapter

        spotCnt = intent.getIntExtra("SPOTCNT", 100)

        val spinner = findViewById<Spinner>(R.id.sort)

        selectSpotAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, newSpotNameList)
        selectSpotListView = findViewById(R.id.selectSpotList)
        selectSpotListView.adapter = selectSpotAdapter

        userSpotListView.setOnItemClickListener { _, view, position, _ ->

            if (view.isEnabled) {
                if (spotCnt < 20) {
                    val msg = (position + 1).toString() + "番目のアイテムが追加されました"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    newSpotList.add(userSpotAdapter.getItem(position) as ListSpot)
                    selectSpotAdapter.add((spotCnt + 1).toString() + "．" + userSpotAdapter.getName(position))
                    newSpotRealmPositionList.add((userSpotAdapter.getItem(position) as ListSpot).id)
                    userSpotAdapter.setGray(position, true)
                    view.setBackgroundColor(Color.GRAY)
                    view.isEnabled = false
                    newIntentSpotList.add(realmSavedSpotList[position])
                    selectSpotListView.deferNotifyDataSetChanged()
                    userSpotAdapter.notifyDataSetChanged()
                    spotCnt++
                } else {
                    Toast.makeText(this, "スポットは最大20件までです", Toast.LENGTH_SHORT).show()
                }
            }
        }

        selectSpotListView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this).apply {
                setTitle("スポット削除")
                setMessage("スポット:${newSpotNameList[position]} を削除しますか？")
                setPositiveButton("削除") { _, _ ->
                    // 削除をタップしたときの処理
                    if (newSpotRealmPositionList[position] != -10L) {
                        loop@for(_rlc in 0 until realmSpotList.size){
                            if (realmSpotList[_rlc].id == newSpotRealmPositionList[position]) {
                                userSpotAdapter.setGray(_rlc, false)
                                userSpotAdapter.notifyDataSetChanged()
                                break@loop
                            }
                        }
                    }
                    newSpotRealmPositionList.removeAt(position)

                    newSpotList.removeAt(position)
                    newSpotNameList.removeAt(position)
                    newIntentSpotList.removeAt(position)
                    spotCnt--

                    //削除した項目以下の連番更新
                    for (_cnt in position until newSpotList.size) {
                        newSpotNameList[_cnt] = "${1 + _cnt}．${newSpotList[_cnt].name}"
                    }
                    selectSpotAdapter.notifyDataSetChanged()
                }
                setNegativeButton("戻る", null)
                show()
            }
            return@setOnItemLongClickListener true
        }


        // リスナーを登録
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                val sortSpinner = parent as Spinner
                val item = sortSpinner.selectedItem as String
                if (nowSort != item) {
                    sortList()
                    nowSort = item
                }
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }

        val searchButton = findViewById<Button>(R.id.searchButton)
        searchButton.setOnClickListener {
            if (spotCnt < 20) {
                val intent = Intent(application, PlacePicker::class.java)
                startActivityForResult(intent, RESULT_SUBACTIVITY)
            } else {
                Toast.makeText(this, "スポットは最大20件までです", Toast.LENGTH_SHORT).show()
            }
        }

        //toolbar設定
        setSupportActionBar(toolbar)
        title = "スポット追加"
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(navigation)

        val myApp = this.application as MyApplication
        navigation.selectedItemId = myApp.getBnp()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    //toolbar設置
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_selectspot, menu)
        return true
    }

    //toolbarタップ時
    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        //登録ボタンタップ時
        R.id.saveButton -> {
            Toast.makeText(this, "登録ボタン", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK, Intent().putExtra("SPOTDATA", newIntentSpotList))
            finish()
            true
        }
        //戻るボタンタップ時
        android.R.id.home -> {
            Toast.makeText(this, "もどーるぼたんたっぷど", Toast.LENGTH_SHORT).show()
            finish()
            true
        }
        else -> false
    }

    //ボトムバータップ時
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this,SearchPlanActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this,TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
                finish()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_SUBACTIVITY && null != intent) {

            selectSpotAdapter.add((spotCnt + 1).toString() + "．" + intent.getStringExtra("NAME"))
            newIntentSpotList.add(SpotData("tokenID", intent.getStringExtra("NAME"), parseDouble(intent.getStringExtra("LatLngX")), parseDouble(intent.getStringExtra("LatLngY")), "", "", "", "", Date()))
            newSpotRealmPositionList.add(-10)
            selectSpotAdapter.notifyDataSetChanged()

            val listSpot = ListSpot()
            listSpot.id = -10
            listSpot.name = intent.getStringExtra("NAME")
            listSpot.datetime = DF.format(Date())
            newSpotList.add(listSpot)

            spotCnt++
        }
    }

    fun sortList() {
        realmSavedSpotList.reverse()
        realmSpotNameList.reverse()
        realmSpotList.reverse()
        userSpotAdapter.reverseColor()
        userSpotAdapter.notifyDataSetChanged()
    }
}
