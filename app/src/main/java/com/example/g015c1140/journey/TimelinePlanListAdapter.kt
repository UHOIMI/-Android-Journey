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
import com.example.g015c1140.journey.TimelinePlanData
import de.hdodenhof.circleimageview.CircleImageView


class TimelinePlanListAdapter(internal var context: Context,internal val activity: Activity) : BaseAdapter() {

    internal var layoutInflater: LayoutInflater? = null
    internal lateinit var timelinePlanDataList: ArrayList<TimelinePlanData>
    val ACTIVITY  = activity

    internal class ViewHolder {
        var uIcon: CircleImageView? = null
        var uName: TextView? = null
        var pTitle: TextView? = null
        var sImg: ImageView? = null
        var sNameList: ArrayList<TextView>? = null
        var pTime: TextView? = null
        var pFavo: TextView? = null
    }

    init {
        this.layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    fun setTimelinePlanList(timelinePlanDataList: ArrayList<TimelinePlanData>) {
        this.timelinePlanDataList = timelinePlanDataList
    }

    override fun getCount(): Int {
        return timelinePlanDataList.size
    }

    override fun getItem(position: Int): Any {
        return timelinePlanDataList[position]
    }

    override fun getItemId(position: Int): Long {
        return timelinePlanDataList[position].planId
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val (viewHolder, view) = when (convertView) {
            null -> {
                val view = layoutInflater!!.inflate(R.layout.timeline_row, parent, false)
                val viewHolder = ViewHolder()
                viewHolder.uIcon = view.findViewById(R.id.planUserIconCircleImage)
                viewHolder.uName = view.findViewById(R.id.planUserNameTextView)
                viewHolder.pTitle = view.findViewById(R.id.planTitleTextView)
                viewHolder.sImg = view.findViewById(R.id.planSpotImageView)
                viewHolder.sNameList = arrayListOf(
                        view.findViewById(R.id.planSpotName1TextView),
                        view.findViewById(R.id.planSpotName2TextView),
                        view.findViewById(R.id.planSpotName3TextView)
                )
                viewHolder.pTime = view.findViewById(R.id.planTimeTextView)
                viewHolder.pFavo = view.findViewById(R.id.planFavoriteTextView)

                view.tag = viewHolder
                viewHolder to view
            }
            else -> convertView.tag as ViewHolder to convertView
        }

        viewHolder.uIcon!!.setImageBitmap(timelinePlanDataList[position].planUserIconImage)
        viewHolder.uIcon!!.tag = timelinePlanDataList[position].userId
        viewHolder.uName!!.text = timelinePlanDataList[position].planUserName
        viewHolder.pTitle!!.text = timelinePlanDataList[position].planTitle
        viewHolder.sImg!!.setImageBitmap(timelinePlanDataList[position].planSpotImage)
        viewHolder.sNameList!![0].text = timelinePlanDataList[position].planSpotTitleList[0]
        viewHolder.sNameList!![1].text = timelinePlanDataList[position].planSpotTitleList[1]
        viewHolder.sNameList!![2].text = timelinePlanDataList[position].planSpotTitleList[2]
        viewHolder.pTime!!.text = timelinePlanDataList[position].planTime
        viewHolder.pFavo!!.text = timelinePlanDataList[position].planFavorite


        viewHolder.uIcon!!.setOnClickListener {
            // イメージ画像がクリックされたときに実行される処理
            ACTIVITY.startActivity(Intent(context,DetailUserActivity::class.java).putExtra("ANOTHER_USER",true).putExtra("USER_ID", (it.tag) as String))
        }

        return view

/*        var view: View? = convertView

        Log.d("ニコニコ", "動画")

        view = layoutInflater!!.inflate(R.layout.timeline_row, parent, false)
        (view.findViewById(R.id.planUserIconCircleImage) as ImageView).setImageBitmap(timelinePlanDataList[position].planUserIconImage)
        (view.findViewById(R.id.planUserIconCircleImage) as ImageView).tag = timelinePlanDataList[position].userId
        (view.findViewById(R.id.planUserNameTextView) as TextView).text = timelinePlanDataList[position].planUserName
        (view.findViewById(R.id.planTitleTextView) as TextView).text = timelinePlanDataList[position].planTitle
        (view.findViewById(R.id.planSpotImageView) as ImageView).setImageBitmap(timelinePlanDataList[position].planSpotImage)
        (view.findViewById(R.id.planSpotName1TextView) as TextView).text = timelinePlanDataList[position].planSpotTitleList[0]
        (view.findViewById(R.id.planSpotName2TextView) as TextView).text = timelinePlanDataList[position].planSpotTitleList[1]
        (view.findViewById(R.id.planSpotName3TextView) as TextView).text = timelinePlanDataList[position].planSpotTitleList[2]
        (view.findViewById(R.id.planTimeTextView) as TextView).text = timelinePlanDataList[position].planTime
        (view.findViewById(R.id.planFavoriteTextView) as TextView).text = timelinePlanDataList[position].planFavorite


        (view.findViewById(R.id.planUserIconCircleImage) as ImageView).setOnClickListener {
            // イメージ画像がクリックされたときに実行される処理
            ACTIVITY.startActivity(Intent(context,DetailUserActivity::class.java).putExtra("ANOTHER_USER",true).putExtra("USER_ID", (it.tag) as String))
        }

        return view!!*/
    }
}