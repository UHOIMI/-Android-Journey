package com.example.g015c1140.journey

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_edit_user.*
import com.isseiaoki.simplecropview.CropImageView





class EditUserActivity : AppCompatActivity() {

    var sharedPreferences:SharedPreferences? = null


    var userIconUri = ""
    var userData = arrayListOf<String>()







    companion object {
        private const val RESULT_PICK_IMAGEFILE = 1001

        private const val REQUEST_CROP_PICK = 1002
    }

    //val userIconView = findViewById(R.id.editUserIconImageView) as CircleImageView


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

        //↓ここで落ちたのでコメントにしました
        //pass += passLast.substring(passLast.length -4 , passLast.length)
        editUserPassTextView.text = pass
        val generation = when( sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION,"") ){
            "10" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION,"")}代"
        }
        editUserGenerationoSpinner.setSelection((editUserGenerationoSpinner.adapter as ArrayAdapter<String>).getPosition(generation))
        editUserCommentEditText.setText( sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT,"") )

        //ユーザーアイコン丸くする
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        val iconPath = sharedPreferences.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "none")
        if (iconPath != "none") {
            val iconBmp = BitmapFactory.decodeFile(iconPath)
            val resizedIconBitmap = Bitmap.createScaledBitmap(iconBmp, 263, 263, false)
            val drawable = RoundedBitmapDrawableFactory.create(resources, resizedIconBitmap)
            //丸く加工
            drawable.cornerRadius = 150f
            editUserIconImageView.setImageDrawable(drawable)
        }
    }





    fun onClickIconImage(v: View) {
        // イメージ画像がクリックされたときに実行される処理
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

                val cropImageView = findViewById(R.id.editUserIconImageView) as CropImageView


                cropImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.id.editUserIconImageView))


                /*val intent = Intent("com.android.camera.action.CROP")
                intent.data = uri
                intent.putExtra("outputX", 200)
                intent.putExtra("outputY", 200)
                intent.putExtra("aspectX", 1)
                intent.putExtra("aspectY", 1)
                intent.putExtra("scale", true)
                intent.putExtra("return-data", true)
                startActivityForResult(intent, REQUEST_CROP_PICK)
                */
            }
            REQUEST_CROP_PICK -> {
                if (resultCode != Activity.RESULT_OK) return
                val bitmap = data.extras!!.getParcelable<Bitmap>("data")
                // 取得したBitmapでごにょごにょする
                editUserIconImageView.setImageBitmap(bitmap)
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
