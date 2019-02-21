package com.example.g015c1140.journey

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

    internal class ViewHolder {
        var sTitle: TextView? = null
        var sImg: ImageView? = null
        var sComme: TextView? = null
    }

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val (viewHolder, view) = when (convertView) {
            null -> {
                val view = layoutInflater!!.inflate(R.layout.detail_plan_spot_row, parent, false)
                val viewHolder = ViewHolder()
                viewHolder.sTitle = view.findViewById(R.id.detailPlanSpotTitleTextView)
                viewHolder.sImg = view.findViewById(R.id.detailPlanSpotImageView)
                viewHolder.sComme = view.findViewById(R.id.detailPlanSpotCommentTextView)

                view.tag = viewHolder
                viewHolder to view
            }
            else -> convertView.tag as ViewHolder to convertView
        }

        viewHolder.sTitle!!.text = detailPlanSpotList[position].spotTitle
        viewHolder.sImg!!.setImageBitmap(detailPlanSpotList[position].spotImage)
        viewHolder.sComme!!.text = detailPlanSpotList[position].spotComment
        return view
    }
}