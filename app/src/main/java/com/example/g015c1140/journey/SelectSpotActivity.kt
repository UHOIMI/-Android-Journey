package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
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


//import sun.util.locale.provider.LocaleProviderAdapter.getAdapter

class SelectSpotActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    var dataList = ArrayList<String>()
    var spotDataList = ArrayList<TestRea>()

    //インテントで来たリスト用
    private var spotList = arrayListOf<SpotData>()

    //インテントするリスト用
    private var newSpotList = arrayListOf<SpotData>()

    //spotListから生成する個人が追加した表示用のスポット名用
    private val spotNameList = ArrayList<String>()
    private val spotNameList2 = ArrayList<ListSpot>()

    //選択したリスト項目の名前を表示するリスト用
    private val newSpotNameList = ArrayList<String>()
    private val newSpotNameList2 = ArrayList<ListSpot>()

    //登録した用
    private lateinit var userSpotListView: ListView

    //選択した用
    private lateinit var selectSpotListView: ListView

    /****************/// test you
    private lateinit var userSpotAdapter: ArrayAdapter<String>
    private lateinit var userSpotAdapter2: SpotListAdapter
    /******************/

    /****************/
    //インテント用スポットカウント
    private var spotCnt = 0
    /****************/
    var nowSort = "昇順"

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

        var realmList = mRealm.where(TestRea::class.java).findAll()

        val df = SimpleDateFormat("yyyy/MM/dd hh:MM")

        for (_sd in realmList) {
            spotList.add(SpotData(_sd.id, _sd.name, _sd.latitude, _sd.longitude, _sd.comment, _sd.image_A, _sd.image_B, _sd.image_C, _sd.datetime))
            spotNameList.add(_sd.name + "\n" + df.format(_sd.datetime))
            var ls = ListSpot()
            ls.name = _sd.name
            ls.datetime = df.format(_sd.datetime)
            spotNameList2.add(ls)
        }

        userSpotListView = findViewById(R.id.userSpotList) as ListView
        userSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, spotNameList)
        userSpotAdapter2 = SpotListAdapter(this)
        userSpotAdapter2.setSpotList(spotNameList2)
        //userSpotListView.adapter = userSpotAdapter
        userSpotListView.adapter = userSpotAdapter2

        /*userSpotListView.setOnItemClickListener {parent, view, position, id ->

            val intent = Intent(this, DetailSpotActivity::class.java)
            var tappedSpot = SpotData(spotDataList[position]!!.id,spotDataList[position]!!.name,spotDataList[position]!!.latitude,spotDataList[position]!!.longitude,spotDataList[position]!!.comment,spotDataList[position]!!.image_A,spotDataList[position]!!.image_B,spotDataList[position]!!.image_C,spotDataList[position]!!.datetime)
            intent.putExtra("SPOT",tappedSpot)
            startActivity(intent)
        }*/

        /****************/
        spotCnt = intent.getIntExtra("SPOTCNT", 100)
        /****************/

        val spinner = findViewById<Spinner>(R.id.sort)

/*        for(_spotList in spotList) {
            spotNameList.add(df.format(_spotList.dateTime) + "：" + _spotList.title)
        }

        // テストのためuserSpotAdapterなどのvalなどの宣言をクラス変数に移動した
        //val
        userSpotAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spotNameList)
        userSpotListView = findViewById(R.id.userSpotList)
        userSpotListView.adapter = userSpotAdapter*/

        val selectSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newSpotNameList)
        selectSpotListView = findViewById(R.id.selectSpotList)
        selectSpotListView.adapter = selectSpotAdapter

        userSpotListView.setOnItemClickListener { parent, view, position, _ ->

            if (view.isEnabled == true) {
                if (spotCnt < 20) {
                    val msg = (position + 1).toString() + "番目のアイテムが追加されました"
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                    val list = parent as ListView
                    //val item = list.getItemAtPosition(position) as String
                    //val adapter = list.adapter as ArrayAdapter<String>
                    newSpotNameList2.add(userSpotAdapter2.getItem(position) as ListSpot)
                    selectSpotAdapter.add((spotCnt + 1).toString() + "．" + userSpotAdapter2.getName(position))
                    userSpotAdapter2.setGray(position, true)
                    view.setBackgroundColor(Color.GRAY)
                    view.isEnabled = false
                    newSpotList.add(spotList[position])
                    selectSpotListView.adapter = selectSpotAdapter
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
                setPositiveButton("削除", { _, _ ->
                    // 削除をタップしたときの処理
                    //spotListAdapter.remove(spotListAdapter.getItem(position))
                    newSpotNameList2.removeAt(position)
                    newSpotNameList.removeAt(position)
                    //spotList.removeAt(position)
                    //削除した項目以下の連番更新
                    for (_cnt in position until newSpotNameList2.size) {
                        newSpotNameList[_cnt] = "${position + _cnt}．${newSpotNameList2[_cnt].name}"
                    }
                    selectSpotAdapter.notifyDataSetChanged()
                })
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
                val spinner = parent as Spinner
                val item = spinner.selectedItem as String
                if (nowSort != item) {
                    sortList()
                    nowSort = item
                }
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>) {
                //
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

        /************/
        //toolbar設定
        setSupportActionBar(toolbar)
        title = "スポット追加"
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)
        /************/

        /************/
        //ボトムバー設定
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomNavigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        /****************/

    }

    /****************/
    //toolbar設置
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_selectspot, menu)
        return true
    }
    /****************/

    /****************/
    //toolbarタップ時
    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        //登録ボタンタップ時
        R.id.saveButton -> {
            Toast.makeText(this, "登録ボタン", Toast.LENGTH_LONG).show()
            setResult(RESULT_OK, Intent().putExtra("SPOTDATA", newSpotList))
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
    /****************/

    /****************/
    //ボトムバータップ時
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                //spotNameEditText.setText(R.string.title_home)
                spotList.add(SpotData("tokenID", "tmp${spotList.size}", 1.0, 2.0, "", "", "", "", Date()))
                spotNameList.add(spotList[spotList.size - 1].title)
                userSpotAdapter.notifyDataSetChanged()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                //spotNameEditText.setText(R.string.title_search)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                //spotNameEditText.setText(R.string.title_favorite)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                //spotNameEditText.setText(R.string.title_setting)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    /****************/

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_SUBACTIVITY && null != intent) {
            Log.d("エックス", intent.getStringExtra("LatLngX"))
            Log.d("ワイ", intent.getStringExtra("LatLngY"))
            Log.d("名前", intent.getStringExtra("NAME"))
            //val GETPOINT = LatLng(Double.parseDouble(intent.getStringExtra("LatLngX")), Double.parseDouble(intent.getStringExtra("LatLngY")))
            /*for(_newSpotList in newSpotList) {
                newSpotNameList.add(_newSpotList.name)
            }*/
            val selectSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newSpotNameList)
            selectSpotAdapter.add((spotCnt + 1).toString() + "．" + intent.getStringExtra("NAME"))
            newSpotList.add(SpotData("tokenID", intent.getStringExtra("NAME"), parseDouble(intent.getStringExtra("LatLngX")), parseDouble(intent.getStringExtra("LatLngY")), "", "", "", "", Date()))

            selectSpotListView.adapter = selectSpotAdapter

            spotCnt++
        }
    }

    fun sortList() {
        //val tempSpotList = spotList
        //val tempSpotNameList = spotNameList
        spotList.reverse()
        spotNameList.reverse()
        spotNameList2.reverse()
        userSpotAdapter2.reverseColor()
        userSpotAdapter2.notifyDataSetChanged()
        //val userSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, spotNameList)
        //userSpotListView.adapter = userSpotAdapter
        /*spotList.clear()
        for(_tempSpot in tempSpotList) {
            spotList.add(_tempSpot)
        }*/
    }
}
