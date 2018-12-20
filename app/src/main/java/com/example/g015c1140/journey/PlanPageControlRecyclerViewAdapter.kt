package com.example.g015c1140.journey

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class PlanPageControlRecyclerViewAdapter(context: Context, activity: Activity, timelineList: ArrayList<TimelinePlanData>) : RecyclerView.Adapter<PlanPageControlRecyclerViewAdapter.ViewHolder>() {

    private var layoutInflater: LayoutInflater? = null
    private val CONTEXT = context
    private val ACTIVITY = activity
    private var timelinePlanDataList: ArrayList<TimelinePlanData> = timelineList

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    // [7]
    override fun getItemCount(): Int {
        return timelinePlanDataList.size
    }

    fun getItem(position: Int): Any {
        return timelinePlanDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    // [5]
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanPageControlRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timeline_row, parent, false)
        val holder = ViewHolder(view)
        view.setOnClickListener {
            val position = holder.adapterPosition // positionを取得
            // 何かの処理をします
            Toast.makeText(CONTEXT, "アイテムたっぷ $position", Toast.LENGTH_SHORT).show()
        }
        return holder
    }

    // [3]
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val planUserIconCircleImage = (view.findViewById(R.id.planUserIconCircleImage) as ImageView)
        val planUserNameTextView = (view.findViewById(R.id.planUserNameTextView) as TextView)
        val planTitleTextView = (view.findViewById(R.id.planTitleTextView) as TextView)
        val planSpotImageView = (view.findViewById(R.id.planSpotImageView) as ImageView)
        val planSpotName1TextView = (view.findViewById(R.id.planSpotName1TextView) as TextView)
        val planSpotName2TextView = (view.findViewById(R.id.planSpotName2TextView) as TextView)
        val planSpotName3TextView = (view.findViewById(R.id.planSpotName3TextView) as TextView)
        val planTimeTextView = (view.findViewById(R.id.planTimeTextView) as TextView)
        val planFavoriteTextView = (view.findViewById(R.id.planFavoriteTextView) as TextView)
    }

    // [6]
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.planUserIconCircleImage.setImageBitmap(timelinePlanDataList[position].planUserIconImage)
        holder.planUserIconCircleImage.tag = timelinePlanDataList[position].userId
        holder.planUserNameTextView.text = timelinePlanDataList[position].planUserName
        holder.planTitleTextView.text = timelinePlanDataList[position].planTitle
        holder.planSpotImageView.setImageBitmap(timelinePlanDataList[position].planSpotImage)
        val planSpotTitleList = timelinePlanDataList[position].planSpotTitleList
        holder.planSpotName1TextView.text = planSpotTitleList[0]
        holder.planSpotName2TextView.text = planSpotTitleList[1]
        holder.planSpotName3TextView.text = planSpotTitleList[2]

        holder.planTimeTextView.text = timelinePlanDataList[position].planTime
        holder.planFavoriteTextView.text = timelinePlanDataList[position].planFavorite

        (holder.planUserIconCircleImage.findViewById(R.id.planUserIconCircleImage) as ImageView).setOnClickListener {
            // イメージ画像がクリックされたときに実行される処理
            Log.d("test", "tag ${it.tag}")
//            ACTIVITY.startActivity(Intent(CONTEXT,DetailUserActivity::class.java).putExtra("ANOTHER_USER",true).putExtra("USER_ID", (it.tag) as String))
            Toast.makeText(CONTEXT, "いめーじたっぷ${it.tag}", Toast.LENGTH_SHORT).show()
        }
    }
}