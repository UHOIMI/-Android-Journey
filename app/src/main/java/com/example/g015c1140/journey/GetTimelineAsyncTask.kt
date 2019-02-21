package com.example.g015c1140.journey

import android.os.AsyncTask
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class GetTimelineAsyncTask(area: String, offset: Int) : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackGetTimelineAsyncTask: CallbackGetTimelineAsyncTask? = null
    private var result: String? = null

    private val OFFSET = offset
    private val AREA = area
    private var timelineRecord: JSONArray? = null

    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var limit: String? = null
        var userId: String? = null

        for (_cnt in 0 until params.size) {
            when (_cnt) {
                0 -> limit = params[_cnt]
                1 -> userId = params[_cnt]
            }
        }

        try {

            val url = if (AREA == "") {
                var url = "${Setting().TIMELINE_GET_URL}$OFFSET"
                if (limit != null) {
                    url = "$url&limit=$limit"
                }
                if (userId != null) {
                    url = "$url&user_id=$userId"
                }
                URL(url)
            } else {
                URL("${Setting().TIMELINE_GET_URL}$OFFSET&$AREA")
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
                sb.toString()
                val jsonObject = JSONObject(sb.toString())
                if (jsonObject.getString("status").toString() != "200") {
                    if (jsonObject.getString("status").toString() == "404") {
                        result = "404"
                        return result
                    }
                    result = null
                    return result
                }
                timelineRecord = jsonObject.getJSONArray("record")
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
            callbackGetTimelineAsyncTask!!.callback("RESULT-NG", null)
            return
        }

        if (result == "404") {
            callbackGetTimelineAsyncTask!!.callback("RESULT-404", null)
        } else {
            callbackGetTimelineAsyncTask!!.callback("RESULT-OK", timelineRecord)
        }
    }

    fun setOnCallback(cb: CallbackGetTimelineAsyncTask) {
        callbackGetTimelineAsyncTask = cb
    }

    open class CallbackGetTimelineAsyncTask {
        open fun callback(result: String, timelineRecordJsonArray: JSONArray?) {}
    }
}