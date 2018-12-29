package com.example.g015c1140.journey

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_confirmation.*
import java.io.FileOutputStream
import java.io.IOException

class ConfirmationActivity : AppCompatActivity() {
    lateinit var userData: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        userData = intent.extras.getStringArrayList("USERDATA")

        if (userData[0] == "OK") {
            val myApp: MyApplication = this.application as MyApplication
            val bmp = myApp.getBmp_1()
            myApp.clearBmp_1()
            confirmUserIconImageView.setImageBitmap(bmp)
        }

        idTextView.text = userData[1]
        nameTextView.text = userData[2]
        generationTextView.text = userData[4]
        genderTextView.text = userData[5]
    }

    fun onModificationButtonTapped(v: View) {
        startActivity(Intent(this, CreateUserActivity::class.java).putStringArrayListExtra("USERDATA", userData).putExtra("EditFlg", 100))
        if (userData[0] == "OK") {
            val myApp = this.application as MyApplication
            myApp.setBmp_1((confirmUserIconImageView.drawable as BitmapDrawable).bitmap)
        }
        finish()
    }

    fun onDoneButtonTapped(v: View) {
        //投稿
        if (userData[0] == "OK") {
            userData[0] = seveAndLoadImage()
        }

        userData[4] =
                when {
                    userData[4] == "10歳以下" -> "10"
                    userData[4] == "100歳以上" -> "100"
                    else -> userData[4].replace("代", "")
                }
        userData[5] = userData[5]
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()
        sharedPrefEditor.putBoolean(Setting().USER_SHARED_PREF_FLG, true)
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ID, userData[1])
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_NAME, userData[2])
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_PASSWORD, userData[3])
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENERATION, userData[4])
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENDER, userData[5])
        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_COMMENT, "こんにちは")
        sharedPrefEditor.apply()

        if (userData[0] == "") {
            userCreate()
        } else {
            val puiat = PostUserIconAsyncTask()
            puiat.setOnCallback(object : PostUserIconAsyncTask.CallbackPostUserIconAsyncTask() {
                override fun callback(result: String, data: String) {
                    super.callback(result, data)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test UserImageCallback", "非同期処理$result　　URL $data")
                    if (result == "RESULT-OK") {
                        //完了した場合
                        userData[0] = "${Setting().USER_IMAGE_GET_URL}$data"
                        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ICONIMAGE, userData[0]).apply()
                        userCreate()
                    } else {
                        failedAsyncTask()
                    }
                }
            })
            puiat.execute(userData[0],userData[1])
        }
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("ユーザー登録に失敗しました")
            setMessage("もう一度実行してください")
            setPositiveButton("確認", null)
            show()
        }
    }

    private fun userCreate() {
        val puat = PostUserAsyncTask()
        puat.setOnCallback(object : PostUserAsyncTask.CallbackPostUserAsyncTask() {
            override fun callback(result: String, token: String) {
                super.callback(result, token)
                // ここからAsyncTask処理後の処理を記述します。
                Log.d("test UserCallback", "非同期処理$result")
                if (result == "RESULT-OK") {
                    //完了
                    val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
                    val sharedPrefEditor = sharedPreferences.edit()
                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_TOKEN, token)
                    sharedPrefEditor.apply()
                    Toast.makeText(this@ConfirmationActivity, "登録が完了しました", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ConfirmationActivity, "Token = 「$token」", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    AlertDialog.Builder(this@ConfirmationActivity).apply {
                        setTitle("投稿に失敗しました")
                        setMessage("もう一度実行してください")
                        setPositiveButton("確認", null)
                        show()
                    }
                }
            }
        })
        puat.execute(userData)
    }

    private fun seveAndLoadImage(): String{
        var fileOut: FileOutputStream? = null
        var uri :Uri? = null
        val imageName = "Icon.jpg"
        try {
            // openFileOutputはContextのメソッドなのでActivity内ならばthisでOK
            fileOut = this.openFileOutput(imageName, Context.MODE_PRIVATE)
            (confirmUserIconImageView.drawable as BitmapDrawable).bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOut)

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
        var projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                var cindex: Int = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex);
            }
        } finally {
            cursor?.close()
        }
        return ""
    }
}
