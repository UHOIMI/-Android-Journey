package com.example.g015c1140.journey

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.util.*

class SpotListAdapter(internal var context: Context) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    internal lateinit var spotList: ArrayList<ListSpot>
    private var grayFlagList = arrayListOf<Boolean>()

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setSpotList(spotList: ArrayList<ListSpot>) {
        this.spotList = spotList
        for (_sl in spotList) {
            grayFlagList.add(false)
        }
    }

    fun setGray(position: Int, flag: Boolean) {
        grayFlagList[position] = flag
    }

    override fun getCount(): Int {
        return spotList.size
    }

    override fun getItem(position: Int): Any {
        return spotList[position]
    }

    override fun getItemId(position: Int): Long {
        return spotList[position].id
    }

    fun getName(position: Int): String {
        return spotList[position].name!!
    }

    fun reverseColor() {
        grayFlagList.reverse()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View? = convertView
        val obj: Any? = getItem(position)

        Log.d("ニコニコ", "動画")

        if (view == null) {
            view = layoutInflater!!.inflate(R.layout.spot_row, parent, false)
            (view.findViewById(R.id.title) as TextView).text = spotList[position].name
            (view.findViewById(R.id.datetime) as TextView).text = spotList[position].datetime
            //view.setBackgroundColor(Color.GRAY)
        } else if (obj != null) {
            if (grayFlagList[position]) {
                view.setBackgroundColor(Color.GRAY)
                view.isEnabled = false
            } else {
                view.setBackgroundColor(Color.WHITE)
                view.isEnabled = true
            }
            (view.findViewById(R.id.title) as TextView).text = spotList[position].name
            (view.findViewById(R.id.datetime) as TextView).text = spotList[position].datetime
        }

        return view!!
    }
}
