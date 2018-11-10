package com.example.g015c1140.journey

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_edit_user.*


class EditUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        //タイトル名セット
        title = "ユーザー編集"

        val toolbar = userEditToolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomavigation: BottomNavigationView = findViewById(R.id.userEditNavigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomavigation)
        userEditNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)


        //ユーザーアイコン丸くする
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        val iconPath = sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "none")
        if (iconPath != "none") {
            val iconBmp = BitmapFactory.decodeFile(iconPath)
            val resizedIoconBitmap = Bitmap.createScaledBitmap(iconBmp, 263, 263, false)
            val drawable = RoundedBitmapDrawableFactory.create(resources, resizedIoconBitmap)
            //丸く加工
            drawable.cornerRadius = 150f
            editUserIconImageView.setImageDrawable(drawable)
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

    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //Password表示ボタン処理
    fun passVisibleButtonTapped(v: View) {
        editUserPassTextView.text = "12345678901234567890"
    }

    //キャンセルボタン処理
    fun cancelButtonTapped(v: View) {
        finish()
    }

    //保存ボタン処理
    fun saveButtonTapped(v: View) {
        //APIにPOST
    }

}
