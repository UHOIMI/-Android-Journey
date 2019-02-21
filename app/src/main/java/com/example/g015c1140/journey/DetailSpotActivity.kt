package com.example.g015c1140.journey

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_detail_spot.*
import org.json.JSONObject
import java.util.*


class DetailSpotActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var spot: SpotData
    private var anotherSpotFlg = false
    private var postListFlg = false

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 222
        private const val EDIT_SPOT_REQUEST_CODE = 666
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_spot)
        setSupportActionBar(toolbar)

        //ツールバーセット
        title = "スポット詳細"
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomNavigation: BottomNavigationView = findViewById(R.id.navigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomNavigation)

        val myApp = this.application as MyApplication
        bottomNavigation.selectedItemId = myApp.getBnp()
        bottomNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        anotherSpotFlg = intent.getBooleanExtra("ANOTHER-SPOT-FLG", false)
        postListFlg = intent.getBooleanExtra("POST_LIST_FLG", false)

        if (anotherSpotFlg) {
            //スポット一覧からの遷移以外
            val gsat = GetSpotAsyncTask(intent.getStringExtra("ANOTHER-SPOT-ID"), false)
            gsat.setOnCallback(object : GetSpotAsyncTask.CallbackGetSpotAsyncTask() {
                override fun callback(resultSpotJsonList: ArrayList<JSONObject>?, resultIdFlg: Boolean) {
                    super.callback(resultSpotJsonList, resultIdFlg)
                    if (resultSpotJsonList!![resultSpotJsonList.size - 1].getString("result") == "RESULT-OK") {
                        resultSpotJsonList.removeAt(resultSpotJsonList.size - 1)

                        val spotJson = resultSpotJsonList[0]
                        val arJson: JSONObject = spotJson.getJSONObject("spot_address")

                        //完了
                        val bmp = arrayListOf<String>(
                                spotJson.getString("spot_image_a"),
                                spotJson.getString("spot_image_b"),
                                spotJson.getString("spot_image_c")
                        )

                        val giat = GetImageAsyncTask()
                        giat.setOnCallback(object : GetImageAsyncTask.CallbackGetImageAsyncTask() {
                            override fun callback(resultBmpString: String, resultBmpList: ArrayList<ArrayList<Bitmap?>>?) {
                                super.callback(resultBmpString, resultBmpList)
                                if (resultBmpString == "RESULT-OK") {

                                    spot = SpotData(
                                            spotJson.getString("spot_id"),
                                            spotJson.getString("spot_title"),
                                            arJson.getString("lat").toDouble(),
                                            arJson.getString("lng").toDouble(),
                                            spotJson.getString("spot_comment"),
                                            "imageA",
                                            "imageB",
                                            "imageC",
                                            Date()
                                    )

                                    //Map呼び出し
                                    val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
                                    mapFragment.getMapAsync(this@DetailSpotActivity)

                                    spotNameTextView.text = spot.title
                                    commentTextView.text = spot.comment


                                    if (resultBmpList!![0][0] != null) {
                                        imageView1.setImageBitmap(resultBmpList[0][0])
                                    } else {
                                        imageView1.setImageResource(R.drawable.no_image)
                                    }

                                    if (resultBmpList[0][1] != null) {
                                        imageView2.setImageBitmap(resultBmpList[0][1])
                                    } else {
                                        imageView2.setImageResource(R.drawable.no_image)
                                    }

                                    if (resultBmpList[0][2] != null) {
                                        imageView3.setImageBitmap(resultBmpList[0][2])
                                    } else {
                                        imageView3.setImageResource(R.drawable.no_image)
                                    }

                                } else {
                                    failedAsyncTask()
                                    return
                                }
                            }
                        })
                        giat.execute(arrayListOf(bmp))
                    } else {
                        failedAsyncTask()
                    }
                }
            })
            gsat.execute()

        } else {
            //スポット一覧からの遷移
            spot = intent.getSerializableExtra("SPOT") as SpotData

            //Map呼び出し
            val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
            mapFragment.getMapAsync(this)

            spotNameTextView.text = spot.title
            commentTextView.text = spot.comment

            storagePermissionCheck()
        }
    }

    private fun storagePermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Android 6.0 のみ、該当パーミッションが許可されていない場合
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "permission OK", Toast.LENGTH_SHORT).show()
            // 許可済みの場合、もしくはAndroid 6.0以前
            // パーミッションが必要な処理
            setImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        // 自分のコード以外がrequestPermissionsしているかもしれないので、requestCodeをチェックします。
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが必要な処理
                setImage()
            } else {
                // パーミッションが得られなかった時
                // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                Toast.makeText(this, "許可して頂けない場合は画像を表示できません", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun setImage() {
        if (spot.image_A != "") {
            val bmImg = BitmapFactory.decodeFile(spot.image_A)
            imageView1.setImageBitmap(bmImg)
        }
        if (spot.image_B != "") {
            val bmImg = BitmapFactory.decodeFile(spot.image_B)
            imageView2.setImageBitmap(bmImg)
        }
        if (spot.image_C != "") {
            val bmImg = BitmapFactory.decodeFile(spot.image_C)
            imageView3.setImageBitmap(bmImg)
        }
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("スポット取得に失敗しました")
            setPositiveButton("確認", null)
            show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(spot.latitude, spot.longitude), 12.5f))
        googleMap.addMarker(MarkerOptions().position(LatLng(spot.latitude, spot.longitude)).title(spot.title)).showInfoWindow()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!anotherSpotFlg) {
            menuInflater.inflate(R.menu.menu_detail, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
        R.id.saveButton -> {
            val intent = Intent(this, PutSpotActivity::class.java)
            intent.putExtra("SPOT", spot)
            intent.putExtra("POST_LIST_FLG", postListFlg)
            startActivityForResult(intent, EDIT_SPOT_REQUEST_CODE)
            true
        }
        //戻るボタンタップ時
        android.R.id.home -> {
            Toast.makeText(this, "もどーるぼたんたっぷど", Toast.LENGTH_SHORT).show()
            resultSet()
            finish ()
            true
        }
        else -> {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK && requestCode == EDIT_SPOT_REQUEST_CODE && intent != null) {

            spot = intent.getSerializableExtra("SPOT") as SpotData

            //Map呼び出し
            val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
            mapFragment.getMapAsync(this)

            spotNameTextView.text = spot.title
            commentTextView.text = spot.comment

            storagePermissionCheck()

        } else {

        }
    }


    //ボトムバータップ時
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this, HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this, SearchPlanActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this, TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this, DetailUserActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 戻るボタンの処理
            resultSet()
            finish()
            super.onKeyDown(keyCode, event)
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    private fun resultSet() {
        if (!anotherSpotFlg) {
            setResult(RESULT_OK, Intent().putExtra("SPOT", spot))
        }
    }
}
