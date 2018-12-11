package com.example.g015c1140.journey

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class DetailPlanSpotListAdapter(internal var context: Context) : BaseAdapter() {

    private var layoutInflater: LayoutInflater? = null
    private lateinit var detailPlanSpotList: ArrayList<DetailPlanSpotData>

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setDetailPlanSpotList(detailPlanSpotList: ArrayList<DetailPlanSpotData>) {
        this.detailPlanSpotList = detailPlanSpotList
    }

    override fun getCount(): Int {
        return detailPlanSpotList.size
    }

    override fun getItem(position: Int): Any {
        return detailPlanSpotList[position]
    }

    override fun getItemId(position: Int): Long {
        return detailPlanSpotList[position].spotId
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view: View? = convertView
        view = layoutInflater!!.inflate(R.layout.detail_plan_spot_row, parent, false)
        (view.findViewById(R.id.detailPlanSpotTitleTextView) as TextView).text = (detailPlanSpotList[position].spotTitle)
        (view.findViewById(R.id.detailPlanSpotImageView) as ImageView).setImageBitmap( detailPlanSpotList[position].spotImage )
        (view.findViewById(R.id.detailPlanSpotCommentTextView) as TextView).text = detailPlanSpotList[position].spotComment
        return view!!
    }
}