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

class GetSpotAsyncTask(cnt: Int, i: String) : AsyncTask<String, String, String>() {
    private val SPOT_LIST_CNT = cnt
    private val USER_ID = i


    //callBack用
    private var callbackGetSpotAsyncTask: CallbackGetSpotAsyncTask? = null
    private var result: String? = null

    override fun doInBackground(vararg params: String?): String? {

        if (USER_ID == "none")
            Log.d("test", "USERID-Error")

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        try {
            val url = URL("${Setting().SPOT_GET_URL}$USER_ID")
            connection = url.openConnection() as HttpURLConnection
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

            val br = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            for (line in br.readLines()) {
                line.run { sb.append(line) }
            }
            br.close()

            val spotIdList = ArrayList<String>()

            try {
                val jsonObject = JSONObject(sb.toString())
                val jsonArray = jsonObject.getJSONArray("record")

                Log.d("test GSAT", "${jsonArray.length()}             ${sb.length}")
                for (i in 0 until jsonArray.length()) {
                    Log.d("test", "array.getJSONObject(i): ${jsonArray.getJSONObject(i)}")
                    spotIdList.add(jsonArray.getJSONObject(i).getString("spot_id"))
                }
                result = spotIdList.toString()
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
            Log.d("test GetSpotTask", "return null")
            callbackGetSpotAsyncTask!!.callback(arrayListOf("RESULT-NG"))
            return
        }

        val resultJSONArray = JSONArray(result)
        Log.d("test GetSpotTask", "result：$result")

        val spotIdList = arrayListOf<String>()
        spotIdList.add("RESULT-OK")

        for (backCnt in (resultJSONArray.length() - SPOT_LIST_CNT) until resultJSONArray.length()) {
            spotIdList.add(resultJSONArray.getString(backCnt))
        }

        callbackGetSpotAsyncTask!!.callback(spotIdList)
    }

    fun setOnCallback(cb: CallbackGetSpotAsyncTask) {
        callbackGetSpotAsyncTask = cb
    }

    open class CallbackGetSpotAsyncTask {
        open fun callback(result: ArrayList<String>) {}
    }

}