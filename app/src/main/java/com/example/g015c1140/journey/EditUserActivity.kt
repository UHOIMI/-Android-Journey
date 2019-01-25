package com.example.g015c1140.journey

import android.app.Activity
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit_user.*
import java.io.FileOutputStream
import java.io.IOException


class EditUserActivity : AppCompatActivity() {

    var sharedPreferences: SharedPreferences? = null
    var imageFlg = 0
    var headerFlg = 0
    var iconFlg = 0

    val IMAGE_EDIT = 200

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
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(userEditNavigation)

        val myApp = this.application as MyApplication
        userEditNavigation.selectedItemId = myApp.getBnp()
        userEditNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        headerFlg = intent.getIntExtra("headerFlg", -1)
        if (headerFlg == 100) {
            val myApp: MyApplication = this.application as MyApplication
            val bmp = myApp.getBmp_1()
            myApp.clearBmp_1()
            editUserHeaderImageButton.setImageBitmap(bmp)
        }

        iconFlg = intent.getIntExtra("iconFlg", -1)
        if (iconFlg == 100) {
            val myApp: MyApplication = this.application as MyApplication
            val bmp = myApp.getBmp_2()
            myApp.clearBmp_2()
            editUserIconImageView.setImageBitmap(bmp)
        }

        sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        editUserNameEditText.setText(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_NAME, ""))

        val passLast = sharedPreferences!!.getString(Setting().USER_SHARED_PREF_PASSWORD, "")
        var pass = ""
        for (_passCnt in 0 until passLast.length - 4) {
            pass += "*"
        }
        //↓ここで落ちたのでコメントにしました
        pass += passLast.substring(passLast.length - 4, passLast.length)
        editUserPassTextView.text = pass

        val generation = when (sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")) {
            "0" -> "10歳以下"
            "100" -> "100歳以上"
            else -> "${sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")}代"
        }
        editUserGenerationoSpinner.setSelection((editUserGenerationoSpinner.adapter as ArrayAdapter<String>).getPosition(generation))

        editUserCommentEditText.setText(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT, ""))

        editUserCommentEditText.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
            if (focus) {
                Toast.makeText(applicationContext, "Got the focus", Toast.LENGTH_LONG).show()
                userEditNavigation.visibility = View.INVISIBLE
            } else {
                Toast.makeText(applicationContext, "Lost the focus", Toast.LENGTH_LONG).show()
                userEditNavigation.visibility = View.VISIBLE
            }
        }

    }

    fun logoutButtonTapped(view: View) {
        AlertDialog.Builder(this).apply {
            setTitle("ログアウトします")
            setMessage("本当によろしいですか？")
            setPositiveButton("はい") { _, _ ->
                // OKをタップしたときの処理
                val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
                val sharedPrefEditor = sharedPreferences.edit()
                sharedPrefEditor.clear().apply()
                Toast.makeText(this@EditUserActivity, "ログアウトしました", Toast.LENGTH_SHORT).show()
                finishAffinity()
                startActivity(Intent(this@EditUserActivity, IndexActivity::class.java))
            }
            setNegativeButton("いいえ", null)
            show()
        }
    }

    fun onClickImage(v: View) {
        editUserHeaderImageButton.isClickable = false
        editUserIconImageView.isClickable = false
        // イメージ画像がクリックされたときに実行される処理
        when (v.id) {
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
                val bmp = myApp.getBmp_1()
                myApp.clearBmp_1()
                when (imageFlg) {
                    1 -> {
                        editUserHeaderImageButton.setImageBitmap(bmp)
                        headerFlg = IMAGE_EDIT
                    }
                    2 -> {
                        editUserIconImageView.setImageBitmap(bmp)
                        iconFlg = IMAGE_EDIT
                    }
                }
                editUserHeaderImageButton.isClickable = true
                editUserIconImageView.isClickable = true
            }

            else ->{
                editUserHeaderImageButton.isClickable = true
                editUserIconImageView.isClickable = true
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
                finish()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    //Password表示ボタン処理
    fun passVisibleButtonTapped(v: View) {
        // テキスト入力用Viewの作成
        val inputFilter = InputFilter { source, _, _, _, _, _ ->
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
        editUserSaveButton.isClickable = false
        //APIにPOST
        var result = ""
        var generation = ""
        if (editUserNameEditText.text.toString().trim().isEmpty()) {
            result += "ユーザー名を入力してください\n"
        }

        if (editUserGenerationoSpinner.selectedItemPosition == 0) {
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
                        editUserGenerationoSpinner.selectedItem.toString() == "10歳以下" -> "0"
                        editUserGenerationoSpinner.selectedItem.toString() == "100歳以上" -> "100"
                        else -> editUserGenerationoSpinner.selectedItem.toString().replace("代", "")
                    }

            if (generation != sharedPreferences!!.getString(Setting().USER_SHARED_PREF_GENERATION, "")) {
                userDataList.add(mutableListOf("&generation", generation))
            }

            if (editUserCommentEditText.text.toString() != sharedPreferences!!.getString(Setting().USER_SHARED_PREF_COMMENT, "")) {
                userDataList.add(mutableListOf("&comment", editUserCommentEditText.text.toString()))
            }


            val imageStrList = arrayListOf<String>()
            var headerUri = ""
            if (headerFlg == IMAGE_EDIT) {
                headerUri = saveAndLoadImage("header")
                imageStrList.add(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_HEADERIMAGE, "").substringAfterLast("/"))
            }else{
                imageStrList.add("")
            }

            var iconUrl = ""
            if (iconFlg == IMAGE_EDIT) {
                iconUrl = saveAndLoadImage("icon")
                imageStrList.add(sharedPreferences!!.getString(Setting().USER_SHARED_PREF_ICONIMAGE, "").substringAfterLast("/"))
            }else{
                imageStrList.add("")
            }


            val hPuiat = PostUserIconAsyncTask()
            hPuiat.setOnCallback(object : PostUserIconAsyncTask.CallbackPostUserIconAsyncTask() {
                override fun callback(result: String, data: String) {
                    super.callback(result, data)
                    // ここからAsyncTask処理後の処理を記述します。
                    when (result) {
                        "RESULT-OK" -> //完了した場合
                            userDataList.add(mutableListOf("&user_header", "${Setting().USER_IMAGE_GET_URL}$data"))
                        "NO-IMAGE" -> {
                        }
                        else -> {
                            failedAsyncTask()
                            return
                        }
                    }

                    val iPuiat = PostUserIconAsyncTask()
                    iPuiat.setOnCallback(object : PostUserIconAsyncTask.CallbackPostUserIconAsyncTask() {
                        override fun callback(result: String, data: String) {
                            super.callback(result, data)
                            // ここからAsyncTask処理後の処理を記述します。
                            when (result) {
                                "RESULT-OK" -> //完了した場合
                                    userDataList.add(mutableListOf("&user_icon", "${Setting().USER_IMAGE_GET_URL}$data"))
                                "NO-IMAGE" -> {
                                }
                                else -> {
                                    failedAsyncTask()
                                    return
                                }
                            }

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
                                                    "&user_header" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_HEADERIMAGE, userData[1])
                                                    "&user_icon" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ICONIMAGE, userData[1])
                                                    "&user_name" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_NAME, userData[1])
                                                    "&generation" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENERATION, generation)
                                                    "&comment" -> sharedPrefEditor.putString(Setting().USER_SHARED_PREF_COMMENT, userData[1])
                                                }
                                            }
                                            sharedPrefEditor.apply()

                                            val diat = DeleteImageAsyncTask(imageStrList)
                                            diat.setOnCallback(object : DeleteImageAsyncTask.CallbackDeleteImageAsyncTask() {
                                                override fun callback(resultDeleteImageString: String) {
                                                    super.callback(resultDeleteImageString)
                                                    // ここからAsyncTask処理後の処理を記述します。
                                                    Log.d("test UserCallback", "非同期処理$resultDeleteImageString")
                                                    if (resultDeleteImageString == "RESULT-OK") {
                                                        //完了
                                                        Toast.makeText(this@EditUserActivity, "変更が完了しました", Toast.LENGTH_SHORT).show()
                                                        finish()
                                                    } else {
                                                        failedAsyncTask()
                                                    }
                                                }
                                            })
                                            diat.execute()
                                        } else {
                                            failedAsyncTask()
                                        }
                                    }
                                })
                                puat.execute(userDataList)
                            } else {
                                finish()
                            }
                        }
                    })
                    iPuiat.execute(iconUrl, sharedPreferences!!.getString(Setting().USER_SHARED_PREF_ID, ""))
                }
            })
            hPuiat.execute(headerUri, sharedPreferences!!.getString(Setting().USER_SHARED_PREF_ID, ""))

        } else {
            AlertDialog.Builder(this).apply {
                setTitle("編集内容が間違っています")
                setMessage(result)
                setPositiveButton("確認", null)
                show()
            }
            editUserSaveButton.isClickable = true
        }
    }

    private fun saveAndLoadImage(viewName: String): String {
        var fileOut: FileOutputStream? = null
        var uri: Uri? = null
        var imageName = ""
        var imageView: ImageView? = null

        when (viewName) {
            "header" -> {
                imageName = "Header.jpg"
                imageView = editUserHeaderImageButton
            }
            "icon" -> {
                imageName = "Icon.jpg"
                imageView = editUserIconImageView
            }
        }

        try {
            // openFileOutputはContextのメソッドなのでActivity内ならばthisでOK
            fileOut = this.openFileOutput(imageName, Context.MODE_PRIVATE)
            (imageView!!.drawable as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOut)
            uri = Uri.fromFile(getFileStreamPath(imageName))
        } catch (e: IOException) {
            failedAsyncTask()
        } finally {
            fileOut?.close()
            return getPathFromUri(this, uri!!)
        }
    }

    private fun getPathFromUri(context: Context, uri: Uri): String {
        var isAfterKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        Log.e("TAG", "uri:" + uri.authority);
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {// ExternalStorageProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                //if ("primary".equalsIgnoreCase(type)) {
                return if ("primary" == type) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    "/stroage/" + type + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {// DownloadsProvider
                var id: String = DocumentsContract.getDocumentId(uri)
                var contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents" == uri.authority) {// MediaProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                var contentUri: Uri? = null
                contentUri = MediaStore.Files.getContentUri("external")
                var selection = "_id=?"
                /*var selectionArgs = {
                        split[1]
                }*/
                var selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, *selectionArgs)
            }
        } else if ("content" == uri.scheme) {//MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {// File
            return uri.path
        }
        return ""
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, vararg selectionArgs: String?/*[]*/): String {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val cindex: Int = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex)
            }
        } finally {
            cursor?.close()
        }
        return ""
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("保存に失敗しました")
            setMessage("もう一度実行してください")
            setPositiveButton("確認", null)
            show()
        }
        editUserSaveButton.isClickable = true
    }
}
