package com.example.g015c1140.journey

import android.app.Application
import android.graphics.Bitmap

class MyApplication : Application() {

    private var bmp_1: Bitmap? = null
    private var bmp_2: Bitmap? = null


    fun setBmp_1(bmp: Bitmap) {
        this.bmp_1 = bmp
    }

    fun setBmp_2(bmp: Bitmap) {
        this.bmp_2 = bmp
    }

    fun getBmp_1(): Bitmap? {
        return bmp_1
    }

    fun getBmp_2(): Bitmap? {
        return bmp_2
    }

    fun clearBmp_1() {
        bmp_1 = null
    }

    fun clearBmp_2() {
        bmp_2 = null
    }
}