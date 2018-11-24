package com.example.g015c1140.journey

import android.os.AsyncTask
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class PutUserAsyncTask : AsyncTask<MutableList<MutableList<String>>, String, String>() {

    //callBack用
    private var callbackPutUserAsyncTask: CallbackPutUserAsyncTask? = null

    //insert
    override fun doInBackground(vararg params: MutableList<MutableList<String>>?): String? {

        //ここでAPIを叩きます。バックグラウンドで処理する内容です。
        var connection: HttpURLConnection? = null
        var postResult: String? = null
        var httpResult: String? = null
        val url = URL(Setting().USER_PUT_URL)

        val userDataPutList = mutableListOf(mutableListOf<String>())
        userDataPutList.removeAt(0)

        if (params[0] == null) {
            println("PostSpot 引数異常URL：$params[0]")
            return "引数異常　URL"
        }else{
            for (list in params[0]!!.iterator()) {
                for (_dataCnt in 0 until list.size) {
                    if (list[_dataCnt] == "") {
                        println("PostSpot 引数異常URL：$params[0]")
                        return "引数異常　URL"
                    }
                }
                userDataPutList.add(mutableListOf(list[0],list[1]))
            }
        }
        userDataPutList[0][0] = userDataPutList[0][0].replace("&","")

        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.instanceFollowRedirects = false
            connection.doOutput = true
            connection.connect()  //ここで指定したAPIを叩いてみてます。

            var out: OutputStream? = null
            try {
                out = connection.outputStream

                for (dataList in userDataPutList    ){
                            out.write( ( "${dataList[0]}=${dataList[1]}" ).toByteArray() )
                }
                out.flush()
                Log.d("test", "flush")

                val `is` = connection.inputStream
                val bReader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                val sb = StringBuilder()
                for (line in bReader.readLines()) {
                    line.run { sb.append(line) }
                }
                bReader.close()
                `is`.close()
                postResult = JSONObject(sb.toString()).getString("status")

            } catch (e: IOException) {
                // POST送信エラー
                e.printStackTrace()
                postResult = "USER_PUT-error：　"
            } finally {
                out?.close()
            }

            val status = connection.responseCode
            httpResult = when (status) {
                HttpURLConnection.HTTP_OK -> "HTTP-OK"
                else -> "status=$status"
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }

        return "$httpResult:$postResult"
        //finallyで接続を切断してあげましょう。
    }

    //返ってきたデータをビューに反映させる処理はonPostExecuteに書きます。これはメインスレッドです。
    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)

        Log.d("test PostSpot", "onPostEx: $result")
        when (result) {
            "HTTP-OK:200" -> {
                Log.d("test PostSpot", "HTTP-OK")
                callbackPutUserAsyncTask!!.callback("RESULT-OK")
                return
            }

            else -> {
                Log.d("test PostSpot", "HTTP-NG")
                callbackPutUserAsyncTask!!.callback("RESULT-NG")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPutUserAsyncTask) {
        callbackPutUserAsyncTask = cb
    }

    open class CallbackPutUserAsyncTask {
        open fun callback(result: String) {}
    }

}