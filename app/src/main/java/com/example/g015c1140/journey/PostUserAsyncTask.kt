package com.example.g015c1140.journey

import android.os.AsyncTask
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class PostUserAsyncTask : AsyncTask<ArrayList<String>, String, String>() {

    //callBack用
    private var callbackPostPlanAsyncTask: CallbackPostUserAsyncTask? = null
    private lateinit var token: String

    //insert
    override fun doInBackground(vararg params: ArrayList<String>?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult: String? = null
        var httpResult: String? = null

        try {
            val url = URL(Setting().USER_POST_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            var out: OutputStream? = null
            try {
                val userData = params[0]

                out = connection.outputStream
                out.write(
                        ("user_icon=${userData!![0]}" +
                                "&user_id=${userData[1]}" +
                                "&user_name=${userData[2]}" +
                                "&user_pass=${userData[3]}" +
                                "&generation=${userData[4]}" +
                                "&gender=${userData[5]}" +
                                "&comment=こんにちは"
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
                postResult = "USER-NG"
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
                callbackPostPlanAsyncTask!!.callback("RESULT-OK", token)
                return
            }

            else -> {
                callbackPostPlanAsyncTask!!.callback("RESULT-NG", "")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostUserAsyncTask) {
        callbackPostPlanAsyncTask = cb
    }

    open class CallbackPostUserAsyncTask {
        open fun callback(result: String, token: String) {}
    }
}