package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_spot_list.*
import java.text.SimpleDateFormat

//import sun.misc.MessageUtils.where


class SpotListActivity : AppCompatActivity() {

    private lateinit var mRealm: Realm
    var dataList = ArrayList<String>()
    var spotDataList = ArrayList<TestRea>()

    //private lateinit var spotDataList : RealmResults<TestRea>
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>

    var nowSort = "昇順"

    companion object {
        private const val DETAIL_SPOT_REQUEST_CODE = 555
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_list)

        //タイトル名セット
        title = "スポット一覧"

        //戻るボタンセット
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(navigation)

        val myApp = this.application as MyApplication
        navigation.selectedItemId = myApp.getBnp()
        navigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        val realmList = mRealm.where(TestRea::class.java).findAll()

        val df = SimpleDateFormat("yyyy/MM/dd hh:MM")

        for (_sd in realmList) {
            spotDataList.add(_sd)
            dataList.add(_sd.name + "\n" + df.format(_sd.datetime))
        }

        listView = findViewById(R.id.spotList) as ListView
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
//            tappedListPosition = position
            val intent = Intent(this, DetailSpotActivity::class.java)
            val tappedSpot = SpotData(
                    spotDataList[position].id,
                    spotDataList[position].name,
                    spotDataList[position].latitude,
                    spotDataList[position].longitude,
                    spotDataList[position].comment,
                    spotDataList[position].image_A,
                    spotDataList[position].image_B,
                    spotDataList[position].image_C,
                    spotDataList[position].datetime)
            intent.putExtra("SPOT", tappedSpot)
            startActivityForResult(intent, DETAIL_SPOT_REQUEST_CODE)
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this).apply {
                setTitle("スポット削除")
                setMessage("スポット:${dataList[position]} を削除しますか？")
                setPositiveButton("削除") { _, _ ->
                    mRealm.executeTransaction {
                        var delSpot = mRealm.where(TestRea::class.java).equalTo("id", spotDataList[position].id).findAll()
                        delSpot.deleteFromRealm(0)
                    }
                    // 削除をタップしたときの処理
                    //spotListAdapter.remove(spotListAdapter.getItem(position))
                    dataList.removeAt(position)
                    spotDataList.removeAt(position)
                    //spotList.removeAt(position)
                    //削除した項目以下の連番更新
                    adapter.notifyDataSetChanged()
                }
                setNegativeButton("戻る", null)
                show()
            }
            return@setOnItemLongClickListener true
        }

        val spinner = findViewById<Spinner>(R.id.sort)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK && requestCode == DETAIL_SPOT_REQUEST_CODE && intent != null) {

/*
            val spot = intent.getSerializableExtra("SPOTDATA") as TestRea

            spotDataList[tappedListPosition!!] = spot
            dataList[tappedListPosition!!] = (spot.name + "\n" + spot.datetime)
*/

            val realmList = mRealm.where(TestRea::class.java).findAll()
            spotDataList.clear()
            dataList.clear()

            val df = SimpleDateFormat("yyyy/MM/dd hh:MM")
            for (_sd in realmList) {
                spotDataList.add(_sd)
                dataList.add(_sd.name + "\n" + df.format(_sd.datetime))
            }
            adapter.notifyDataSetChanged()

            val item = sort.selectedItem as String
            if ("降順" == item) {
                sortList()
            }

        }else{

        }
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

    //ボトムバータップ時
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this,HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this,SearchPlanActivity::class.java))
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

    fun sortList() {
        spotDataList.reverse()
        dataList.reverse()
        adapter.notifyDataSetChanged()
        //val userSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList)
        //listView.adapter = userSpotAdapter
    }
}
