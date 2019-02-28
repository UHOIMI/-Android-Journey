package com.example.g015c1140.journey

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class AreaPageControlRecyclerViewAdapter(context: Context, activity: Activity, areaList: ArrayList<HomeAreaData>) : RecyclerView.Adapter<AreaPageControlRecyclerViewAdapter.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null
    private val CONTEXT = context
    private val ACTIVITY = activity
    private var areaDataList: ArrayList<HomeAreaData> = areaList

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getItemCount(): Int {
        return areaDataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaPageControlRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_area_row, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition // positionを取得
            // 何かの処理をします
            ACTIVITY.startActivity(Intent(CONTEXT,TimelineActivity::class.java).putExtra("AREA_FLG",true).putExtra("AREA_NAME",areaDataList[position].areaName).putExtra("AREA_STRING",areaDataList[position].areaApiString))
        }
        return holder
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var areaName = ""
        val areaImageView = (view.findViewById(R.id.areaImageView) as ImageView)
        var areaApiString = ""
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.areaName = areaDataList[position].areaName
        holder.areaImageView.setImageDrawable(areaDataList[position].areaImage)
        holder.areaApiString = areaDataList[position].areaApiString
    }
}