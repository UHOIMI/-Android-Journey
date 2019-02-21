package com.example.g015c1140.journey

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class GetImageAsyncTask : AsyncTask<ArrayList<ArrayList<String>>, Void, ArrayList<ArrayList<Bitmap?>>?>() {

    private var callbackGetImageAsyncTask: CallbackGetImageAsyncTask? = null

    // 非同期処理
    override fun doInBackground(vararg params: ArrayList<ArrayList<String>>): ArrayList<ArrayList<Bitmap?>>? {
        return downloadImage(params[0])
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    override fun onPostExecute(resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
        super.onPostExecute(resultBmpList)

        if (resultBmpList == null) {
            callbackGetImageAsyncTask!!.callback("RESULT-NG",null)
            return
        }

        callbackGetImageAsyncTask!!.callback("RESULT-OK",resultBmpList)
        return
    }


    private fun downloadImage(imageNameList: ArrayList<ArrayList<String>>): ArrayList<ArrayList<Bitmap?>> {
        val bmpList = arrayListOf<ArrayList<Bitmap?>>()
        var nameList:ArrayList<Bitmap?>
        var bmp: Bitmap?
        var urlConnection: HttpURLConnection? = null

        imageNameList.forEach { name ->
            nameList = arrayListOf()
            name.forEach {
                if (it != "") {
                    try {
                        val url = URL(it)

                        // HttpURLConnection インスタンス生成
                        urlConnection = url.openConnection() as HttpURLConnection

                        // タイムアウト設定
//                        urlConnection.readTimeout = 10000
//                        urlConnection.connectTimeout = 20000

                        // リクエストメソッド
                        urlConnection!!.requestMethod = "GET"

                        // リダイレクトを自動で許可しない設定
                        urlConnection!!.instanceFollowRedirects = false

                        // ヘッダーの設定(複数設定可能)
                        urlConnection!!.setRequestProperty("Accept-Language", "jp")

                        // 接続
                        urlConnection!!.connect()

                        val respCode = urlConnection!!.responseCode

                        when (respCode) {
                            HttpURLConnection.HTTP_OK -> {
                                var `is`: InputStream? = null
                                try {
                                    `is` = urlConnection!!.inputStream
                                    bmp = BitmapFactory.decodeStream(`is`)
                                    `is`!!.close()
                                    nameList.add(bmp!!)
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
                        e.printStackTrace()
                    } finally {
                        urlConnection?.disconnect()
                    }

                }else{
                    nameList.add(null)
                }
            }
            bmpList.add(nameList)
        }
        return bmpList
    }

    fun setOnCallback(cb: CallbackGetImageAsyncTask) {
        callbackGetImageAsyncTask = cb
    }

    open class CallbackGetImageAsyncTask {
        open fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {}
    }
}