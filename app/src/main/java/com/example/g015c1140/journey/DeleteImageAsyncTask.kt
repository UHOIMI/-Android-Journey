package com.example.g015c1140.journey

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class DeleteImageAsyncTask(imgStrList: ArrayList<String>) : AsyncTask<Void, String, String>() {

    //callBack用
    private var callbackDeleteImageAsyncTask: CallbackDeleteImageAsyncTask? = null
    private val RESULT_LIST = arrayListOf<String>()
    private val IMAGE_STR_LIST = imgStrList

    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var httpResult = "none"


        for (_imgCnt in 0 until IMAGE_STR_LIST.size) {

            if (IMAGE_STR_LIST[_imgCnt] == "" || IMAGE_STR_LIST[_imgCnt] == "null") {
                RESULT_LIST.add("imageStr-Null")

            } else {
                try {
                    val url = URL(Setting().SERVER_IMAGE_DELETE_URL)

                    connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "DELETE"
                    connection.instanceFollowRedirects = false
                    connection.doOutput = true
                    connection.connect()  //ここで指定したAPIを叩いてみてます。

                    //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

                    var out: OutputStream? = null
                    try {

                        out = connection.outputStream
                        out.write((
                                "image_name=${IMAGE_STR_LIST[_imgCnt]}"
                                ).toByteArray()
                        )

                        out.flush()

                        val `is` = connection.inputStream
                        val bReader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                        val sb = StringBuilder()
                        for (line in bReader.readLines()) {
                            line.run { sb.append(line) }
                        }
                        bReader.close()
                        `is`.close()
                        RESULT_LIST.add("IMAGE_DELETE-OK")
                    } catch (e: IOException) {
                        // POST送信エラー
                        e.printStackTrace()
                        RESULT_LIST.add("IMAGE_DELETE_ERROR")
                    } finally {
                        out?.close()
                    }

                    val status = connection.responseCode
                    httpResult = when (status) {
                        HttpURLConnection.HTTP_OK -> "HTTP-OK"
                        else -> "status=$status"
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    connection?.disconnect()
                }

            }
        }

        return httpResult
        //finallyで接続を切断してあげましょう。
        //失敗した時はnullやエラーコードなどを返しましょう。
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        var resultStr = result
        if (resultStr == "none") {
            resultStr = "HTTP-OK"
        }
        when (resultStr) {
            "HTTP-OK" -> {

                for (_imgCnt in 0 until RESULT_LIST.size) {
                    if (!(RESULT_LIST[_imgCnt] == "IMAGE_DELETE-OK" || RESULT_LIST[_imgCnt] == "imageStr-Null")) {
                        checkFailed()
                        return
                    }
                }

                callbackDeleteImageAsyncTask!!.callback("RESULT-OK")
                return
            }

            else -> {
                checkFailed()
                return
            }
        }
    }

    private fun checkFailed() {
        callbackDeleteImageAsyncTask!!.callback("RESULT-NG")
    }

    fun setOnCallback(cb: CallbackDeleteImageAsyncTask) {
        callbackDeleteImageAsyncTask = cb
    }

    open class CallbackDeleteImageAsyncTask {
        open fun callback(resultDeleteImageString: String) {}
    }
}