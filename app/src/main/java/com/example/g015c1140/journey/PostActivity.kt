package com.example.g015c1140.journey

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_post.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.IOException

class PostActivity : AppCompatActivity(), OnMapReadyCallback {

    //スポットリスト用アダプター
    private lateinit var spotListAdapter: ArrayAdapter<String>

    //スポット名リスト用データ
    private lateinit var spotNameList: MutableList<String>

    //交通手段ボタン用
    private lateinit var transportationImageButton: MutableList<ImageButton>

    //交通手段ボタンフラグ用
    private val TRANSPORTATION_IMAGE_FLG = mutableListOf(0, 0, 0, 0, 0, 0, 0)

    //受け渡しスポットリスト用
    var spotList = mutableListOf<SpotData>()
    private var imageDeleteNameList :ArrayList<String>? = arrayListOf()
    private var tappedSpotPosition:Int? = null

    private lateinit var mRealm: Realm

    /******************/
    //SelectSpotActivity用
    private val RESULT_CODE = 1123

    //MapPinAdd用
    private lateinit var mMap: GoogleMap
    private val MARKER_LIST = mutableListOf<Marker>()
    /******************/

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 222
        private const val DETAIL_SPOT_REQUEST_CODE = 555
    }

    private var storagePermissionFlg = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        //タイトル名セット
        title = "プラン投稿"

        //交通手段ボタン設定
        transportationImageButton = mutableListOf(findViewById(R.id.walkImageButton), findViewById(R.id.bicycleImageButton), findViewById(R.id.carImageButton), findViewById(R.id.busImageButton), findViewById(R.id.trainImageButton), findViewById(R.id.airplaneImageButton), findViewById(R.id.boatImageButton))

        //ツールバーセット
        val toolbar = toolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(navigation)

        val myApp = this.application as MyApplication
        navigation.selectedItemId = myApp.getBnp()
        navigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        //Map呼び出し
        val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
        mapFragment.getMapAsync(this)

        //リストビュー設定
        // リスト項目とListViewを対応付けるArrayAdapterを用意する
        spotNameList = mutableListOf("スポット追加＋")
        spotListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, spotNameList)
        // ListViewにArrayAdapterを設定する
//        val spotListView: ListView = findViewById(R.id.spotListView)
        spotListView.adapter = spotListAdapter

        // 項目をタップしたときの処理
        spotListView.setOnItemClickListener { _, _, position, _ ->
            // 一番上の項目をタップしたら
            if (position == 0) {
                if (spotList.size < 20) {
                    /************************************************/
                    startActivityForResult(Intent(this, SelectSpotActivity::class.java).putExtra("SPOTCNT", spotList.size), RESULT_CODE)
                    /************************************************/
                }
            } else {
                tappedSpotPosition = position - 1
                if (spotList[position - 1].id != "tokenID") {
                    startActivityForResult(Intent(this, DetailSpotActivity::class.java).putExtra("SPOT", spotList[position - 1]), DETAIL_SPOT_REQUEST_CODE)
                }else{
                    startActivityForResult(Intent(this, DetailSpotActivity::class.java).putExtra("SPOT", spotList[position - 1]).putExtra("POST_LIST_FLG", true), DETAIL_SPOT_REQUEST_CODE)
                }
            }
        }

        // 項目を長押ししたときの処理
        spotListView.setOnItemLongClickListener { _, _, position, _ ->

            // 一番下の項目以外は長押しで削除
            when (position) {
                0 -> return@setOnItemLongClickListener false

                else -> {
                    AlertDialog.Builder(this).apply {
                        setTitle("スポット削除")
                        setMessage("スポット:${spotNameList[position]} を削除しますか？")
                        setPositiveButton("削除") { _, _ ->
                            // 削除をタップしたときの処理
                            //spotListAdapter.remove(spotListAdapter.getItem(position))
                            spotNameList.removeAt(position)
                            spotList.removeAt(position - 1)
                            //削除した項目以下の連番更新
                            for (_cnt in position until spotNameList.size) {
                                spotNameList[_cnt] = "${_cnt}：${spotList[_cnt - 1].title}"
                            }
                            spotNameList[0] = "スポット追加＋"
                            spotListAdapter.notifyDataSetChanged()

                            //Pin編集
                            mapPinEdit()
                        }
                        setNegativeButton("戻る", null)
                        show()
                    }
                    return@setOnItemLongClickListener true
                }
            }
        }

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        planDetailEditText.onFocusChangeListener = View.OnFocusChangeListener { _, focus ->
            if (focus) {
//                Toast.makeText(applicationContext, "Got the focus", Toast.LENGTH_LONG).show()
                navigation.visibility = View.INVISIBLE
            } else {
//                Toast.makeText(applicationContext, "Lost the focus", Toast.LENGTH_LONG).show()
                navigation.visibility = View.VISIBLE
            }
        }
    }

    /************************************************/
    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == RESULT_OK && requestCode == RESULT_CODE && intent != null) {

            val selectSpotList = intent.getSerializableExtra("SPOTDATA") as MutableList<SpotData>

            spotList.addAll(selectSpotList)
            selectSpotList.forEach { spotNameList.add("${spotNameList.size}：${it.title}") }

            if (spotList.size >= 20) {
                spotNameList[0] = "これ以上スポットを追加できません"
            } else {
                spotNameList[0] = "スポット追加＋"
            }
            spotListAdapter.notifyDataSetChanged()

            //Pin編集
            mapPinEdit()
        }else if(resultCode == RESULT_OK && requestCode == DETAIL_SPOT_REQUEST_CODE && intent != null){

            val spot = intent.getSerializableExtra("SPOT") as SpotData

            spotList[tappedSpotPosition!!] = spot
            spotNameList[tappedSpotPosition!! + 1] = "${tappedSpotPosition!! + 1}：${spot.title}"

            spotListAdapter.notifyDataSetChanged()
        }
    }

    /************************************************/

    //GoogleMap 設定
    override fun onMapReady(googleMap: GoogleMap) {
        //val
        mMap = googleMap

/*
         Add a marker in Sydney and move the camera
        val skyTree = LatLng(35.710063, 139.8107)
        mMap.addMarker(MarkerOptions().position(skyTree).title("東京スカイツリー"))
*/

        val cameraPosition: CameraPosition = CameraPosition.Builder()
                .target(LatLng(35.710063, 139.8107))
                .zoom(15f)
                .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun mapPinEdit() {

        //Pin全削除
        MARKER_LIST.forEach { it.remove() }
        MARKER_LIST.clear()

        when (spotList.size) {
            //全件削除された場合
            0 -> return
            else -> {
                var cnt = 1
                spotList.forEach {
                    //Pin追加
                    val marker = mMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)).title("$cnt"))
                    cnt++
                    //情報ウィンドウ表示
                    marker.showInfoWindow()
                    MARKER_LIST.add(marker)
                }

                //camera移動
                val cameraPosition
                        : CameraPosition = CameraPosition.Builder()
                        .target(LatLng(spotList[0].latitude, spotList[0].longitude))
                        .zoom(17f)
                        .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }
    }

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

    @SuppressLint("PrivateResource")
    fun onTransportationButtonTapped(view: View) {

        when (view.id) {
            R.id.walkImageButton -> {
                TRANSPORTATION_IMAGE_FLG[0] = if (TRANSPORTATION_IMAGE_FLG[0] == 1) {
                    transportationImageButton[0].setImageResource(R.drawable.s_walk_off)
                    0
                } else {
                    transportationImageButton[0].setImageResource(R.drawable.s_walk_on)
                    1
                }
            }

            R.id.bicycleImageButton -> {
                TRANSPORTATION_IMAGE_FLG[1] = if (TRANSPORTATION_IMAGE_FLG[1] == 1) {
                    transportationImageButton[1].setImageResource(R.drawable.s_bicycle_off)
                    0
                } else {
                    transportationImageButton[1].setImageResource(R.drawable.s_bicycle_on)
                    1
                }
            }

            R.id.carImageButton -> {
                TRANSPORTATION_IMAGE_FLG[2] = if (TRANSPORTATION_IMAGE_FLG[2] == 1) {
                    transportationImageButton[2].setImageResource(R.drawable.s_car_off)
                    0
                } else {
                    transportationImageButton[2].setImageResource(R.drawable.s_car_on)
                    1
                }

            }

            R.id.busImageButton -> {
                TRANSPORTATION_IMAGE_FLG[3] = if (TRANSPORTATION_IMAGE_FLG[3] == 1) {
                    transportationImageButton[3].setImageResource(R.drawable.s_bus_off)
                    0
                } else {
                    transportationImageButton[3].setImageResource(R.drawable.s_bus_on)
                    1
                }

            }

            R.id.trainImageButton -> {
                TRANSPORTATION_IMAGE_FLG[4] = if (TRANSPORTATION_IMAGE_FLG[4] == 1) {
                    transportationImageButton[4].setImageResource(R.drawable.s_train_off)
                    0
                } else {
                    transportationImageButton[4].setImageResource(R.drawable.s_train_on)
                    1
                }
            }

            R.id.airplaneImageButton -> {
                TRANSPORTATION_IMAGE_FLG[5] = if (TRANSPORTATION_IMAGE_FLG[5] == 1) {
                    transportationImageButton[5].setImageResource(R.drawable.s_airplane_off)
                    0
                } else {
                    transportationImageButton[5].setImageResource(R.drawable.s_airplane_on)
                    1
                }
            }

            R.id.boatImageButton -> {
                TRANSPORTATION_IMAGE_FLG[6] = if (TRANSPORTATION_IMAGE_FLG[6] == 1) {
                    transportationImageButton[6].setImageResource(R.drawable.s_boat_off)
                    0
                } else {
                    transportationImageButton[6].setImageResource(R.drawable.s_boat_on)
                    1
                }

            }
        }
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

    fun onPostButtonTapped(view: View) {
        postButton.isClickable = false
        //全部がOKな場合
        storagePermissionCheck()

        if (checkData() && storagePermissionFlg) {
            val sharedPreferences = getSharedPreferences(Setting().USER_SHARED_PREF, Context.MODE_PRIVATE)
            var imageList = arrayListOf<String>()
            spotList.forEach {
                imageList.add(it.image_A)
                imageList.add(it.image_B)
                imageList.add(it.image_C)
            }

            imageList = saveAndLoadImage(imageList)

            /********************/
            //imageを投稿
            val piat = PostImageAsyncTask()
            piat.setOnCallback(object : PostImageAsyncTask.CallbackPostImageAsyncTask() {
                override fun callback(result: String, data: String) {
                    super.callback(result, data)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test ImageCallback", "非同期処理$result　　URL $data")
                    if (result == "RESULT-OK") {
                        //完了した場合
                        val imageJson = JSONArray(data)
                        for (_nullCnt in 0 until imageJson.length()) {
                            if (imageJson.isNull(_nullCnt))
                                imageJson.put(_nullCnt, "")
                        }
                        var _imageCnt = 0
                        for (_spotCnt in 0 until spotList.size) {
                            if (imageJson.getString(_imageCnt) == "") {
                                spotList[_spotCnt].image_A = imageJson.getString(_imageCnt++)
                            } else {
                                spotList[_spotCnt].image_A = "${Setting().USER_IMAGE_GET_URL}${imageJson.getString(_imageCnt++)}"
                            }

                            if (imageJson.getString(_imageCnt) == "") {
                                spotList[_spotCnt].image_B = imageJson.getString(_imageCnt++)
                            } else {
                                spotList[_spotCnt].image_B = "${Setting().USER_IMAGE_GET_URL}${imageJson.getString(_imageCnt++)}"
                            }

                            if (imageJson.getString(_imageCnt) == "") {
                                spotList[_spotCnt].image_C = imageJson.getString(_imageCnt++)
                            } else {
                                spotList[_spotCnt].image_C = "${Setting().USER_IMAGE_GET_URL}${imageJson.getString(_imageCnt++)}"
                            }
                        }

                        /********************/
                        //Planを投稿
                        val ppat = PostPlanAsyncTask(sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, "none"), sharedPreferences.getString(Setting().USER_SHARED_PREF_TOKEN, "none"))
                        ppat.setOnCallback(object : PostPlanAsyncTask.CallbackPostPlanAsyncTask() {
                            override fun callback(result: String) {
                                super.callback(result)
                                // ここからAsyncTask処理後の処理を記述します。
                                Log.d("test PlanCallback", "非同期処理$result")
                                if (result == "RESULT-OK") {
                                    /********************/

                                    //plan取得
                                    val gpat = GetPlanAsyncTask(sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, "none"), false)
                                    gpat.setOnCallback(object : GetPlanAsyncTask.CallbackGetPlanAsyncTask() {
                                        override fun callback(resultPlanJson: JSONObject) {
                                            super.callback(resultPlanJson)
                                            // ここからAsyncTask処理後の処理を記述します。
                                            Log.d("test GetSpotCallback", "非同期処理$result")
                                            if (resultPlanJson.getString("result") == "RESULT-OK") {

                                                /********************/
                                                //spotを投稿
                                                val psat = PostSpotAsyncTask(sharedPreferences.getString(Setting().USER_SHARED_PREF_ID, "none"), sharedPreferences.getString(Setting().USER_SHARED_PREF_TOKEN, "none"), resultPlanJson.getString("plan_id"))
                                                psat.setOnCallback(object : PostSpotAsyncTask.CallbackPostSpotAsyncTask() {
                                                    override fun callback(result: String) {
                                                        super.callback(result)
                                                        // ここからAsyncTask処理後の処理を記述します。
                                                        Log.d("test SpotCallback", "非同期処理結果：$result")
                                                        if (result == "RESULT-OK") {
                                                            completePostAsyncTask()
                                                        } else {
                                                            failedAsyncTask()
                                                        }
                                                    }
                                                })
                                                psat.execute(spotList)
                                                /********************/

                                            } else {
                                                failedAsyncTask()
                                            }
                                        }
                                    })
                                    gpat.execute()
                                    /********************/
                                } else {
                                    failedAsyncTask()
                                }
                            }
                        })
                        ppat.execute(
                                planTitleEditText.text.toString(),
                                planDetailEditText.text.toString(),
                                TRANSPORTATION_IMAGE_FLG.toString().replace(" ", "").substring(1, 14),
                                planMoneySpinner.selectedItem.toString(),
                                planPrefecturesSpinner.selectedItem.toString()
                        )
                        /********************/
                    } else {
                        failedAsyncTask()
                    }
                }
            })
            piat.execute(imageList)
        }else{
            postButton.isClickable = true
        }
    }

    private fun checkData(): Boolean {
        var checkResult = ""

        if (planTitleEditText.text.toString().replace(" ", "").replace("　", "") == "") {
            checkResult += "プラン名が入力されていません\n"
        }

        if (planPrefecturesSpinner.selectedItemPosition == 0) {
            checkResult += "都道府県が選択されていません\n"
        }

        if (spotList.size == 0) {
            checkResult += "スポットが登録されていません\n"
        }

        if (TRANSPORTATION_IMAGE_FLG.toString().replace(" ", "").substring(1, 14) == "0,0,0,0,0,0,0") {
            checkResult += "交通手段が選択されていません\n"
        }

        if (planMoneySpinner.selectedItemPosition == 0) {
            checkResult += "金額が選択されていません\n"
        }

        if (planDetailEditText.text.toString().replace(" ", "").replace("　", "") == "") {
            checkResult += "プラン詳細が入力されていません\n"
        }

        return if (checkResult == "") {
            true
        } else {
            AlertDialog.Builder(this).apply {
                setTitle("入力情報が間違っています")
                setMessage(checkResult)
                setPositiveButton("確認", null)
                show()
            }
            false
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
            storagePermissionFlg = true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {

        Log.d("vvvvvvvvvvvvvvvvvvvvvv", "あああああああああああああああいいいいいいいいいいいいいいいいいいいいいい")
        // 自分のコード以外がrequestPermissionsしているかもしれないので、requestCodeをチェックします。
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // パーミッションが必要な処理
                storagePermissionFlg = true
            } else {
                // パーミッションが得られなかった時
                // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等
                Toast.makeText(this, "許可して頂けない場合は、スポット画像を投稿する処理ができません", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun saveAndLoadImage(imageList: ArrayList<String>) : ArrayList<String>{

        var fileOut: FileOutputStream? = null
        var bmpImg:Bitmap

        for (_imgCnt in 0 until imageList.size){

            if (imageList[_imgCnt] != "") {
                bmpImg = BitmapFactory.decodeFile(imageList[_imgCnt])
                imageDeleteNameList!!.add("${imageList[_imgCnt].substringAfterLast("/").substringBefore(".")}.jpg")
                try {
                    // openFileOutputはContextのメソッドなのでActivity内ならばthisでOK
                    fileOut = this.openFileOutput("${imageDeleteNameList!![imageDeleteNameList!!.size -1]}", Context.MODE_PRIVATE)
                    bmpImg.compress(Bitmap.CompressFormat.JPEG, 50/*100*/, fileOut)

                    bmpImg.recycle()
                    imageList[_imgCnt] = getPathFromUri(this, Uri.fromFile(getFileStreamPath("${imageDeleteNameList!![imageDeleteNameList!!.size -1]}")))

                } catch (e: IOException) {
                    failedAsyncTask()
                } finally {
                    fileOut?.close()
                }
            }
        }
        return imageList
    }

    private fun getPathFromUri(context: Context, uri: Uri): String {
        var isAfterKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        Log.e("TAG", "uri:" + uri.authority);
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {// ExternalStorageProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                //if ("primary".equalsIgnoreCase(type)) {
                return if ("primary" == type) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    "/stroage/" + type + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {// DownloadsProvider
                var id: String = DocumentsContract.getDocumentId(uri)
                var contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), id.toLong()
                )
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents" == uri.authority) {// MediaProvider
                var docId: String = DocumentsContract.getDocumentId(uri)
                var split = docId.split(":")
                var type: String = split[0]
                var contentUri: Uri? = null
                contentUri = MediaStore.Files.getContentUri("external")
                var selection = "_id=?"
                /*var selectionArgs = {
                        split[1]
                }*/
                var selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, *selectionArgs)
            }
        } else if ("content" == uri.scheme) {//MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {// File
            return uri.path
        }
        return ""
    }

    private fun getDataColumn(context: Context, uri: Uri, selection: String?, vararg selectionArgs: String?/*[]*/): String {
        var cursor: Cursor? = null
        var projection = arrayOf(MediaStore.Files.FileColumns.DATA)
        try {
            cursor = context.contentResolver.query(
                    uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                var cindex: Int = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex);
            }
        } finally {
            cursor?.close()
        }
        return ""
    }

    private fun completePostAsyncTask() {
        Toast.makeText(this, "投稿が完了しました", Toast.LENGTH_SHORT).show()

        println("プラン名：${planTitleEditText.text}")
        println("都道府県：${planPrefecturesSpinner.selectedItem}")
        spotNameList.removeAt(0)
        spotNameList.forEach { println("スポット一覧：$it") }
        print("交通手段：")
        for (_cnt in 0 until TRANSPORTATION_IMAGE_FLG.size) {
            if (TRANSPORTATION_IMAGE_FLG[_cnt] == 1) {
                when (_cnt) {
                    0 -> print("歩き　")
                    1 -> print("自転車　")
                    2 -> print("車　")
                    3 -> print("バス　")
                    4 -> print("電車　")
                    5 -> print("飛行機　")
                    6 -> print("船　")
                }
            }
        }
        println("")
        println("金額：${planMoneySpinner.selectedItem}")
        println("プラン説明：${planDetailEditText.text}")

//Realm削除処理テストするためコメントアウト（削除処理は完成済み）
/*
        spotList.forEach { spotData ->
            mRealm.executeTransaction {
                val spotRealmData = mRealm.where(TestRea::class.java).equalTo("id", spotData.id).findAll()
                spotRealmData.deleteFromRealm(0)
            }
        }
*/
        imageDeleteNameList!!.forEach {
            this.deleteFile(it)
        }
        imageDeleteNameList = null
        finish()
    }

    private fun failedAsyncTask() {
        AlertDialog.Builder(this).apply {
            setTitle("投稿に失敗しました")
            setMessage("もう一度実行してください")
            setPositiveButton("確認", null)
            show()
        }
        postButton.isClickable = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mRealm.close()
    }
}
