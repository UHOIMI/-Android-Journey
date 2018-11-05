package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast

class IndexActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)

        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("userFlg", false)) {
            val plat = PostLoginAsyncTask()
            plat.setOnCallback(object : PostLoginAsyncTask.CallbackPostLoginAsyncTask() {
                override fun callback(result: String, token: String) {
                    super.callback(result,token)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test LoginCallback", "非同期処理$result")
                    if (result == "RESULT-OK") {
                        //完了した関数呼び出し
                        val sharedPrefEditor = sharedPreferences.edit()
                        sharedPrefEditor.putString("token", token)
                        sharedPrefEditor.apply()
                        Toast.makeText(this@IndexActivity,"ログイン情報あり\nToken取得完了",Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        AlertDialog.Builder(this@IndexActivity).apply {
                            setTitle("ログインに失敗しました")
                            setMessage("もう一度実行してください")
                            setPositiveButton("確認", null)
                            show()
                        }
//                        finish()
                    }
                }
            })
            plat.execute( arrayListOf( sharedPreferences.getString("id","none"),sharedPreferences.getString("password","none") ) )
            /********************/
        }
    }

    fun onCreateButtonTapped(v: View) {
        startActivity(Intent(this, CreateActivity::class.java))
        finish()
    }

    fun onLoginButtonTapped(v: View) {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
