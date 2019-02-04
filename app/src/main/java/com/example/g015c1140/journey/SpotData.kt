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

/*
* スポットの追加APIには以下が必要
* user_id	    int	Fkey(user)　             //Post時にSettingクラスから持ってくる
* spot_title	varchar(255)	not null    //SpotDataから
* spot_address	point	        not null    //SpotDataから生成
* spot_comment	text	                    //SpotDataから
* spot_image_A	text	                    //現時点ではnull
* spot_image_B	text	                    //null
* spot_image_C	text	                    //null
* */