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

class PostPlanAsyncTask(ui:String, t: String) : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackPostPlanAsyncTask: CallbackPostPlanAsyncTask? = null
    private val USER_ID = ui

    private val TOKEN = t

    //insert
    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult: String? = null
        var httpResult: String? = null

        if (TOKEN == "none")
            return "TOKEN-Error"
        if (USER_ID == "none")
            return "USER_ID-Error"

        try {
            val url = URL(Setting().PLAN_POST_URL)

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。


            var out: OutputStream? = null
            try {

                for (_cnt in 0 until 5) {
                    if (params[_cnt] == null) {
                        println("POSTTASK 引数異常WRITE：${params[_cnt]}")
                        return "POSTTASK 引数異常"
                    }
                }

                out = connection.outputStream
                out.write((
                        "user_id=$USER_ID" +
                                "&plan_title=${params[0]}" +
                                "&plan_comment=${params[1]}" +
                                "&transportation=${params[2]}" +
                                "&price=${params[3]}" +
                                "&area=${params[4]}" +
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
                callbackPostPlanAsyncTask!!.callback("RESULT-OK")
                return
            }

            else -> {
                Log.d("test PostSpot", "HTTP-NG")
                callbackPostPlanAsyncTask!!.callback("RESULT-NG")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostPlanAsyncTask) {
        callbackPostPlanAsyncTask = cb
    }

    open class CallbackPostPlanAsyncTask {
        open fun callback(result: String) {}
    }
}