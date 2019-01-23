package com.example.g015c1140.journey

//import io.realm.kotlin.createObject
import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
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
import android.os.AsyncTask
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
import kotlinx.android.synthetic.main.activity_put_spot.*
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class PutSpotActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val m_uri: Uri? = null
    private var tappedImageNum: Int = 0

    //data class spotData(val name : String, val latitude : Double, val longitude : Double)
    //val spotList: MutableList<SpotData> = mutableListOf()
    val spotList = arrayListOf<SpotData>()
    var spotNameTextView: EditText? = null
    var commentTextView: EditText? = null
    lateinit var spotImageView1: ImageView
    lateinit var spotImageView2: ImageView
    lateinit var spotImageView3: ImageView
    var longitude: Double = 0.0
    var latitude: Double = 0.0
    var image_A: String = ""
    var image_B: String = ""
    var image_C: String = ""

    var imageIntent = getIntent()
    private lateinit var mRealm: Realm
    var editFlag: Boolean = false

    val df = SimpleDateFormat("yyyyMMddHHmmssSSS")
    var errorMessage = ""

    val RESULT_SUBACTIVITY = 1000
    var JsonArray = JSONArray()

    lateinit private var spot: SpotData

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 111
        private const val STORAGE_PERMISSION_REQUEST_CODE = 222
        private const val RESULT_PICK_IMAGEFILE = 1001
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("test", "onCreate")
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

        spotNameTextView = findViewById(R.id.nameText) as EditText
        commentTextView = findViewById(R.id.commentEditText) as EditText

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
            storagePermissionCheck(1)
        }
        spotImageView3.setOnClickListener {
            storagePermissionCheck(1)
        }


        Realm.init(this)
        //val config = RealmConfiguration.Builder().build()
        //Realm.setDefaultConfiguration(config)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        //mRealm = Realm.getDefaultInstance()

        //val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment

        if (intent.getSerializableExtra("SPOT") != null) {
            editFlag = true
            spot = intent.getSerializableExtra("SPOT") as SpotData
            spotNameTextView!!.setText(spot.title)
            commentTextView!!.setText(spot.comment)
            if (!spot.image_A.equals("")) {
                image_A = spot.image_A
                val bmImg = BitmapFactory.decodeFile(spot.image_A)
                spotImageView1.setImageBitmap(bmImg)
            }
            if (!spot.image_B.equals("")) {
                image_B = spot.image_B
                val bmImg = BitmapFactory.decodeFile(spot.image_B)
                spotImageView2.setImageBitmap(bmImg)
            }
            if (!spot.image_C.equals("")) {
                image_C = spot.image_C
                val bmImg = BitmapFactory.decodeFile(spot.image_C)
                spotImageView3.setImageBitmap(bmImg)
            }
        }

        val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapAsync(this)

        //val mapFragment2 = getSupportFragmentManager().findFragmentById(R.id.mapFragment) as SupportMapFragment
        //mMap = mapFragment2.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (intent.getSerializableExtra("SPOT") == null) {
            PinButtonTapped()
            /*spot = intent.getSerializableExtra("SPOT") as SpotData
            if (!spot.image_A.equals("")){
                val bmImg = BitmapFactory.decodeFile(spot.image_A)
                spotImageView1.setImageBitmap(bmImg)
            }
            if (!spot.image_B.equals("")){
                val bmImg = BitmapFactory.decodeFile(spot.image_B)
                spotImageView2.setImageBitmap(bmImg)
            }
            if (!spot.image_C.equals("")){
                val bmImg = BitmapFactory.decodeFile(spot.image_C)
                spotImageView3.setImageBitmap(bmImg)
            }
            Log.d("editFlag","エディットフラグ")*/
            //editFlag = true
            //placeMarkerOnMap(LatLng(spot.latitude, spot.longitude))
        } else {
            //PinButtonTapped()
        }

        //PinButtonTapped()
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
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startActivity(Intent(this,SearchPlanActivity::class.java))
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
                startActivity(Intent(this,TimelineActivity::class.java).putExtra("FAVORITE_FLG", true))
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

    fun PinButtonTapped() {
        var lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!gpsEnabled) {
            AlertDialog.Builder(this).apply {
                setTitle("位置情報が有効になっていません")
                setMessage("このままアプリを続行したい場合は、有効化してください")
                setPositiveButton("設定", { _, _ ->
                    // OKをタップしたときの処理
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                })
                setNegativeButton("戻る", null)
                show()
            }
            return
        }

        setUpMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("ttttttttttttttttttttttt", "あああああああああああああああいいいいいいいいいいいいいいいいいいいいいい")

        if (editFlag == false) {
            //　位置情報権限確認
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //　権限がない場合
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
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

    fun storagePermissionCheck(num: Int) {
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

        Log.d("vvvvvvvvvvvvvvvvvvvvvv", "あああああああああああああああいいいいいいいいいいいいいいいいいいいいいい")
        // 自分のコード以外がrequestPermissionsしているかもしれないので、requestCodeをチェックします。

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {

            if (grantResults.any { it != PackageManager.PERMISSION_GRANTED }) {
                // リクエストを蹴られた場合はどうしましょ？　今回は、単純にActivityを終了させます。
                Log.d("check", "許可しない")
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_PICK_IMAGEFILE) {

            if (resultCode != RESULT_OK) {
                // キャンセル時
                return
            }

            var resultUri: Uri? = if (data != null) data.getData() else m_uri

            if (resultUri == null) {
                // 取得失敗
                return
            }

            // ギャラリーへスキャンを促す
            MediaScannerConnection.scanFile(
                    this,
                    arrayOf(resultUri.getPath()),
                    arrayOf("image/jpeg"),
                    null
            )

            Log.d("URI", resultUri.toString())
            Log.d("URI2", resultUri.getPath())
            Log.d("URI3", getPathFromUri(this, resultUri))

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

    fun onClickImage() {
        // イメージ画像がクリックされたときに実行される処理
        startActivityForResult(imageIntent, RESULT_PICK_IMAGEFILE);
    }

    private fun setUpMap() {
        Log.d("test", "setUpMap")

        //　位置情報権限確認
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //　権限がない場合
            //////ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            //ActivityCompat.requestPermissions()
            return
        }

        Log.d("test", "setUpMapの２")

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            //最後に既知の場所を取得します。 まれな状況では、これはヌルになる可能性があります。
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                latitude = location.latitude.toDouble()
                Log.d("座ひょー", location.latitude.toString())
                Log.d("座ひょー2", location.latitude.toLong().toString())
                longitude = location.longitude.toDouble()

                spotList.add(SpotData("tokenID", spotNameTextView!!.getText().toString(), location.latitude, location.longitude, "", "", "", "", Date()))

                Log.d("テスト", spotList.get(0).title)

                placeMarkerOnMap(currentLatLng)

            } else {
                Log.d("ヌルチェッカー", "null")
            }
        }
    }

    //　現在位置にマーカー設置
    private fun placeMarkerOnMap(location: LatLng) {

        Log.d("test", "place marker")
        mMap.clear()

        //　マーカー作成
        val markerOptions = MarkerOptions().position(location)

        //　文字列設定
        val titleStr = getAddress(location)
        Log.d("test", "アドレス")
        Log.d("test", titleStr)

        markerOptions.title(titleStr)

        //　マーカー設置
        mMap.addMarker(markerOptions)

        var cameraPosition: CameraPosition = CameraPosition.Builder()
                .target(location)
                .zoom(15f)
                .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    //　住所表示
    private fun getAddress(latLng: LatLng): String {

        Log.d("test", "getAddress")

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
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    fun saveSpot() {
        if (spotNameTextView!!.text.count() <= 0) {
            errorMessage = "スポット名を入力してください。\n"
        } else if (spotNameTextView!!.text.count() > 20) {
            errorMessage = "スポット名の文字数が20文字を超えています。\n"
        }

        if (commentTextView!!.text.count() > 140) {
            errorMessage += "スポット説明の文字数が140文字を超えています。\n"
        }

        if (!errorMessage.equals("")) {
            AlertDialog.Builder(this).apply {
                setTitle("エラー")
                setMessage(errorMessage)
                setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                    // OKをタップしたときの処理
                    errorMessage = ""
                })
                show()
            }
            saveButton.isClickable = true
        } else {
            createSpot(spotNameTextView!!.text.toString(), latitude, longitude, commentTextView!!.text.toString(), image_A, image_B, image_C)
            //print(mRealm.where(RealmSpotData::class.java).findAll())
            startActivity(Intent(this, SpotListActivity::class.java))
            finish()
        }
    }

    fun createSpot(name: String, latitude: Double, longitude: Double, comment: String, image_A: String, image_B: String, image_C: String) {

        if (editFlag == false) {
            create(name, latitude, longitude, comment, image_A, image_B, image_C)
        } else {
            mRealm.executeTransaction {
                var editSpot = mRealm.where(TestRea::class.java).equalTo("id", spot.id).findFirst()
                editSpot!!.name = name
                editSpot!!.comment = comment
                editSpot!!.image_A = image_A
                editSpot!!.image_B = image_B
                editSpot!!.image_C = image_C
            }
        }

        Log.d("Realm", mRealm.where(TestRea::class.java).findAll().toString())

        /*mRealm!!.beginTransaction()

        var RspotData = mRealm!!.createObject(RealmSpotData::class.java)
        //var RspotData = RealmSpotData()
        RspotData.id = 73824902
        RspotData.name = name
        RspotData.latitude = latitude
        RspotData.longitude = longitude
        RspotData.comment = ""
        RspotData.datetime = Date()
        RspotData.image_A = image_A
        RspotData.image_B = image_B
        RspotData.image_C = image_C
        //mRealm!!.insert(RspotData)
        mRealm!!.copyToRealm(RspotData)

        mRealm!!.commitTransaction()


        mRealm!!.executeTransaction {
            //var RspotData = mRealm!!.createObject(RealmSpotData::class.java)
            //var RspotData = RealmSpotData(df.format(Date()).toInt(),name,latitude,longitude,"",Date(),"","","")
            //var RspotData = RealmSpotData(783927482,"",0.0,0.0,"",Date(),"","","")
            var RspotData = mRealm!!.createObject(TestRea::class.java)
            //var RspotData = RealmSpotData()
            RspotData.id = 73824902
            RspotData.name = name
            RspotData.latitude = latitude
            RspotData.longitude = longitude
            RspotData.comment = ""
            RspotData.datetime = Date()
            RspotData.image_A = image_A
            RspotData.image_B = image_B
            RspotData.image_C = image_C
            //mRealm!!.insert(RspotData)
            mRealm!!.copyToRealm(RspotData)
            //mRealm!!.insert(RealmSpotData(1,"first",1,1,"",Date(),"","",""))
        }
        print( mRealm!!.where(RealmSpotData::class.java).findAll())
        Log.d("Realm", mRealm!!.where(RealmSpotData::class.java).findAll().toString())*/
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }

    fun getPathFromUri(context: Context, uri: Uri): String {
        var isAfterKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        Log.e("TAG", "uri:" + uri.getAuthority());
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(
                            uri.getAuthority())) {// ExternalStorageProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                //if ("primary".equalsIgnoreCase(type)) {
                if ("primary".equals(type)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    return "/stroage/" + type + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents".equals(
                            uri.getAuthority())) {// DownloadsProvider
                var id: String = DocumentsContract.getDocumentId(uri)
                var contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id.toLong())
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents".equals(
                            uri.getAuthority())) {// MediaProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                var contentUri: Uri? = null
                contentUri = MediaStore.Files.getContentUri("external")
                var selection: String = "_id=?"
                /*var selectionArgs = {
                        split[1]
                }*/
                var selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, *selectionArgs)
            }
        } else if ("content".equals(uri.getScheme())) {//MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.getScheme())) {// File
            return uri.getPath()
        }
        return ""
    }

    fun getDataColumn(context: Context, uri: Uri, selection: String?,
                      vararg selectionArgs: String?/*[]*/): String {
        var cursor: Cursor? = null
        var projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                var cindex: Int = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return ""
    }

    fun create(name: String, latitude: Double, longitude: Double, comment: String, image_A: String, image_B: String, image_C: String) {
        mRealm.executeTransaction {
            var trea = mRealm.createObject(TestRea::class.java, UUID.randomUUID().toString())
            trea.name = name
            trea.latitude = latitude
            trea.longitude = longitude
            trea.comment = comment
            trea.image_A = image_A
            trea.image_B = image_B
            trea.image_C = image_C

            mRealm.copyToRealm(trea)
        }
    }

    inner class GetAllMemoTask : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String?): String? {
            var connection: HttpURLConnection? = null
            var reader: BufferedReader? = null

            try {
                val url = URL("http://172.16.89.158:3000/api/v1/spot/find?user_id=1")
                connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val br = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                for (line in br.readLines()) {
                    line.run { sb.append(line) }
                }

                val titleArrayList = ArrayList<String>()

                try {
                    JsonArray = JSONArray(sb.toString())
                    for (i in 0 until JsonArray.length()) {
                        println("array.getJSONObject(i):${JsonArray.getJSONObject(i)}")
                        titleArrayList.add(JsonArray.getJSONObject(i).getString("title"))
                    }
                } catch (e: JSONException) {
                    println("error")
                }
                br.close()
                return titleArrayList.toString()

            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            } finally {
                connection?.disconnect()
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return null
        }
    }
}