package com.example.g015c1140.journey

import android.graphics.Bitmap

data class DetailPlanSpotData(
        var spotId : Long = 0,
        var spotTitle : String = "",
        var spotImage: Bitmap? = null,
        var spotComment: String = ""
)