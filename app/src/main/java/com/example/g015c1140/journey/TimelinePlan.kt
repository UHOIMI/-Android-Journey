package com.example.g015c1140.journey

import android.graphics.Bitmap

class TimelinePlan {
    var planId: Long = 0
    var planUserIconImage: Bitmap? = null
    var planUserName: String = ""
    var planTitle: String = ""
    var planSpotImage: Bitmap? = null
    var planSpotNameList: ArrayList<String> = arrayListOf()
    var planTime: String = ""
    var planFavorite: String = ""
}