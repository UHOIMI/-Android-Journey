package com.example.g015c1140.journey

import android.content.Context
import android.support.annotation.Nullable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout

class PageControlView : LinearLayout {

    //private var imageViews: Array<ImageView>? = null
    var imageViews : MutableList<ImageView> = mutableListOf()

    constructor(context: Context):super(context)
    constructor(context: Context, @Nullable attrs: AttributeSet?):super(context, attrs, 0)
    constructor(context: Context, @Nullable attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)

    fun setRecyclerView(recyclerView: RecyclerView, layoutManager: LinearLayoutManager) {
        val itemCount = layoutManager.itemCount
        if (itemCount < 1) {
            return
        }
        createPageControl(itemCount)

        // RecyclerViewのスクロールに合わせてドットを切り替え
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position = layoutManager.findLastVisibleItemPosition()
                val scrollItemCount = layoutManager.itemCount

                if (scrollItemCount != null){
                    for (i in 0 until itemCount) {
                        imageViews[i].setImageResource(R.drawable.shape_page_control_default)
                    }
                    imageViews[position].setImageResource(R.drawable.shape_page_control_selected)
                }
            }
        })
    }

    fun createPageControl (itemCount : Int) {

        for (i in 0 until itemCount){
            imageViews.add(ImageView(context))
        }

        for (i in 0 until imageViews.size){
            imageViews[i] = ImageView(context)
            imageViews[i].setImageResource(R.drawable.shape_page_control_default)

            val dotSize = resources.getDimensionPixelSize(R.dimen.page_control_dot_size)
            val params = LinearLayout.LayoutParams(dotSize, dotSize)
            params.setMargins(dotSize, 0, dotSize / 2, 0)
            imageViews[i].layoutParams = params
            addView(imageViews[i])
        }

        imageViews[0].setImageResource(R.drawable.shape_page_control_selected)

    }
}