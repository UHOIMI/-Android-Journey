package com.example.g015c1140.journey

import android.app.Activity
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView

import com.isseiaoki.simplecropview.CropImageView
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore


class CropIconActivity : AppCompatActivity() {


    companion object {
        private const val RESULT_PICK_IMAGEFILE = 1001

        private const val REQUEST_CROP_PICK = 1002

        private const val CROP_RESULT = 1003
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_icon)


        val intent = getIntent()
        val b = intent.extras
        val bmp = b.get("data") as Uri

        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, bmp)


        val cropImageView = findViewById<View>(R.id.cropImageView) as CropImageView

        cropImageView.setCropMode(CropImageView.CropMode.CIRCLE)

        //val croppedImageView = findViewById<View>(R.id.croppedImageView) as ImageView

        // トリミングしたい画像をセット
        cropImageView.setImageBitmap(bitmap)

        val cropButton = findViewById<View>(R.id.crop_button) as Button
        cropButton.setOnClickListener {
            // フレームに合わせてトリミング
            val cropBmp = cropImageView.croppedBitmap

            val intent = Intent(this, EditUserActivity::class.java)
            intent.putExtra("data", cropBmp)
            startActivityForResult(intent, CROP_RESULT)

//            setResult(Activity.RESULT_OK,Intent().putExtra("CROPIMAGE",cropBmp))
//            finish()

/*
            val intent = Intent(this, EditUserActivity::class.java)
            intent.putExtra("data", cropBmp)
            startActivityForResult(intent, REQUEST_CROP_PICK)
*/

        }



    }
}
