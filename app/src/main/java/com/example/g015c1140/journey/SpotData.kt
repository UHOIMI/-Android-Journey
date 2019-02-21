package com.example.g015c1140.journey

import java.io.Serializable
import java.util.*

data class SpotData(
        val id : String,
        var title : String,
        val latitude : Double,
        val longitude : Double,
        var comment: String,
        var image_A: String,
        var image_B: String,
        var image_C: String,
        val dateTime : Date
): Serializable