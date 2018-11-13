package com.example.g015c1140.journey

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_edit_user.*



class EditUserActivity : AppCompatActivity() {

    var sharedPreferences:SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user)

        //タイトル名セット
        title = "ユーザー編集"

        val toolbar = editUserToolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomavigation: BottomNavigationView = findViewById(R.id.userEditNavigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomavigation)
        userEditNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        editUserNameEditText.setText(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_NAME,""))

        val passLast = sharedPreferences!!.getString(Setting().USER_SHARED_PREF_PASSWORD,"")
        var pass = ""
        for (_passCnt in 0 until passLast.length - 4) {
            pass += "*"
        }
        pass += passLast.substring(passLast.length -4 , passLast.length)
        editUserPassTextView.text = pass
        val generation = when( sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION,"") ){
            "10" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION,"")}代"
        }
        editUserGenerationoSpinner.setSelection((editUserGenerationoSpinner.adapter as ArrayAdapter<String>).getPosition(generation))
        editUserCommentEditText.setText( sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT,"") )

/*        //ユーザーアイコン丸くする
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        val iconPath = sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "none")
        if (iconPath != "none") {
            val iconBmp = BitmapFactory.decodeFile(iconPath)
            val resizedIoconBitmap = Bitmap.createScaledBitmap(iconBmp, 263, 263, false)
            val drawable = RoundedBitmapDrawableFactory.create(resources, resizedIoconBitmap)
            //丸く加工
            drawable.cornerRadius = 150f
            editUserIconImageView.setImageDrawable(drawable)
        }*/
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
        // テキスト入力用Viewの作成
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString().matches("^[a-zA-Z0-9]+$".toRegex())) {
                source
            } else {
                ""
            }
        }
        val lengthFilter = InputFilter.LengthFilter(20)
        val editUserPassVisibleView = EditText(this)
        editUserPassVisibleView.filters = arrayOf(inputFilter,lengthFilter)

        val alertDialog:AlertDialog
        val adb = AlertDialog.Builder(this)

        adb.setTitle("ユーザーIDを入力してください")
        adb.setView(editUserPassVisibleView)
        adb.setPositiveButton("確認", null)
        adb.setNegativeButton("終了",null)
        alertDialog = adb.show()

        val buttonOk = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val buttonNg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonOk.setOnClickListener {
            // OKボタンをタップした時の処理をここに記述
            if (editUserPassVisibleView.text.toString() == sharedPreferences!!.getString(Setting().USER_SHARED_PREF_ID, "")) {
                editUserPassVisibleView.setText(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_PASSWORD, ""))
            }else{
                AlertDialog.Builder(this).apply {
                    setTitle("ユーザーIDが間違っています")
                    setPositiveButton("OK", null)
                    show()
                }
            }
        }
        buttonNg.setOnClickListener {
            // NGボタンをタップした時の処理をここに記述
            alertDialog.dismiss()
        }
    }

    //キャンセルボタン処理
    fun cancelButtonTapped(v: View) {
        finish()
    }

    //保存ボタン処理
    fun saveButtonTapped(v: View) {
        //APIにPOST
        var result = ""
        if (editUserNameEditText.text.toString().trim().isEmpty()) {
            result += "ユーザー名を入力してください\n"
        }

        if (editUserGenerationoSpinner.selectedItem.toString() == "あなたの年代を選択してください") {
            result += "年代を選択してください\n"
        }
        if (result == "") {

        }else{
            AlertDialog.Builder(this).apply {
                setTitle("編集内容が間違っています")
                setMessage(result)
                setPositiveButton("確認", null)
                show()
            }
        }
    }
}
