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

class GetPlanFavoriteAsyncTask(pIdList: ArrayList<String>, uId: String) : AsyncTask<Void, String, String>() {

    //callBack用
    private var callbackGetPlanFavoriteAsyncTask: CallbackGetPlanFavoriteAsyncTask? = null
    private var result: String? = null
    private val PLAN_ID_LIST = pIdList
    private val USER_ID = uId
    private val FAVORITE_LIST = arrayListOf<String>()

    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        for (id in PLAN_ID_LIST) {
            if (id == "") {
                Log.d("test", "PLAN_ID-Error")
                return result
            }
        }

        PLAN_ID_LIST.forEach {
            try {
                val url = if (USER_ID != "") {
                    URL(Setting().FAVORITE_GET_PUID_URL.format(it, USER_ID))
                } else {
                    URL("${Setting().FAVORITE_GET_COUNT_URL}$it")
                }

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
                        jsonObject.getString("status")
                        if ((USER_ID != "") && (jsonObject.getString("status") == "404")) {
                            FAVORITE_LIST.add("favorite-no")
                            result = "favorite-no"
                        } else {
                            result = null
                            return result
                        }
                    } else if (USER_ID != "") {
                        FAVORITE_LIST.add("favorite-yes")
                        result = "favorite-yes"
                    }else{
                        FAVORITE_LIST.add(jsonObject.getJSONObject("record").getString("count"))
                        result = it
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
        }

        return result
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        if (result == null) {
            Log.d("test GetUserIdTask", "return null")
            callbackGetPlanFavoriteAsyncTask!!.callback(arrayListOf("RESULT-NG"))
            return
        }

        Log.d("test GetUserIdTask", "result：$result")
        FAVORITE_LIST.add("RESULT-OK")
        callbackGetPlanFavoriteAsyncTask!!.callback(FAVORITE_LIST)
    }

    fun setOnCallback(cb: CallbackGetPlanFavoriteAsyncTask) {
        callbackGetPlanFavoriteAsyncTask = cb
    }

    open class CallbackGetPlanFavoriteAsyncTask {
        open fun callback(resultFavoriteArrayList: ArrayList<String>) {}
    }
}