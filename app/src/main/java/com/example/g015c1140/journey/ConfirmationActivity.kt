package com.example.g015c1140.journey

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

class ConfirmationActivity : AppCompatActivity() {
    lateinit var userData: ArrayList<String>
    var imageUri = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmation)
        userData = intent.extras.getStringArrayList("USERDATA")

        if (userData[0] != "") {
            imageUri = getPathFromUri(this, Uri.parse(userData[0]))
            iconImageView.setImageBitmap(getBitmapFromUri(Uri.parse(userData[0])))
        }

        idTextView.text = userData[1]
        nameTextView.text = userData[2]
        generationTextView.text = userData[4]
        genderTextView.text = userData[5]
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun getPathFromUri(context: Context, uri: Uri): String {
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

    fun onModificationButtonTapped(v: View) {
        startActivity(Intent(this, CreateActivity::class.java).putStringArrayListExtra("USERDATA", userData).putExtra("EditFlg", 100))
        finish()
    }

    fun onDoneButtonTapped(v: View) {
        //投稿
        userData[0] = imageUri
        userData[4] =
                when {
                    userData[4] == "10歳以下" -> "10"
                    userData[4] == "100歳以上" -> "100"
                    else -> userData[4].replace("代", "")
                }
        userData[5] = userData[5].replace("性", "")
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()
        sharedPrefEditor.putBoolean("userFlg", true)
        sharedPrefEditor.putString("iconImage", userData[0])
        sharedPrefEditor.putString("id", userData[1])
        sharedPrefEditor.putString("name", userData[2])
        sharedPrefEditor.putString("password", userData[3])
        sharedPrefEditor.putString("generation", userData[4])
        sharedPrefEditor.putString("gender", userData[5])
        sharedPrefEditor.apply()

        if (userData[0] == ""){
            userCreate()
        }else {
            val puiat = PostUserIconAsyncTask()
            puiat.setOnCallback(object : PostUserIconAsyncTask.CallbackPostUserIconAsyncTask() {
                override fun callback(result: String, data: String) {
                    super.callback(result, data)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test UserImageCallback", "非同期処理$result　　URL $data")
                    if (result == "RESULT-OK") {
                        //完了した場合
                        userData[0] = data
                        userCreate()
                    } else {
                        failedAsyncTask()
                    }
                }
            })
            puiat.execute(userData[0])
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

    private fun userCreate(){
        val puat = PostUserAsyncTask()
        puat.setOnCallback(object : PostUserAsyncTask.CallbackPostUserAsyncTask() {
            override fun callback(result: String,token:String) {
                super.callback(result,token)
                // ここからAsyncTask処理後の処理を記述します。
                Log.d("test UserCallback", "非同期処理$result")
                if (result == "RESULT-OK") {
                    //完了
                    val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                    val sharedPrefEditor = sharedPreferences.edit()
                    sharedPrefEditor.putString("token", token)
                    sharedPrefEditor.apply()
                    Toast.makeText(this@ConfirmationActivity, "登録が完了しました", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@ConfirmationActivity,"Token = 「$token」", Toast.LENGTH_SHORT).show()
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
}
