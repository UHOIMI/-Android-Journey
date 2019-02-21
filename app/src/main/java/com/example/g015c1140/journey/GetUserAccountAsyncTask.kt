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

class GetUserAccountAsyncTask(idList: ArrayList<String>) : AsyncTask<Void, String, String>() {
    private val USER_ID_LIST = idList
    private val ACCOUNT_LIST = arrayListOf<JSONObject>()

    //callBack用
    private var callbackGetUserAccountAsyncTask: CallbackGetUserAccountAsyncTask? = null
    private var result: String? = null

    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        for (id in USER_ID_LIST) {
            if (id == "") {
                return result
            }
        }

        USER_ID_LIST.forEach {
            try {
                val url = URL("${Setting().USER_ACCOUNT_GET_URL}$it")
                connection = url.openConnection() as HttpURLConnection
                connection!!.connect()  //ここで指定したAPIを叩いてみてます。

                //ここから叩いたAPIから帰ってきたデータを使えるよう処理していきます。

                val br = BufferedReader(InputStreamReader(connection!!.inputStream))
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

                    ACCOUNT_LIST.add( jsonObject.getJSONArray("record").getJSONObject(0) )

                    result = it

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
        }
        return result
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result == null) {
            callbackGetUserAccountAsyncTask!!.callback(arrayListOf( JSONObject().put("result", "RESULT-NG") ))
            return
        }

        ACCOUNT_LIST.add( JSONObject().put("result", "RESULT-OK") )
        callbackGetUserAccountAsyncTask!!.callback(ACCOUNT_LIST)
    }

    fun setOnCallback(cb: CallbackGetUserAccountAsyncTask) {
        callbackGetUserAccountAsyncTask = cb
    }

    open class CallbackGetUserAccountAsyncTask {
        open fun callback(resultUserAccountList: ArrayList<JSONObject>) {}
    }
}