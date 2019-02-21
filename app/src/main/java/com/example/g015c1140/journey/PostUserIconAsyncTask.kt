package com.example.g015c1140.journey

import android.os.AsyncTask
import org.json.JSONArray
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class PostUserIconAsyncTask : AsyncTask<String, Void, String>() {

    //callBack用
    private var callbackPostUserIconAsyncTask: CallbackPostUserIconAsyncTask? = null
    private lateinit var imageUrl: String

    override fun doInBackground(vararg parameter: String): String {

        if (parameter[0] != "") {
            var connection: HttpURLConnection? = null
            val lineEnd = "\r\n"
            val twoHyphens = "--"
            val boundary = "wwwwwwwboundarywwwwwww"
            var postResult: String
            var httpResult = ""
            val url = URL(Setting().SERVER_IMAGE_POST_URL)

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
                    val file = File(parameter[0])
                    writeBytes(twoHyphens + boundary + lineEnd)
                    writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"${parameter[1]}.jpg\"$lineEnd")
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

                imageUrl = JSONArray(sb.toString()).getString(0)

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
            return "$httpResult:$postResult"
        } else {
            return "NO-IMAGE"
        }
    }

    public override fun onPostExecute(result: String) {
        super.onPostExecute(result)

        when (result) {
            "HTTP-OK:IMAGE送信OK" -> {
                callbackPostUserIconAsyncTask!!.callback("RESULT-OK", imageUrl)
                return
            }

            "NO-IMAGE" -> {
                callbackPostUserIconAsyncTask!!.callback("NO-IMAGE", "")
                return
            }

            else -> {
                callbackPostUserIconAsyncTask!!.callback("RESULT-NG", "")
                return
            }
        }
    }

    fun setOnCallback(cb: CallbackPostUserIconAsyncTask) {
        callbackPostUserIconAsyncTask = cb
    }

    open class CallbackPostUserIconAsyncTask {
        open fun callback(result: String, data: String) {}
    }

}