package com.example.g015c1140.journey

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class PostLoginAsyncTask : AsyncTask<ArrayList<String>, Void, String>() {
    //callBack用
    private var callbackPostLoginAsyncTask: CallbackPostLoginAsyncTask? = null
    lateinit var token: String

    override fun doInBackground(vararg params: ArrayList<String>): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult: String? = null
        var httpResult: String? = null

        try {
            val url = URL(Setting().USER_LOGIN_URL)
            val list = params[0]

            for (value in list) {
                if (value == "none")
                    return "PRAM-Error"
            }

            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。


            var out: OutputStream? = null
            try {

                out = connection.outputStream
                out.write((
                        "user_id=${list[0]}" + "&user_pass=${list[1]}").toByteArray()
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

                postResult = try {
                    JSONObject(sb.toString()).getString("status")
                    "PostUserLogin-NG"
                } catch (e: JSONException) {
                    e.printStackTrace()
                    token = sb.toString().replace("\"", "")
                    200.toString()
                }

            } catch (e: IOException) {
                // POST送信エラー
                e.printStackTrace()
                postResult = "PostUserLogin-Error"
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
                callbackPostLoginAsyncTask!!.callback("RESULT-OK", token)
                return
            }

            else -> {
                Log.d("test PostSpot", "HTTP-NG")
                callbackPostLoginAsyncTask!!.callback("RESULT-NG", "")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostLoginAsyncTask) {
        callbackPostLoginAsyncTask = cb
    }

    open class CallbackPostLoginAsyncTask {
        open fun callback(result: String, token: String) {}
    }
}