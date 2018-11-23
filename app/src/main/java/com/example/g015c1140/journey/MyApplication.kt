package com.example.g015c1140.journey

import android.app.Application
import android.graphics.Bitmap

class MyApplication : Application() {

    private var bmp: Bitmap? = null

    fun setBmp(bmp: Bitmap) {
        this.bmp = bmp
    }

    fun getBmp(): Bitmap? {
        return bmp
    }

    fun clearBmp() {
        bmp = null
    }
}