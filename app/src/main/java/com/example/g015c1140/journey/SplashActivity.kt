package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class SplashActivity : AppCompatActivity() {

    private val HANDLER = Handler()
    private lateinit var run  : Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)

        run = Runnable {
            if (sharedPreferences.getBoolean(Setting().USER_SHARED_PREF_FLG, false)) {
                val plat = PostLoginAsyncTask()
                plat.setOnCallback(object : PostLoginAsyncTask.CallbackPostLoginAsyncTask() {
                    override fun callback(result: String, token: String) {
                        super.callback(result,token)
                        // ここからAsyncTask処理後の処理を記述します。
                        if (result == "RESULT-OK") {
                            //完了した関数呼び出し
                            val sharedPrefEditor = sharedPreferences.edit()
                            sharedPrefEditor.putString(Setting().USER_SHARED_PREF_TOKEN, token)
                            sharedPrefEditor.apply()
                            Toast.makeText(this@SplashActivity,"ログイン情報あり\nToken取得完了", Toast.LENGTH_SHORT).show()

                            if(!isFinishing) {
                                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                                finish()
                            }
                        } else {
                            AlertDialog.Builder(this@SplashActivity).apply {
                                setTitle("ログインに失敗しました")
                                setMessage("もう一度起動してください")
                                setPositiveButton("確認") { _, _ ->
                                    finish()
                                    show()
                                }
                            }
                        }
                    }
                })
                plat.execute( arrayListOf( sharedPreferences.getString(Setting().USER_SHARED_PREF_ID,"none"),sharedPreferences.getString(Setting().USER_SHARED_PREF_PASSWORD,"none") ) )
            }else {
                if (!isFinishing) {
                    startActivity(Intent(this@SplashActivity, IndexActivity::class.java))
                    finish()
                }
            }
        }

        HANDLER.postDelayed(run,500)// 2000ミリ秒後（2秒後）に実行
    }

    override fun onPause() {
        super.onPause()
        HANDLER.removeCallbacks(run)
    }
}
