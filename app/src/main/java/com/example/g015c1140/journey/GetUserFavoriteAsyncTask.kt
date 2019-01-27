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

class GetUserFavoriteAsyncTask(uId: String, offset: Int) : AsyncTask<Void, String, String>() {

    //callBack用
    private var callbackGetUserFavoriteAsyncTask: CallbackGetUserFavoriteAsyncTask? = null
    private var result: String? = null
    private val OFFSET = offset

    private val USER_ID = uId

    val CONVERT_JSON_ARRAY = JSONArray()
    lateinit var convertJsonObject: JSONObject

    override fun doInBackground(vararg void: Void): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        if (USER_ID == "") {
            Log.d("test", "USER_ID-Error")
            return result
        }

        try {
            val url = URL("${Setting().FAVORITE_GET_UID_URL}$USER_ID&offset=$OFFSET")

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
                val resultJsonObject = JSONObject(sb.toString())

                when {
                    resultJsonObject.getString("status") == "200" -> {
                        val resultJsonArray = resultJsonObject.getJSONArray("record")

                        var jsonObject:JSONObject
                        var jsonPlan:JSONObject
                        var jsonSpot:JSONArray

                        for (_jaCnt in 0 until resultJsonArray.length()){
                            jsonObject = resultJsonArray[_jaCnt] as JSONObject
                            jsonPlan = jsonObject.getJSONObject("plan")
                            jsonSpot = jsonObject.getJSONArray("spots")

                            convertJsonObject = JSONObject()
                            convertJsonObject.put("plan_date", jsonPlan.getString("plan_date"))
                            convertJsonObject.put("plan_id", jsonObject.getString("plan_id"))
                            convertJsonObject.put("user_id", jsonPlan.getString("user_id"))
                            convertJsonObject.put("plan_title", jsonPlan.getString("plan_title"))
                            convertJsonObject.put("plan_comment", jsonPlan.getString("plan_comment"))
                            convertJsonObject.put("transportation", jsonPlan.getString("transportation"))
                            convertJsonObject.put("price", jsonPlan.getString("price"))
                            convertJsonObject.put("area", jsonPlan.getString("area"))
                            convertJsonObject.put("spots", jsonSpot)
                            CONVERT_JSON_ARRAY.put(convertJsonObject)
                        }
                        result = "favorite-yes"
                    }

                    resultJsonObject.getString("status") == "404" -> {
                        result = "favorite-no"
                    }

                    else -> {
                        result = null
                        return result
                    }
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
            Log.d("test GetUserIdTask", "return null")
            callbackGetUserFavoriteAsyncTask!!.callback("RESULT-NG",null)
            return
        }

        Log.d("test GetUserIdTask", "result：$result")

        if (result == "favorite-yes"){
            callbackGetUserFavoriteAsyncTask!!.callback("RESULT-OK", CONVERT_JSON_ARRAY)
        }else{
            callbackGetUserFavoriteAsyncTask!!.callback("RESULT-404",null)
        }
    }

    fun setOnCallback(cb: CallbackGetUserFavoriteAsyncTask) {
        callbackGetUserFavoriteAsyncTask = cb
    }

    open class CallbackGetUserFavoriteAsyncTask{
        open fun callback(result: String,favoriteRecordJSONArray: JSONArray?) {}
    }
}