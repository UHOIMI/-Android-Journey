package com.example.g015c1140.journey

import android.os.AsyncTask
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class GetSpotAsyncTask(id: String, idFlg: Boolean) : AsyncTask<String, String, String>() {
    private val ID = id
    private val SPOT_ID_FLG = idFlg
    private val RESULT_LIST = ArrayList<JSONObject>()


    //callBack用
    private var callbackGetSpotAsyncTask: CallbackGetSpotAsyncTask? = null
    private var result: String? = null

    override fun doInBackground(vararg params: String?): String? {

        if (ID == "") {
            return result
        }


        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        try {
            val url = if (SPOT_ID_FLG) {
                URL("${Setting().SPOT_GET_PID_URL}$ID")
            } else {
                URL("${Setting().SPOT_GET_SID_URL}$ID")
            }

            connection = url.openConnection() as HttpURLConnection
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

            val br = BufferedReader(InputStreamReader(connection.inputStream))
            val sb = StringBuilder()
            for (line in br.readLines()) {
                line.run { sb.append(line) }
            }
            br.close()

            try {
                val jsonObject = JSONObject(sb.toString())
                if (jsonObject.getString("status") != "200") {
                    result = null
                    return result
                }

                val jsonArray = jsonObject.getJSONArray("record")

                for (_spotCnt in 0 until jsonArray.length()) {
                    RESULT_LIST.add(jsonArray.getJSONObject(_spotCnt))
                }
                result = "OK"

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
            callbackGetSpotAsyncTask!!.callback(arrayListOf(JSONObject().put("result", "RESULT-NG")), SPOT_ID_FLG)
            return
        }

        RESULT_LIST.add(JSONObject().put("result", "RESULT-OK"))
        callbackGetSpotAsyncTask!!.callback(RESULT_LIST, SPOT_ID_FLG)
    }

    fun setOnCallback(cb: CallbackGetSpotAsyncTask) {
        callbackGetSpotAsyncTask = cb
    }

    open class CallbackGetSpotAsyncTask {
        open fun callback(resultSpotJsonList: ArrayList<JSONObject>?, resultIdFlg: Boolean) {}
    }
}