package com.example.g015c1140.journey

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

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

    fun getItem(position: Int): Any {
        return areaDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaPageControlRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.home_area_row, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition // positionを取得
            // 何かの処理をします
//            ACTIVITY.startActivity(Intent(CONTEXT,DetailUserActivity::class.java).putExtra("ANOTHER_USER",true).putExtra("USER_ID", (it.tag) as String))
            Toast.makeText(CONTEXT, "areaたっぷ ${areaDataList[position].areaName}", Toast.LENGTH_SHORT).show()
        }
        return holder
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val areaImageView = (view.findViewById(R.id.areaImageView) as ImageView)
        val areaNameTextView = (view.findViewById(R.id.areaNameTextView) as TextView)
        var areaApiString = ""
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.areaImageView.setImageBitmap(areaDataList[position].areaImage)
        holder.areaNameTextView.text = areaDataList[position].areaName
        holder.areaApiString = areaDataList[position].areaApiString
    }
}