package com.example.g015c1140.journey

import android.os.AsyncTask
import org.json.JSONArray
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class PostImageAsyncTask : AsyncTask<MutableList<String>, Void, String>() {

    //callBack用
    private var callbackPostImageAsyncTask: CallbackPostImageAsyncTask? = null
    private lateinit var imageList: MutableList<String>

    override fun doInBackground(vararg parameter: MutableList<String>): String {

        imageList = parameter[0]

        var connection: HttpURLConnection? = null
        val lineEnd = "\r\n"
        val twoHyphens = "--"
        val boundary = "wwwwwwwboundarywwwwwww"
        var postResult = ""
        var httpResult = ""
        val url = URL(Setting().SERVER_IMAGE_POST_URL)

        for (_listCnt in 0 until imageList.size step 3) {
            try {
                connection = url.openConnection() as HttpURLConnection
                connection.run {
                    requestMethod = "POST"//HTTPのメソッドをPOSTに設定する。
                    setRequestProperty("Connection", "Keep-Alive")
                    setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
                    setRequestProperty("Accept-Charset", "UTF-8")
                    doInput = true//リクエストのボディ送信を許可する
                    doOutput = true//レスポンスのボディ受信を許可する
                    useCaches = false//キャッシュを使用しない
                    connect()
                }

                // データを投げる
                val dos = DataOutputStream(connection.outputStream)

                dos.run {
                    var file: File
                    for (_imageCnt in _listCnt until _listCnt + 3) {
                        if (imageList[_imageCnt] != "") {
                            file = File(imageList[_imageCnt])
                            writeBytes(twoHyphens + boundary + lineEnd)
                            writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$_imageCnt.jpg\"$lineEnd")
                            writeBytes("Content-Type: image/jpeg$lineEnd")
                            writeBytes("Content-Transfer-Encoding: binary$lineEnd")
                            writeBytes(lineEnd)
                            flush()

                            val fis = FileInputStream(file)

                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            while (true) {
                                bytesRead = fis.read(buffer)
                                if (bytesRead != -1) {
                                    write(buffer, 0, bytesRead)
                                } else {
                                    break
                                }
                            }
                            fis.close()
                            flush()
                            writeBytes(lineEnd)
                            flush()
                        }
                    }
                    //終わるときに必要↓
                    writeBytes(lineEnd)
                    writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                    flush()
                }
                dos.close()

                // データを受け取る
                val `is` = connection.inputStream
                val bReader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                val sb = StringBuilder()

                for (line in bReader.readLines()) {
                    line.run { sb.append(line) }
                }
                bReader.close()
                `is`.close()
                postResult = "IMAGE送信OK"

                val jsonArray = JSONArray(sb.toString())
                var jsonCnt = 0
                val jsonMaxCnt = jsonArray.length()
                for (_imageCnt in _listCnt until _listCnt + 3) {
                    if (imageList[_imageCnt] != "") {
                        imageList[_imageCnt] = jsonArray.getString(jsonCnt)
                        if (jsonCnt == jsonMaxCnt - 1) {
                            break
                        }
                        jsonCnt++
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                postResult = "IMAGE送信エラー：　"
            } finally {
                if (connection != null) {
                    val status = connection.responseCode
                    when (status) {
                        HttpURLConnection.HTTP_OK -> httpResult = "HTTP-OK"
                        else -> httpResult = "status=$status"
                    }
                    connection.disconnect()
                }
            }
            if ("$httpResult:$postResult" != "HTTP-OK:IMAGE送信OK")
                break
        }
        return "$httpResult:$postResult"
    }

    public override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        when (result) {
            "HTTP-OK:IMAGE送信OK" -> {
                callbackPostImageAsyncTask!!.callback("RESULT-OK", imageList.toString())
                return
            }

            else -> {
                callbackPostImageAsyncTask!!.callback("RESULT-NG", "")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostImageAsyncTask) {
        callbackPostImageAsyncTask = cb
    }

    open class CallbackPostImageAsyncTask {
        open fun callback(result: String, data: String) {}
    }

}