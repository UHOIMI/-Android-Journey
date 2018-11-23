package com.example.g015c1140.journey

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_edit_user.*


class EditUserActivity : AppCompatActivity() {

    var sharedPreferences: SharedPreferences? = null
    var imageFlg = 0

    companion object {
        private const val RESULT_PICK_IMAGEFILE = 1001

        private const val RESULT_CROP = 2003
    }

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
        editUserNameEditText.setText(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_NAME, ""))

        val passLast = sharedPreferences!!.getString(Setting().USER_SHARED_PREF_PASSWORD, "")
        var pass = ""
        for (_passCnt in 0 until passLast.length - 4) {
            pass += "*"
        }

        //↓ここで落ちたのでコメントにしました
        pass += passLast.substring(passLast.length -4 , passLast.length)
        editUserPassTextView.text = pass
        val generation = when (sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")) {
            "10" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")}代"
        }
        editUserGenerationoSpinner.setSelection((editUserGenerationoSpinner.adapter as ArrayAdapter<String>).getPosition(generation))
        editUserCommentEditText.setText( sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT,"") )
    }


    fun onClickImage(v: View) {
        // イメージ画像がクリックされたときに実行される処理
        when(v.id){
            R.id.editUserHeaderImageButton -> imageFlg = 1
            R.id.editUserIconImageView -> imageFlg = 2
        }
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            RESULT_PICK_IMAGEFILE -> {
                if (resultCode != Activity.RESULT_OK) return
                val uri = data.data // 選ばれた写真のUri
                val intent = Intent(this, CropIconActivity::class.java)
                intent.putExtra("uri", uri)
                intent.putExtra("imageFlg", imageFlg)
                startActivityForResult(intent, RESULT_CROP)
            }

            RESULT_CROP -> {
                val myApp: MyApplication = this.application as MyApplication
                val bmp = myApp.getBmp()
                myApp.clearBmp()
                when(imageFlg){
                    1 -> editUserHeaderImageButton.setImageBitmap(bmp)
                    2 ->  editUserIconImageView.setImageBitmap(bmp)
                }
            }
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
                startActivity(Intent(this,DetailUserActivity::class.java))
                finish()
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
        editUserPassVisibleView.filters = arrayOf(inputFilter, lengthFilter)

        val alertDialog: AlertDialog
        val adb = AlertDialog.Builder(this)

        adb.setTitle("ユーザーIDを入力してください")
        adb.setView(editUserPassVisibleView)
        adb.setPositiveButton("確認", null)
        adb.setNegativeButton("終了", null)
        alertDialog = adb.show()

        val buttonOk = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val buttonNg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        buttonOk.setOnClickListener {
            // OKボタンをタップした時の処理をここに記述
            if (editUserPassVisibleView.text.toString() == sharedPreferences!!.getString(Setting().USER_SHARED_PREF_ID, "")) {
                AlertDialog.Builder(this).apply {
                    setTitle("パスワードはこちらです")
                    setMessage(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_PASSWORD, ""))
                    setPositiveButton("OK", null)
                    show()
                }
                alertDialog.dismiss()
            } else {
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
        var generation = ""
        if (editUserNameEditText.text.toString().trim().isEmpty()) {
            result += "ユーザー名を入力してください\n"
        }

        if (editUserGenerationoSpinner.selectedItem.toString() == "あなたの年代を選択してください") {
            result += "年代を選択してください\n"
        }

        if (result == "") {
            val userDataList = mutableListOf(mutableListOf<String>())
            userDataList.removeAt(0)

            if (editUserNameEditText.text.toString() != sharedPreferences!!.getString(Setting().USER_SHARED_PREF_NAME, "")) {
                userDataList.add(mutableListOf("&user_name", editUserNameEditText.text.toString()))
            }

            generation =
                    when {
                        editUserGenerationoSpinner.selectedItem.toString() == "10歳以下" -> "10"
                        editUserGenerationoSpinner.selectedItem.toString() == "100歳以上" -> "100"
                        else -> editUserGenerationoSpinner.selectedItem.toString().replace("代", "")
                    }

            if (generation != sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")) {
                userDataList.add(mutableListOf("&generation", generation))
            }

            if (editUserCommentEditText.text.toString() != sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT, "")) {
                userDataList.add(mutableListOf("&comment", editUserCommentEditText.text.toString()))
            }

//            "&user_icon"
//            "&user_header"

            if (userDataList.isNotEmpty()) {
                userDataList.add(mutableListOf("&token", sharedPreferences!!.getString(Setting().USER_SHARED_PREF_TOKEN, "none")))

                val puat = PutUserAsyncTask()
                puat.setOnCallback(object : PutUserAsyncTask.CallbackPutUserAsyncTask() {
                    override fun callback(result: String) {
                        super.callback(result)
                        // ここからAsyncTask処理後の処理を記述します。
                        Log.d("test UserCallback", "非同期処理$result")
                        if (result == "RESULT-OK") {
                            //完了
                            val sharedPrefEditor = sharedPreferences!!.edit()

                            for (userData in userDataList) {
                                when (userData[0]) {
                                    "&user_header" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_HEADERIMAGE, "")
                                    "&user_icon" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ICONIMAGE, "")
                                    "&user_name" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_NAME, userData[1])
                                    "&generation" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENERATION, generation)
                                    "&comment" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_COMMENT, userData[1])
                                }
                            }
                            sharedPrefEditor.apply()
                            startActivity(Intent(this@EditUserActivity, DetailUserActivity::class.java))
                            finish()
                        } else {
                            AlertDialog.Builder(this@EditUserActivity).apply {
                                setTitle("保存に失敗しました")
                                setMessage("もう一度実行してください")
                                setPositiveButton("確認", null)
                                show()
                            }
                        }
                    }
                })
                puat.execute(userDataList)
            } else {
                startActivity(Intent(this@EditUserActivity, DetailUserActivity::class.java))
                finish()
            }

        } else {
            AlertDialog.Builder(this).apply {
                setTitle("編集内容が間違っています")
                setMessage(result)
                setPositiveButton("確認", null)
                show()
            }
        }
    }
}
