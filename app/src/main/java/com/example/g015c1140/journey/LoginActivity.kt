package com.example.g015c1140.journey

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString().matches("^[a-zA-Z0-9]+$".toRegex())) {
                source
            } else {
                ""
            }
        }
        val lengthFilter = InputFilter.LengthFilter(20)

        loginIdEditText.filters = arrayOf(inputFilter, lengthFilter)
        loginpassEditText.filters = arrayOf(inputFilter, lengthFilter)
    }

    fun onLoginButtonTapped(v: View) {

        var result = ""
        if (loginIdEditText.text.toString().isEmpty())
            result += "ユーザーIDを入力してください\n"


        if (!(loginpassEditText.text.toString().isEmpty())) {
            //文字ある
            if (loginpassEditText.text.toString().length < 8) {
                //7文字以下
                result += "パスワードは8文字以上で入力してください\n"
            }
        } else {
            result += "パスワードを入力してください\n"
        }

        if (result == "") {
            //合っているかtokenを取得してチェック
            val plat = PostLoginAsyncTask()
            plat.setOnCallback(object : PostLoginAsyncTask.CallbackPostLoginAsyncTask() {
                override fun callback(result: String, token: String) {
                    super.callback(result, token)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test LoginCallback", "非同期処理$result")
                    if (result == "RESULT-OK") {
                        //完了した関数呼び出し
                        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
                        val sharedPrefEditor = sharedPreferences.edit()
                        sharedPrefEditor.putString("token", token)
                        sharedPrefEditor.putString("id",loginIdEditText.text.toString())
                        sharedPrefEditor.putString("password",loginpassEditText.text.toString())
                        sharedPrefEditor.apply()

                        /**************************/
                        val guaat = GetUserAccountAsyncTask(sharedPreferences.getString("id","none"))
                        guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                            override fun callback(resultJSONObject: JSONObject) {
                                super.callback(resultJSONObject)
                                // ここからAsyncTask処理後の処理を記述します。
                                Log.d("test GetUserAccCallback", "非同期処理$resultJSONObject")
                                if (resultJSONObject.getString("result") == "RESULT-OK") {
                                    //完了した関数呼び出し
                                    sharedPrefEditor.putString("name", resultJSONObject.getString("user_name"))
                                    sharedPrefEditor.putString("generation",resultJSONObject.getString("generation") )
                                    sharedPrefEditor.putString("gender",resultJSONObject.getString("gender") )
                                    sharedPrefEditor.putString("comment",resultJSONObject.getString("comment") )
                                    sharedPrefEditor.putString("iconImage",resultJSONObject.getString("user_icon") )
                                    sharedPrefEditor.putString("headerImage",resultJSONObject.getString("user_header") )
                                    sharedPrefEditor.putBoolean("userFlg", true)
                                    sharedPrefEditor.apply()
                                    Toast.makeText(this@LoginActivity,"引継ぎが完了しました",Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    AlertDialog.Builder(this@LoginActivity).apply {
                                        setTitle("引継ぎに失敗しました")
                                        setMessage("もう一度実行してください")
                                        setPositiveButton("確認", null)
                                        show()
                                    }
                                }
                            }
                        })
                        guaat.execute()

                        /**************************/
                    } else {
                        AlertDialog.Builder(this@LoginActivity).apply {
                            setTitle("引継ぎに失敗しました")
                            setMessage("ユーザーIDまたは、パスワードが一致しません")
                            setPositiveButton("確認", null)
                            show()
                        }
                    }
                }
            })
            plat.execute(arrayListOf(loginIdEditText.text.toString(), loginpassEditText.text.toString()))
        }else{
            AlertDialog.Builder(this).apply {
                setTitle("入力情報が間違っています")
                setMessage(result)
                setPositiveButton("確認", null)
                show()
            }
        }
        /********************/
//        startActivity(Intent(this, IndexActivity::class.java))
        //finish()
    }
}
