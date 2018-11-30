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

class GetSpotAsyncTask(sCnt: Int, id: ArrayList<ArrayList<String>>, idFlg: Boolean) : AsyncTask<String, String, String>() {
    private val SPOT_LIST_CNT = sCnt
    private val ID_LIST = id
    private val SPOT_ID_FLG = idFlg
    private val SPOT_LIST = ArrayList<ArrayList<JSONObject>>()


    //callBack用
    private var callbackGetSpotAsyncTask: CallbackGetSpotAsyncTask? = null
    private var result: String? = null

    override fun doInBackground(vararg params: String?): String? {

        for (arrayList in ID_LIST) {
            for (id in arrayList) {
                if (id == "") {
                    Log.d("test", "GSAT ID-Error")
                    return result
                }
            }
        }


        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        for (_idCnt in 0 until ID_LIST.size){
            SPOT_LIST.add(arrayListOf())
            for (_valueCnt in 0 until ID_LIST[_idCnt].size){
                try {
                    val url = if (SPOT_ID_FLG) {
                        URL("${Setting().SPOT_GET_URL}${ID_LIST[_idCnt][_valueCnt]}")
                    } else {
                        URL("${Setting().SPOT_GET_ID_URL}${ID_LIST[_idCnt][_valueCnt]}")
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
                        for (_spotCnt in 0 until jsonArray.length()) {
                            Log.d("test", "array.getJSONObject(cnt): ${jsonArray.getJSONObject(_spotCnt)}")
                            SPOT_LIST[_idCnt].add(jsonArray.getJSONObject(_spotCnt))
                        }
                        result = ID_LIST[_idCnt][_valueCnt]
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
        }
        return result
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (SPOT_ID_FLG){

            if (result == null) {
                Log.d("test GetSpotTask", "return null")
                callbackGetSpotAsyncTask!!.callback(arrayListOf( arrayListOf(JSONObject().put("result", "RESULT-NG")) ), null, SPOT_ID_FLG)
                return
            }

            SPOT_LIST.add(arrayListOf( JSONObject().put("result", "RESULT-OK")))
            Log.d("test GetSpotTask", "result：$result")
            callbackGetSpotAsyncTask!!.callback(SPOT_LIST, null, SPOT_ID_FLG)

        }else {

            if (result == null) {
                Log.d("test GetSpotTask", "return null")
                callbackGetSpotAsyncTask!!.callback(null, arrayListOf("RESULT-NG"), SPOT_ID_FLG)
                return
            }

            Log.d("test GetSpotTask", "result：$result")
            val apiSpotIdList = arrayListOf<String>()
            SPOT_LIST.forEach {
                it.forEach { value->
                    apiSpotIdList.add(value.getString("spot_id"))
                }
            }

            val spotIdList = arrayListOf<String>()
            spotIdList.add("RESULT-OK")

            for (backCnt in (apiSpotIdList.size - SPOT_LIST_CNT) until apiSpotIdList.size) {
                spotIdList.add(apiSpotIdList[backCnt])
            }

            callbackGetSpotAsyncTask!!.callback(null, spotIdList, SPOT_ID_FLG)
        }
    }

    fun setOnCallback(cb: CallbackGetSpotAsyncTask) {
        callbackGetSpotAsyncTask = cb
    }

    open class CallbackGetSpotAsyncTask {
        open fun callback(resultSpotJsonList: ArrayList<ArrayList<JSONObject>>?, resultArrayList: ArrayList<String>?, resultIdFlg: Boolean) {}
    }

}