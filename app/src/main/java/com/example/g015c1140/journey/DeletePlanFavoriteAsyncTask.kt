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

class DeletePlanFavoriteAsyncTask(pId: String, tkn:String) : AsyncTask<Void, String, String>() {

    //callBack用
    private var callbackDeletePlanFavoriteAsyncTask: CallbackDeletePlanFavoriteAsyncTask? = null
    private var result: String? = null
    private val PLAN_ID = pId
    private val TOKEN = tkn

    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult = ""
        var httpResult = ""

        if (TOKEN == "none")
            return "TOKEN-Error"

        if (PLAN_ID == "none")
            return "PLAN_ID-Error"

        try {
            val url = URL("${Setting().FAVORITE_DELETE_URL}plan_id=$PLAN_ID&token=$TOKEN")

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

            var out: OutputStream? = null
            try {

                out = connection.outputStream
//                out.write((
//                        "plan_id=$PLAN_ID" +
//                                "&token=$TOKEN"
//                        ).toByteArray()
//                )
//
//                out.flush()
//                Log.d("debug", "flush")

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
                postResult = "PLAN_DELETE_ERROR"
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

        when (result) {
            "HTTP-OK:200" -> {
                Log.d("test PostSpot", "HTTP-OK")
                callbackDeletePlanFavoriteAsyncTask!!.callback("RESULT-OK")
                return
            }

            else -> {
                Log.d("test PostSpot", "HTTP-NG")
                callbackDeletePlanFavoriteAsyncTask!!.callback("RESULT-NG")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackDeletePlanFavoriteAsyncTask) {
        callbackDeletePlanFavoriteAsyncTask = cb
    }

    open class CallbackDeletePlanFavoriteAsyncTask {
        open fun callback(resultFavoriteString: String) {}
    }
}