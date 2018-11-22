package com.example.g015c1140.journey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun spotAddButtonTapped(v:View){
        //startActivity(Intent(this, DetailSpotActivity::class.java))
        startActivity(Intent(this, PutSpotActivity::class.java))
    }

    fun spotListButtonTapped(v:View){
       startActivity(Intent(this, SpotListActivity::class.java))
    }

    fun postButtonTapped(v:View){
        startActivity(Intent(this, PostActivity::class.java))
    }

    fun userButtonTapped(v:View){
        startActivity(Intent(this,IndexActivity::class.java))
    }

    fun userDetailButtonTapped(v:View){
        startActivity(Intent(this,DetailUserActivity::class.java))
    }
    fun timelineButtonTapped(v:View){
        startActivity(Intent(this,TimelineActivity::class.java))
    }

    fun userDataDeleteButtonTapped(v:View){
        val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
        val sharedPrefEditor = sharedPreferences.edit()
        sharedPrefEditor.clear().apply()
        Toast.makeText(this, "sharedPreferences削除", Toast.LENGTH_SHORT).show()
    }

}
