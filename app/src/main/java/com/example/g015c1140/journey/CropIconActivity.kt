package com.example.g015c1140.journey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.isseiaoki.simplecropview.CropImageView




class CropIconActivity : AppCompatActivity() {

    lateinit var cropImageView: CropImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_icon)


        val intent = intent
        val bundle = intent.extras
        val bmp = bundle.get("uri") as Uri
        val imageFlg = bundle.get("imageFlg") as Int

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, bmp)
        cropImageView = findViewById<View>(R.id.cropImageView) as CropImageView

        when(imageFlg) {
            1 -> {
                cropImageView.setCropMode(CropImageView.CropMode.RATIO_16_9)
                cropImageView.setOutputMaxSize(410, 160)
                cropImageView.setOutputWidth(410)
                cropImageView.setOutputHeight(160)
                cropImageView.setMinFrameSizeInDp(80)
                cropImageView.setInitialFrameScale(0.70f)

            }
            2 -> {
                cropImageView.setCropMode(CropImageView.CropMode.CIRCLE)
                cropImageView.setOutputMaxSize(100, 100)
                cropImageView.setOutputWidth(100)
                cropImageView.setOutputHeight(100)
                cropImageView.setMinFrameSizeInDp(100)
                cropImageView.setInitialFrameScale(0.70f)
            }
        }

/*
        cropImageView.setBackgroundColor(0x000000)
        cropImageView.setOverlayColor(0x5BB2FD)
        cropImageView.setFrameColor(R.color.colorPrimary)
        cropImageView.setHandleColor(R.color.colorPrimary)
        cropImageView.setGuideColor(R.color.colorPrimary)
*/

        // トリミングしたい画像をセット
        cropImageView.imageBitmap = bitmap

        val cropButton = findViewById<View>(R.id.crop_button) as Button
        cropButton.setOnClickListener {
            // フレームに合わせてトリミング
            val cropBmp = cropImageView.croppedBitmap
            val myApp = this.application as MyApplication
            myApp.setBmp_1(cropBmp)
            setResult(RESULT_OK, Intent())
            finish()
        }
    }

    fun clockwiseButtonTapped(v:View) {
        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D)
    }

    fun counterClockwiseButtonTapped(v:View) {
        cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_M90D)
    }

}
