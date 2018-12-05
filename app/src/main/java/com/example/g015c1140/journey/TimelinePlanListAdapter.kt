package layout

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.g015c1140.journey.DetailUserActivity
import com.example.g015c1140.journey.R
import com.example.g015c1140.journey.TimelinePlan


class TimelinePlanListAdapter(internal var context: Context,internal val activity: Activity) : BaseAdapter() {

    internal var layoutInflater: LayoutInflater? = null
    internal lateinit var timelinePlanList: ArrayList<TimelinePlan>
    val ACTIVITY  = activity

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setTimelinePlanList(timelinePlanList: ArrayList<TimelinePlan>) {
        this.timelinePlanList = timelinePlanList
    }

    override fun getCount(): Int {
        return timelinePlanList.size
    }

    override fun getItem(position: Int): Any {
        return timelinePlanList[position]
    }

    override fun getItemId(position: Int): Long {
        return timelinePlanList[position].planId
    }

    fun getPlanTitle(position: Int): String {
        return timelinePlanList[position].planTitle
    }


    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var view: View? = convertView

        Log.d("ニコニコ", "動画")

        view = layoutInflater!!.inflate(R.layout.timeline_row, parent, false)
        (view.findViewById(R.id.planUserIconCircleImage) as ImageView).setImageBitmap(timelinePlanList[position].planUserIconImage)
        (view.findViewById(R.id.planUserNameTextView) as TextView).text = timelinePlanList[position].planUserName
        (view.findViewById(R.id.planTitleTextView) as TextView).text = timelinePlanList[position].planTitle
        (view.findViewById(R.id.planSpotImageView) as ImageView).setImageBitmap(timelinePlanList[position].planSpotImage)
        (view.findViewById(R.id.planSpotName1TextView) as TextView).text = timelinePlanList[position].planSpotTitleList[0]
        (view.findViewById(R.id.planSpotName2TextView) as TextView).text = timelinePlanList[position].planSpotTitleList[1]
        (view.findViewById(R.id.planSpotName3TextView) as TextView).text = timelinePlanList[position].planSpotTitleList[2]
        (view.findViewById(R.id.planTimeTextView) as TextView).text = timelinePlanList[position].planTime
        (view.findViewById(R.id.planFavoriteTextView) as TextView).text = timelinePlanList[position].planFavorite


        (view.findViewById(R.id.planUserIconCircleImage) as ImageView).setOnClickListener {
            // イメージ画像がクリックされたときに実行される処理
            ACTIVITY.startActivity(Intent(context,DetailUserActivity::class.java))
        }

        return view!!
    }
}