package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_search_plan.*



class SearchPlanActivity : AppCompatActivity() {


    //交通手段ボタン用
    private lateinit var transportationImageButton: MutableList<ImageButton>

    //交通手段ボタンフラグ用
    private val TRANSPORTATION_IMAGE_FLG = mutableListOf(0, 0, 0, 0, 0, 0, 0)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_plan)


        //タイトル名セット
        title = "検索"

        val toolbar = searchToolbar
        setSupportActionBar(toolbar)
        //戻るボタンセット
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeButtonEnabled(true)

        //ボトムバー設定
        val bottomnavigation: BottomNavigationView = findViewById(R.id.searchNavigation)
        // BottomNavigationViewHelperでアイテムのサイズ、アニメーションを調整
        AdjustmentBottomNavigation().disableShiftMode(bottomnavigation)
        searchNavigation.setOnNavigationItemSelectedListener(ON_NAVIGATION_ITEM_SELECTED_LISTENER)

        //交通手段ボタン設定
        transportationImageButton = mutableListOf(findViewById(R.id.walkImageButton), findViewById(R.id.bicycleImageButton), findViewById(R.id.carImageButton), findViewById(R.id.busImageButton), findViewById(R.id.trainImageButton), findViewById(R.id.airplaneImageButton), findViewById(R.id.boatImageButton))


        //値セット
        val searchTextValue = Array(5) { i -> "searchValue-$i" }
        val searchTextListAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, searchTextValue)
        searchTextList.adapter = searchTextListAdapter


        searchTextEditText.onFocusChangeListener = OnFocusChangeListener { view, focus ->
            if (focus) {
                Toast.makeText(applicationContext, "Got the focus", Toast.LENGTH_LONG).show()
                searchDetailLinear.visibility = View.INVISIBLE
                searchListLinear.visibility = View.VISIBLE
            } else {
                Toast.makeText(applicationContext, "Lost the focus", Toast.LENGTH_LONG).show()
                searchDetailLinear.visibility = View.VISIBLE
                searchListLinear.visibility = View.INVISIBLE
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item!!.itemId) {
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


    //BottomBarのボタン処理
    private val ON_NAVIGATION_ITEM_SELECTED_LISTENER = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_favorite -> {
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
}
