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

class GetSearchAsyncTask(keyword: String, generation: String, area: String, price: String, transportation: String, ofs: Int) : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackGetSearchAsyncTask: CallbackGetSearchAsyncTask? = null
    private var result: String? = null
    private val OFFSET = ofs

    private val KEYWORD = keyword
    private val GENERATION = generation
    private val AREA = area
    private val PRICE = price
    private val TRANSPOTTATION = transportation

    private var searchRecord: JSONArray? = null

    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var limit: String? = null

        if (params.isNotEmpty()) {
            limit = params[0]
        }


        try {

            var urlString = Setting().SEARCH_GET_URL

            if (KEYWORD != "") {
                urlString += "keyword=$KEYWORD&"
            }
            if (GENERATION != "") {
                urlString += "generation=$GENERATION&"
            }
            if (AREA != "") {
                urlString += "area=$AREA&"
            }
            if (PRICE != "") {
                urlString += "price=$PRICE&"
            }
            if (TRANSPOTTATION != "") {
                urlString += "transportation=$TRANSPOTTATION&"
            }
            if (limit != null){
                urlString += "limit=$limit&"
            }

            val url = URL("${urlString}offset=$OFFSET")

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
                if (jsonObject.getString("status").toString() == "404") {
                    result = "404"
                    searchRecord = JSONArray()
                    return result
                } else if (jsonObject.getString("status").toString() != "200") {
                    Log.d("test", "Timeline error")
                    result = null
                    return result
                }
                searchRecord = jsonObject.getJSONArray("record")
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
            Log.d("test GetSearchTask", "return null")
            callbackGetSearchAsyncTask!!.callback("RESULT-NG", null)
            return
        }

        if (result == "404") {
            callbackGetSearchAsyncTask!!.callback("RESULT-404", searchRecord)
            return
        }

        Log.d("test GetSearchTask", "result：$result")
        callbackGetSearchAsyncTask!!.callback("RESULT-OK", searchRecord)
    }

    fun setOnCallback(cb: CallbackGetSearchAsyncTask) {
        callbackGetSearchAsyncTask = cb
    }

    open class CallbackGetSearchAsyncTask {
        open fun callback(result: String, searchRecordJsonArray: JSONArray?) {}
    }
}