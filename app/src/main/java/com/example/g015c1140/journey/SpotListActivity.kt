package com.example.g015c1140.journey

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import java.text.SimpleDateFormat
//import sun.misc.MessageUtils.where



class SpotListActivity : AppCompatActivity() {

    private lateinit var mRealm : Realm
    var dataList = ArrayList<String>()
    var spotDataList = ArrayList<TestRea>()

    //private lateinit var spotDataList : RealmResults<TestRea>
    private lateinit var listView : ListView
    private lateinit var adapter : ArrayAdapter<String>

    var nowSort = "昇順"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spot_list)

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        var realmList = mRealm.where(TestRea::class.java).findAll()

        val df = SimpleDateFormat("yyyy/MM/dd hh:MM")

        for (_sd in realmList){
            spotDataList.add(_sd)
            dataList.add(_sd.name + "\n" + df.format(_sd.datetime))
        }

        listView = findViewById(R.id.spotList) as ListView
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList)
        listView.adapter = adapter

        listView.setOnItemClickListener {parent, view, position, id ->

            val intent = Intent(this, DetailSpotActivity::class.java)
            var tappedSpot = SpotData(spotDataList[position]!!.id,spotDataList[position]!!.name,spotDataList[position]!!.latitude,spotDataList[position]!!.longitude,spotDataList[position]!!.comment,spotDataList[position]!!.image_A,spotDataList[position]!!.image_B,spotDataList[position]!!.image_C,spotDataList[position]!!.datetime)
            intent.putExtra("SPOT",tappedSpot)
            startActivity(intent)
        }

        listView.setOnItemLongClickListener {  _, _, position, _ ->
            AlertDialog.Builder(this).apply {
                setTitle("スポット削除")
                setMessage("スポット:${dataList[position]} を削除しますか？")
                setPositiveButton("削除", { _, _ ->
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
                })
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
                if(nowSort != item){
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



    fun sortList(){
        spotDataList.reverse()
        dataList.reverse()
        adapter.notifyDataSetChanged()
        //val userSpotAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList)
        //listView.adapter = userSpotAdapter
    }
}
