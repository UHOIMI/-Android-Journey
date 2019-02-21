package com.example.g015c1140.journey

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_put_spot.*
import java.io.IOException
import java.util.*

class PutSpotActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var tappedImageNum: Int = 0

    private val spotList = arrayListOf<SpotData>()
    private var spotNameTextView: EditText? = null
    private var commentTextView: EditText? = null
    private lateinit var spotImageView1: ImageView
    private lateinit var spotImageView2: ImageView
    private lateinit var spotImageView3: ImageView
    private var longitude: Double = 0.0
    private var latitude: Double = 0.0
    private var image_A: String = ""
    private var image_B: String = ""
    private var image_C: String = ""

    private var imageIntent = intent
    private lateinit var mRealm: Realm
    private var editFlag: Boolean = false

    private var errorMessage = ""

    private lateinit var spot: SpotData

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 111
        private const val STORAGE_PERMISSION_REQUEST_CODE = 222
        private const val RESULT_PICK_IMAGEFILE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_put_spot)

        //タイトル名セット
        title = "スポット追加"

        //戻るボタンセット
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(putSpotNavigation)

        val myApp = this.application as MyApplication
        putSpotNavigation.selectedItemId = myApp.getBnp()
        putSpotNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        spotNameTextView = findViewById(R.id.nameText)
        commentTextView = findViewById(R.id.commentEditText)

        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            saveButton.isClickable = false
            saveSpot()
        }

        imageIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        imageIntent.addCategory(Intent.CATEGORY_OPENABLE)
        imageIntent.type = "*/*"

        spotImageView1 = this.findViewById(R.id.imageView1) as ImageView
        spotImageView2 = this.findViewById(R.id.imageView2) as ImageView
        spotImageView3 = this.findViewById(R.id.imageView3) as ImageView

        spotImageView1.setOnClickListener {
            storagePermissionCheck(1)
        }
        spotImageView2.setOnClickListener {
            storagePermissionCheck(2)
        }
        spotImageView3.setOnClickListener {
            storagePermissionCheck(3)
        }


        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        if (intent.getSerializableExtra("SPOT") != null) {
            editFlag = true
            title = "スポット編集"
            spot = intent.getSerializableExtra("SPOT") as SpotData
            spotNameTextView!!.setText(spot.title)
            commentTextView!!.setText(spot.comment)
            if (spot.image_A != "") {
                image_A = spot.image_A
                val bmImg = BitmapFactory.decodeFile(spot.image_A)
                spotImageView1.setImageBitmap(bmImg)
            }
            if (spot.image_B != "") {
                image_B = spot.image_B
                val bmImg = BitmapFactory.decodeFile(spot.image_B)
                spotImageView2.setImageBitmap(bmImg)
            }
            if (spot.image_C != "") {
                image_C = spot.image_C
                val bmImg = BitmapFactory.decodeFile(spot.image_C)
                spotImageView3.setImageBitmap(bmImg)
            }
        }

        val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (intent.getSerializableExtra("SPOT") == null) {
            pinButtonTapped()
        }
    }

    //ToolBarのボタン処理
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //戻るボタンタップ時
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //ボトムバータップ時
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this, SearchPlanActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this, TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_setting -> {
                startActivity(Intent(this, DetailUserActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    // マーカーをタップすると呼び出される
    override fun onMarkerClick(p0: Marker?) = false

    private fun pinButtonTapped() {
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!gpsEnabled) {
            AlertDialog.Builder(this).apply {
                setTitle("位置情報が有効になっていません")
                setMessage("このままアプリを続行したい場合は、有効化してください")
                setPositiveButton("設定") { _, _ ->
                    // OKをタップしたときの処理
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                setNegativeButton("戻る", null)
                show()
            }
            return
        }

        setUpMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {

        if (!editFlag) {
            //　位置情報権限確認
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //　権限がない場合
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)

                mMap = googleMap
                return
            }

            mMap = googleMap
            // ピンのクリックリスナー
            mMap.setOnMarkerClickListener(this)
            //　現在位置マーカーと現在位置ボタン有効化
            mMap.isMyLocationEnabled = true
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(spot.latitude, spot.longitude), 17f))
            googleMap.addMarker(MarkerOptions().position(LatLng(spot.latitude, spot.longitude)).title(spot.title)).showInfoWindow()
        }

    }

    private fun storagePermissionCheck(num: Int) {
        tappedImageNum = num

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Android 6.0 のみ、該当パーミッションが許可されていない場合
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        } else {
            Toast.makeText(this, "permission OK", Toast.LENGTH_SHORT).show()
            // 許可済みの場合、もしくはAndroid 6.0以前
            // パーミッションが必要な処理
            onClickImage()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        // 自分のコード以外がrequestPermissionsしているかもしれないので、requestCodeをチェックします。

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                // リクエストを蹴られた場合はどうしましょ？　今回は、単純にActivityを終了させます。
                finish()
                return
            } else {
                setUpMap()
                onMapReady(mMap)
            }

            //onMapReady(mMap)
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが必要な処理
                onClickImage()
            } else {
                // パーミッションが得られなかった時
                // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                tappedImageNum = 0
                Toast.makeText(this, "許可しないと処理は続行できません", Toast.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_PICK_IMAGEFILE) {

            if (resultCode != RESULT_OK) {
                // キャンセル時
                return
            }

            val resultUri: Uri = data.data
                    ?: // 取得失敗
                    return

            // ギャラリーへスキャンを促す
            MediaScannerConnection.scanFile(
                    this,
                    arrayOf(resultUri.path),
                    arrayOf("image/jpeg"),
                    null
            )

            when (tappedImageNum) {
                1 -> {
                    spotImageView1.setImageURI(resultUri)
                    image_A = getPathFromUri(this, resultUri)
                }
                2 -> {
                    spotImageView2.setImageURI(resultUri)
                    image_B = getPathFromUri(this, resultUri)

                }
                3 -> {
                    spotImageView3.setImageURI(resultUri)
                    image_C = getPathFromUri(this, resultUri)
                }
            }
        }
    }

    private fun onClickImage() {
        // イメージ画像がクリックされたときに実行される処理
        startActivityForResult(imageIntent, RESULT_PICK_IMAGEFILE)
    }

    private fun setUpMap() {

        //　位置情報権限確認
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //　権限がない場合
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            //最後に既知の場所を取得します。 まれな状況では、これはヌルになる可能性があります。
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                latitude = location.latitude
                longitude = location.longitude

                spotList.add(SpotData("tokenID", spotNameTextView!!.text.toString(), location.latitude, location.longitude, "", "", "", "", Date()))

                placeMarkerOnMap(currentLatLng)

            }
        }
    }

    //　現在位置にマーカー設置
    private fun placeMarkerOnMap(location: LatLng) {

        mMap.clear()

        //　マーカー作成
        val markerOptions = MarkerOptions().position(location)

        //　文字列設定
        val titleStr = getAddress(location)

        markerOptions.title(titleStr)

        //　マーカー設置
        mMap.addMarker(markerOptions)

        val cameraPosition: CameraPosition = CameraPosition.Builder()
                .target(location)
                .zoom(15f)
                .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //　住所表示
    private fun getAddress(latLng: LatLng): String {

        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            //　アドレス取得
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0..address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i).toString() else "\n" + address.getAddressLine(i).toString()
                }
            }
        } catch (e: IOException) {
            Log.d("test", "アドレスエラー")
        }

        return addressText
    }

    private fun saveSpot() {
        if (spotNameTextView!!.text.count() <= 0) {
            errorMessage = "スポット名を入力してください。\n"
        } else if (spotNameTextView!!.text.count() > 20) {
            errorMessage = "スポット名の文字数が20文字を超えています。\n"
        }

        if (commentTextView!!.text.count() > 140) {
            errorMessage += "スポット説明の文字数が140文字を超えています。\n"
        }

        if (errorMessage != "") {
            AlertDialog.Builder(this).apply {
                setTitle("エラー")
                setMessage(errorMessage)
                setPositiveButton("OK") { _, _ ->
                    // OKをタップしたときの処理
                    errorMessage = ""
                }
                show()
            }
            saveButton.isClickable = true
        } else {
            createSpot(spotNameTextView!!.text.toString(), latitude, longitude, commentTextView!!.text.toString(), image_A, image_B, image_C)

            if (editFlag){
                setResult(RESULT_OK, Intent().putExtra("SPOT", spot))
            }else{
                startActivity(Intent(this, SpotListActivity::class.java))
            }
            finish()
        }
    }

    private fun createSpot(name: String, latitude: Double, longitude: Double, comment: String, image_A: String, image_B: String, image_C: String) {

        if (!editFlag) {
            create(name, latitude, longitude, comment, image_A, image_B, image_C)
        } else {

            if (intent.getBooleanExtra("POST_LIST_FLG",false)){
                create(name, spot.latitude, spot.longitude, comment, image_A, image_B, image_C)
            }else {
                mRealm.executeTransaction {
                    val editSpot = mRealm.where(TestRea::class.java).equalTo("id", spot.id).findFirst()
                    editSpot!!.name = name
                    editSpot.comment = comment
                    editSpot.image_A = image_A
                    editSpot.image_B = image_B
                    editSpot.image_C = image_C
                }

                spot.title = name
                spot.comment = comment
                spot.image_A = image_A
                spot.image_B = image_B
                spot.image_C = image_C
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    private fun getPathFromUri(context: Context, uri: Uri): String {
        val isAfterKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            when {
                "com.android.externalstorage.documents" == uri.authority -> {// ExternalStorageProvider
                    val docId: String = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val type: String = split[0]
                    //if ("primary".equalsIgnoreCase(type)) {
                    return if ("primary" == type) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        "/stroage/" + type + "/" + split[1]
                    }
                }
                "com.android.providers.downloads.documents" == uri.authority -> {// DownloadsProvider
                    val id: String = DocumentsContract.getDocumentId(uri)
                    val contentUri: Uri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), id.toLong())
                    return getDataColumn(context, contentUri, null, null)
                }
                "com.android.providers.media.documents" == uri.authority -> {// MediaProvider
                    val docId: String = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    val contentUri: Uri? = MediaStore.Files.getContentUri("external")
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri!!, selection, *selectionArgs)
                }
            }
        } else if ("content" == uri.scheme) {//MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {// File
            return uri.path
        }
        return ""
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?,
                              vararg selectionArgs: String?/*[]*/): String {
        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val cindex: Int = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex)
            }
        } finally {
            cursor?.close()
        }
        return ""
    }

    private fun create(name: String, latitude: Double, longitude: Double, comment: String, image_A: String, image_B: String, image_C: String) {
        mRealm.executeTransaction {
            val trea = mRealm.createObject(TestRea::class.java, UUID.randomUUID().toString())
            trea.name = name
            trea.latitude = latitude
            trea.longitude = longitude
            trea.comment = comment
            trea.image_A = image_A
            trea.image_B = image_B
            trea.image_C = image_C

            mRealm.copyToRealm(trea)
        }
        val reDa = mRealm.where(TestRea::class.java).findAllSorted("datetime", Sort.DESCENDING).first()
        if (reDa != null) {
            spot = SpotData(reDa.id, reDa.name, reDa.latitude, reDa.longitude, reDa.comment, reDa.image_A, reDa.image_B, reDa.image_C, reDa.datetime)
        }
    }
}