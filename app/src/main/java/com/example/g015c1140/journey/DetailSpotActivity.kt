package com.example.g015c1140.journey

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail_spot.*


class DetailSpotActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var spot: SpotData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_spot)
        setSupportActionBar(toolbar)

        spot = intent.getSerializableExtra("SPOT") as SpotData

        //ツールバーセット
        title = "スポット詳細"
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomNavigation)
        navigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        //Map呼び出し
        val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapAsync(this)

        spotNameTextView.text = spot.title
        Log.d("test", "com ${spot.comment}")
        commentTextView.text = spot.comment
        val imageView1 = findViewById(R.id.imageView1) as ImageView
        val imageView2 = findViewById(R.id.imageView2) as ImageView
        val imageView3 = findViewById(R.id.imageView3) as ImageView

        if (!spot.image_A.equals("")) {
            val bmImg = BitmapFactory.decodeFile(spot.image_A)
            imageView1.setImageBitmap(bmImg)
        }
        if (!spot.image_B.equals("")) {
            val bmImg = BitmapFactory.decodeFile(spot.image_B)
            imageView2.setImageBitmap(bmImg)
        }
        if (!spot.image_C.equals("")) {
            val bmImg = BitmapFactory.decodeFile(spot.image_C)
            imageView3.setImageBitmap(bmImg)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(spot.latitude, spot.longitude), 14f))
        googleMap.addMarker(MarkerOptions().position(LatLng(spot.latitude, spot.longitude)).title(spot.title)).showInfoWindow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.saveButton -> {
            val intent = Intent(this, PutSpotActivity::class.java)
            intent.putExtra("SPOT", spot)
            startActivity(intent)
            //Toast.makeText(this, "編集ボタン", Toast.LENGTH_LONG).show()
            true
        }
        //戻るボタンタップ時
        android.R.id.home -> {
            Toast.makeText(this, "もどーるぼたんたっぷど", Toast.LENGTH_SHORT).show()
            finish()
            true
        }
        else -> {
            false
        }
    }

    //ボトムバータップ時
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                spotNameTextView.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                spotNameTextView.setText(R.string.title_search)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                spotNameTextView.setText(R.string.title_favorite)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this,DetailUserActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


}
