package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
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

        val inputFilter = InputFilter { source, _, _, _, _, _ ->
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
/*
            if (loginpassEditText.text.toString().length < 8) {
                //7文字以下
                result += "パスワードは8文字以上で入力してください\n"
            }
*/
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
                        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
                        val sharedPrefEditor = sharedPreferences.edit()
                        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_TOKEN, token)
                        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ID, loginIdEditText.text.toString())
                        sharedPrefEditor.putString(Setting().USER_SHARED_PREF_PASSWORD, loginpassEditText.text.toString())
                        sharedPrefEditor.apply()

                        /**************************/
                        val guaat = GetUserAccountAsyncTask(arrayListOf( sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, "") ))
                        guaat.setOnCallback(object : GetUserAccountAsyncTask.CallbackGetUserAccountAsyncTask() {
                            override fun callback(resultUserAccountList: ArrayList<JSONObject>) {
                                super.callback(resultUserAccountList)
                                // ここからAsyncTask処理後の処理を記述します。
                                Log.d("test GetUserAccCallback", "非同期処理${resultUserAccountList[resultUserAccountList.size - 1]}")

                                if (resultUserAccountList[resultUserAccountList.size - 1].getString("result") == "RESULT-OK") {
                                    //完了した関数呼び出し
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_NAME, resultUserAccountList[0].getString("user_name"))
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENERATION, resultUserAccountList[0].getString("generation"))
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_GENDER, resultUserAccountList[0].getString("gender"))
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_COMMENT, resultUserAccountList[0].getString("comment"))
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_ICONIMAGE, resultUserAccountList[0].getString("user_icon"))
                                    sharedPrefEditor.putString(Setting().USER_SHARED_PREF_HEADERIMAGE, resultUserAccountList[0].getString("user_header"))
                                    sharedPrefEditor.putBoolean(Setting().USER_SHARED_PREF_FLG, true)
                                    sharedPrefEditor.apply()
                                    Toast.makeText(this@LoginActivity, "引継ぎが完了しました", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity,StartActivity::class.java))
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
                            setMessage("ユーザーIDまたは、パスワードが一致していません")
                            setPositiveButton("確認", null)
                            show()
                        }
                    }
                }
            })
            plat.execute(arrayListOf(loginIdEditText.text.toString(), loginpassEditText.text.toString()))
        } else {
            AlertDialog.Builder(this).apply {
                setTitle("入力情報が間違っています")
                setMessage(result)
                setPositiveButton("確認", null)
                show()
            }
        }
        /********************/
    }
}
