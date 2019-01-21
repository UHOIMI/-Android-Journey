package com.example.g015c1140.journey

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class IndexActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_index)
    }

    fun onCreateButtonTapped(v: View) {
        startActivity(Intent(this, CreateUserActivity::class.java))
    }

    fun onLoginButtonTapped(v: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}
