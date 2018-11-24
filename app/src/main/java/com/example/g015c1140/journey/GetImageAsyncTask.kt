package com.example.g015c1140.journey

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class GetImageAsyncTask : AsyncTask<String, Void, Bitmap>() {

    private var callbackGetImageAsyncTask: CallbackGetImageAsyncTask? = null

    // 非同期処理
    override fun doInBackground(vararg params: String): Bitmap? {
        return downloadImage(params[0])
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    override fun onPostExecute(bmp: Bitmap?) {
        super.onPostExecute(bmp)

        if (bmp == null) {
            Log.d("test GetUserIdTask", "return null")
            callbackGetImageAsyncTask!!.callback("RESULT-NG",bmp)
            return
        }

        Log.d("test GetImage", "onPostEx: $bmp")
        callbackGetImageAsyncTask!!.callback("RESULT-OK",bmp)
        return
    }


    private fun downloadImage(iconName: String): Bitmap? {
        var bmp: Bitmap? = null

        var urlConnection: HttpURLConnection? = null

        try {
            val url = URL(iconName)

            // HttpURLConnection インスタンス生成
            urlConnection = url.openConnection() as HttpURLConnection

            // タイムアウト設定
//            urlConnection.readTimeout = 10000
//            urlConnection.connectTimeout = 20000

            // リクエストメソッド
            urlConnection.requestMethod = "GET"

            // リダイレクトを自動で許可しない設定
            urlConnection.instanceFollowRedirects = false

            // ヘッダーの設定(複数設定可能)
            urlConnection.setRequestProperty("Accept-Language", "jp")

            // 接続
            urlConnection.connect()

            val resp = urlConnection.responseCode

            when (resp) {
                HttpURLConnection.HTTP_OK -> {
                    var `is`: InputStream? = null
                    try {
                        `is` = urlConnection.inputStream
                        bmp = BitmapFactory.decodeStream(`is`)
                        `is`!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } finally {
                        `is`?.close()
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                }
                else -> {
                }
            }
        } catch (e: Exception) {
            Log.d("test", "GetImage error")
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
        }

        return bmp
    }

    fun setOnCallback(cb: CallbackGetImageAsyncTask) {
        callbackGetImageAsyncTask = cb
    }

    open class CallbackGetImageAsyncTask {
        open fun callback(result: String,bmp: Bitmap?) {}
    }
}