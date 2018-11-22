package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_detail_user.*

class DetailUserActivity : AppCompatActivity() {

    //private lateinit var spot: SpotData

    companion object {
        private const val RESULT_PICK_IMAGEFILE = 1001
    }

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

        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)

        val iconString = sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "none")
        if (iconString != "none"){
            val giat = GetImageAsyncTask()
            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                override fun callback(result: String, bmp: Bitmap?) {
                    if (result == "RESULT-OK") {
                        detailUserIconCircleView.setImageBitmap(bmp)
                    }else{
                        Toast.makeText(this@DetailUserActivity, "アイコン取得失敗",Toast.LENGTH_SHORT).show()
                    }
                }
            })
            giat.execute(iconString)
        }

        detailUserNameTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_NAME,"名前が存在しません")
        detailUserGenderTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENDER,"性別が存在しません")
        val generation = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION,"年代が存在しません")
        detailUserGenerationTextView.text = when(generation){
            "10" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "$generation 代"
        }
        detailUserCommentTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_COMMENT,"コメントが存在しません")


        detailUserShowAllPlanButton.setOnClickListener {
            //perform your action here
            Toast.makeText(this,"すべて表示タップ",Toast.LENGTH_SHORT).show()
            //finish()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.saveButton -> {
            startActivity(Intent(this, EditUserActivity::class.java))
            Toast.makeText(this, "編集ボタン", Toast.LENGTH_LONG).show()
            finish()
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
