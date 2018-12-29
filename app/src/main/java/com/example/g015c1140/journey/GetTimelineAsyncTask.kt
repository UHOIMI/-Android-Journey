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

class GetTimelineAsyncTask(area: String, ofset:Int) : AsyncTask<String, String, String>() {

    //callBack用
    private var callbackGetTimelineAsyncTask: CallbackGetTimelineAsyncTask? = null
    private var result: String? = null

    private val OFSET = ofset
    private val AREA = area
    private var timelineRecord: JSONArray? = null
    private val PLAN_ID_LIST = arrayListOf<String>()
    private val PLAN_USER_ID_LIST = arrayListOf<String>()
    private val PLAN_SPOT_ID_LIST = arrayListOf<ArrayList<String>>()
    private val PLAN_SPOT_CNT_LIST = arrayListOf<Int>()


    override fun doInBackground(vararg params: String?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null

        try {

            val url = if(AREA == ""){
                URL("${Setting().TIMELINE_GET_URL}$OFSET")
            }else{
                URL("${Setting().TIMELINE_GET_URL}$OFSET&$AREA")
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
                    Log.d("test", "Timeline error")
                    result = null
                    return result
                }

                timelineRecord = jsonObject.getJSONArray("record")
                result = "OK"
            } catch (e: JSONException) {
                e.printStackTrace()
            }

/*
            for (_timelineCnt in 0 until timelineRecord!!.length()) {
                val timelineValue = JSONObject(timelineRecord!![_timelineCnt].toString())
                //spot cnt

                val valueList = arrayListOf<String>()
                var valueCnt = 0

                if (timelineValue.getString("spot_id_a") != "null") {
                    valueList.add(timelineValue.getString("spot_id_a"))
                    valueCnt += 1

                    if (timelineValue.getString("spot_id_b") != "null") {
                        valueList.add(timelineValue.getString("spot_id_b"))
                        valueCnt += 1

                        if (timelineValue.getString("spot_id_c") != "null") {
                            valueList.add(timelineValue.getString("spot_id_c"))
                            valueCnt += 1

                            if (timelineValue.getString("spot_id_d") != "null") {
                                valueList.add(timelineValue.getString("spot_id_d"))
                                valueCnt += 1

                                if (timelineValue.getString("spot_id_e") != "null") {
                                    valueList.add(timelineValue.getString("spot_id_e"))
                                    valueCnt += 1

                                    if (timelineValue.getString("spot_id_f") != "null") {
                                        valueList.add(timelineValue.getString("spot_id_f"))
                                        valueCnt += 1

                                        if (timelineValue.getString("spot_id_g") != "null") {
                                            valueList.add(timelineValue.getString("spot_id_g"))
                                            valueCnt += 1

                                            if (timelineValue.getString("spot_id_h") != "null") {
                                                valueList.add(timelineValue.getString("spot_id_h"))
                                                valueCnt += 1

                                                if (timelineValue.getString("spot_id_i") != "null") {
                                                    valueList.add(timelineValue.getString("spot_id_i"))
                                                    valueCnt += 1

                                                    if (timelineValue.getString("spot_id_j") != "null") {
                                                        valueList.add(timelineValue.getString("spot_id_j"))
                                                        valueCnt += 1

                                                        if (timelineValue.getString("spot_id_k") != "null") {
                                                            valueList.add(timelineValue.getString("spot_id_k"))
                                                            valueCnt += 1

                                                            if (timelineValue.getString("spot_id_l") != "null") {
                                                                valueList.add(timelineValue.getString("spot_id_l"))
                                                                valueCnt += 1

                                                                if (timelineValue.getString("spot_id_m") != "null") {
                                                                    valueList.add(timelineValue.getString("spot_id_m"))
                                                                    valueCnt += 1

                                                                    if (timelineValue.getString("spot_id_n") != "null") {
                                                                        valueList.add(timelineValue.getString("spot_id_n"))
                                                                        valueCnt += 1

                                                                        if (timelineValue.getString("spot_id_o") != "null") {
                                                                            valueList.add(timelineValue.getString("spot_id_o"))
                                                                            valueCnt += 1

                                                                            if (timelineValue.getString("spot_id_p") != "null") {
                                                                                valueList.add(timelineValue.getString("spot_id_p"))
                                                                                valueCnt += 1

                                                                                if (timelineValue.getString("spot_id_q") != "null") {
                                                                                    valueList.add(timelineValue.getString("spot_id_q"))
                                                                                    valueCnt += 1

                                                                                    if (timelineValue.getString("spot_id_r") != "null") {
                                                                                        valueList.add(timelineValue.getString("spot_id_r"))
                                                                                        valueCnt += 1

                                                                                        if (timelineValue.getString("spot_id_s") != "null") {
                                                                                            valueList.add(timelineValue.getString("spot_id_s"))
                                                                                            valueCnt += 1

                                                                                            if (timelineValue.getString("spot_id_t") != "null") {
                                                                                                valueList.add(timelineValue.getString("spot_id_t"))
                                                                                                valueCnt += 1
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


                PLAN_SPOT_ID_LIST.add(valueList)
                PLAN_SPOT_CNT_LIST.add(valueCnt)

                //favorite
                PLAN_ID_LIST.add(timelineValue.getString("plan_id"))

                //user name icon
                PLAN_USER_ID_LIST.add(timelineValue.getString("user_id"))
            }
*/

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
            callbackGetTimelineAsyncTask!!.callback("RESULT-NG",null)
            return
        }


        Log.d("test GettimelineTask", "result：$result")
        callbackGetTimelineAsyncTask!!.callback("RESULT-OK",timelineRecord)
    }

    fun setOnCallback(cb: CallbackGetTimelineAsyncTask) {
        callbackGetTimelineAsyncTask = cb
    }

    open class CallbackGetTimelineAsyncTask {
        open fun callback(result: String, timelineRecordJsonArray: JSONArray?) {}
    }
}