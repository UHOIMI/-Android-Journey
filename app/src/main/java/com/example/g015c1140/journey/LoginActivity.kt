package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

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
        val sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()
        sharedPrefEditor.clear().apply()
        Toast.makeText(this, "sharedPreferences削除", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, IndexActivity::class.java))
        //finish()
    }
}
