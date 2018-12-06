package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_detail_user.*
import org.json.JSONObject

class DetailUserActivity : AppCompatActivity() {

    private var headerFlg = 0
    private var iconFlg = 0
    private var anotherUserFlg = false

    val IMAGE_OK = 100

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
            //finish()
        }

        anotherUserFlg = intent.getBooleanExtra("ANOTHER_USER", false)
    }

    override fun onResume() {
        super.onResume()

        if (!anotherUserFlg) {
            val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)

            val headerString = sharedPreferences.getString(Setting().USER_SHARED_PREF_HEADERIMAGE, "")
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

            val iconString = sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "")
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

            detailUserNameTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_NAME, "名前が存在しません")
            detailUserGenderTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENDER, "性別が存在しません")
            val generation = sharedPreferences.getString(Setting().USER_SHARED_PREF_GENERATION, "年代が存在しません")
            detailUserGenerationTextView.text = when (generation) {
                "10" -> "10歳以下"
                "100" -> "100歳以上"
                else -> "$generation 代"
            }
            detailUserCommentTextView.text = sharedPreferences.getString(Setting().USER_SHARED_PREF_COMMENT, "コメントが存在しません")
        } else {
            //他のユーザー情報
            val guaat = GetUserAccountAsyncTask(arrayListOf(intent.getStringExtra("USER_ID")))
            guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                    super.callback(resultUserAccountList)
                    if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                        resultUserAccountList.removeAt(resultUserAccountList.size - 1)
                        //完了
                        /****************/
                        detailUserNameTextView.text = resultUserAccountList[0].getString("user_name")
                        val generation = resultUserAccountList[0].getString("generation")
                        detailUserGenerationTextView.text = when (generation) {
                            "10" -> "10歳以下"
                            "100" -> "100歳以上"
                            else -> "$generation 代"
                        }
                        detailUserGenderTextView.text = resultUserAccountList[0].getString("gender")
                        detailUserCommentTextView.text = resultUserAccountList[0].getString("comment")

                        val headerString = resultUserAccountList[0].getString("user_header")
                        if (headerString.contains("http")) {
                            val giat = GetImageAsyncTask()
                            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                    if (resultBmpString == "RESULT-OK") {
                                        detailUserHeaderImageView.setImageBitmap(resultBmpList!![0][0])
                                    } else {
                                        Toast.makeText(this@DetailUserActivity, "ヘッダー取得失敗", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                            giat.execute(arrayListOf(arrayListOf(headerString)))
                        }

                        val iconString  = resultUserAccountList[0].getString("user_icon")
                        if (iconString.contains("http")) {
                            val giat = GetImageAsyncTask()
                            giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                                override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                    if (resultBmpString == "RESULT-OK") {
                                        detailUserIconCircleView.setImageBitmap(resultBmpList!![0][0])
                                    } else {
                                        Toast.makeText(this@DetailUserActivity, "アイコン取得失敗", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            })
                            giat.execute(arrayListOf(arrayListOf(iconString)))
                        }

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
