package com.example.g015c1140.journey

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class PostFavoriteAsyncTask(i: String, t: String) : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackPostFavoriteAsyncTask: CallbackPostFavoriteAsyncTask? = null
    private val PLAN_ID = i

    private val TOKEN = t

    //insert
    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult = ""
        var httpResult = ""

        if (TOKEN == "none")
            return "TOKEN-Error"

        if (PLAN_ID == "none")
            return "PLAN_ID-Error"


        try {
            val url = URL(Setting().FAVORITE_POST_URL)

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。


            var out: OutputStream? = null
            try {

                out = connection.outputStream
                out.write((
                        "&plan_id=$PLAN_ID" +
                                "&token=$TOKEN"
                        ).toByteArray()
                )

                out.flush()
                Log.d("debug", "flush")

                val `is` = connection.inputStream
                val bReader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                val sb = StringBuilder()
                for (line in bReader.readLines()) {
                    line.run { sb.append(line) }
                }
                bReader.close()
                `is`.close()
                postResult = JSONObject(sb.toString()).getString("status")

            } catch (e: IOException) {
                // POST送信エラー
                e.printStackTrace()
                postResult = "PLAN送信エラー"
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
        return "$httpResult:$postResult"
        //finallyで接続を切断してあげましょう。
        //失敗した時はnullやエラーコードなどを返しましょう。
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        Log.d("test PlanSpot", "onPostEx: $result")
        when (result) {
            "HTTP-OK:200" -> {
                Log.d("test PostSpot", "HTTP-OK")
                callbackPostFavoriteAsyncTask!!.callback("RESULT-OK")
                return
            }

            else -> {
                Log.d("test PostSpot", "HTTP-NG")
                callbackPostFavoriteAsyncTask!!.callback("RESULT-NG")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostFavoriteAsyncTask) {
        callbackPostFavoriteAsyncTask = cb
    }

    open class CallbackPostFavoriteAsyncTask {
        open fun callback(result: String) {}
    }
}