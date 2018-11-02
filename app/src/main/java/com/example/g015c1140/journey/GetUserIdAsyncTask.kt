package com.example.g015c1140.journey

import android.os.AsyncTask
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class GetUserIdAsyncTask : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackGetUserIdAsyncTask: CallbackGetUserIdAsyncTask? = null
    private var result: String? = null

    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        try {
            val url = URL(Setting().USER_GET_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

            val br = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            for (line in br.readLines()) {
                line.run { sb.append(line) }
            }
            br.close()

            val userIdList = ArrayList<String>()

            try {
                val jsonObject = JSONObject(sb.toString())
                val jsonArray = jsonObject.getJSONArray("record")

                Log.d("test GUIAT", "${jsonArray.length()}             ${sb.length}")
                for (i in 0 until jsonArray.length()) {
                    Log.d("test", "array.getJSONObject(i): ${jsonArray.getJSONObject(i)}")
                    userIdList.add(jsonArray.getJSONObject(i).getString("user_id"))
                }
                result = userIdList.toString()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            //ここから下は、接続エラーとかJSONのエラーとかで失敗した時にエラーを処理する為のものです。
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        //finallyで接続を切断してあげましょう。
        finally {
            connection?.disconnect()
        }
        //失敗した時はnullやエラーコードなどを返しましょう。
        return result
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result == null) {
            Log.d("test GetUserIdTask", "return null")
            callbackGetUserIdAsyncTask!!.callback(arrayListOf("RESULT-NG"))
            return
        }

        val resultJSONArray = JSONArray(result)
        Log.d("test GetUserIdTask", "result：$result")

        val userIdList = arrayListOf("RESULT-OK")
        for (_jsonCnt in 0 until resultJSONArray.length()) {
            try {
                userIdList.add(resultJSONArray.getString(_jsonCnt))
            } catch (e: JSONException) {
                e.printStackTrace()
                userIdList.add("")
            }
        }
        callbackGetUserIdAsyncTask!!.callback(userIdList)
    }

    fun setOnCallback(cb: CallbackGetUserIdAsyncTask) {
        callbackGetUserIdAsyncTask = cb
    }

    open class CallbackGetUserIdAsyncTask {
        open fun callback(resultArray: ArrayList<String>) {}
    }
}