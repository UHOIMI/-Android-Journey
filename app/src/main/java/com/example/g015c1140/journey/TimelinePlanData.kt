package com.example.g015c1140.journey

import android.graphics.Bitmap

data class TimelinePlanData (
    var planId: Long = 0,
    var userId: String = "",
    var planUserIconImage: Bitmap? = null,
    var planUserName: String = "",
    var planTitle: String = "",
    var planSpotImage: Bitmap? = null,
    var planSpotTitleList: ArrayList<String> = arrayListOf(),
    var planTime: String = "",
    var planFavorite: String = ""
)