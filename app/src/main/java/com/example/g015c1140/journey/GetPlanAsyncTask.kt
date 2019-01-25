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

//flg = trueならplan_id falseならuser_id
class GetPlanAsyncTask(id: String, flg: Boolean) : AsyncTask<String, String, String>() {
    private val ID = id
    private val FLG = flg

    //callBack用
    private var callbackGetPlanAsyncTask: CallbackGetPlanAsyncTask? = null
    private var result: String? = null
    private var resultJson: JSONObject? = null

    override fun doInBackground(vararg params: String?): String? {

        if (ID == "") {
            Log.d("test", "GSAT ID-Error")
            return result
        }


        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        try {
            val url = if (FLG) {
                URL("${Setting().PLAN_GET_PID_URL}$ID")
            } else {
                URL("${Setting().PLAN_GET_UID_URL}$ID")
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

                Log.d("test GSAT", "${jsonArray.length()}             ${sb.length}")
                Log.d("test", "array.getJSONObject(0): ${jsonArray.getJSONObject(0)}")
                result = "OK"

                resultJson = if (FLG) {
                    jsonArray.getJSONObject(0)
                } else {
                    jsonArray.getJSONObject( jsonArray.length() - 1 )
                }

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
            Log.d("test GetPlanTask", "return null")
            callbackGetPlanAsyncTask!!.callback(JSONObject().put("result", "RESULT-NG"))
            return
        }

        resultJson!!.put("result", "RESULT-OK")
        Log.d("test GetPlanTask", "result：$result")
        callbackGetPlanAsyncTask!!.callback(resultJson!!)
    }

    fun setOnCallback(cb: CallbackGetPlanAsyncTask) {
        callbackGetPlanAsyncTask = cb
    }

    open class CallbackGetPlanAsyncTask {
        open fun callback(resultPlanJson: JSONObject) {}
    }

}