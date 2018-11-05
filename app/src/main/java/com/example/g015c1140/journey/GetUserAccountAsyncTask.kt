package com.example.g015c1140.journey

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class GetUserAccountAsyncTask(i: String) : AsyncTask<Void, String, String>() {

    //callBack用
    private var callbackGetUserAccountAsyncTask: CallbackGetUserAccountAsyncTask? = null
    private var result: String? = null
    private val USER_ID = i


    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        if (USER_ID =="none")
            Log.d("test", "USERID-Error")

        try {
            val url = URL("${Setting().USER_ACCOUNT_GET_URL}$USER_ID")
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
                val userData = jsonObject.getJSONArray("record").getJSONObject(0)

                Log.d("test GUDAT", "$userData             ${sb.length}")
                result = userData.toString()
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
            callbackGetUserAccountAsyncTask!!.callback(JSONObject().put("result","RESULT-NG"))
            return
        }

        val resultJSONObject = JSONObject(result)
        resultJSONObject.put("result","RESULT-OK")
        Log.d("test GetUserIdTask", "result：$result")
        callbackGetUserAccountAsyncTask!!.callback(resultJSONObject)
    }

    fun setOnCallback(cb: CallbackGetUserAccountAsyncTask) {
        callbackGetUserAccountAsyncTask = cb
    }

    open class CallbackGetUserAccountAsyncTask {
        open fun callback(resultJSONObject: JSONObject) {}
    }
}