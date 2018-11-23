package com.example.g015c1140.journey

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_create_user.*

class CreateUserActivity : AppCompatActivity() {

    var userIcon = ""
    var userData = arrayListOf<String>()


    companion object {
        private const val RESULT_PICK_IMAGEFILE = 1001
        private const val RESULT_CROP = 2003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString().matches("^[a-zA-Z0-9]+$".toRegex())) {
                source
            } else {
                ""
            }
        }
        val lengthFilter = InputFilter.LengthFilter(20)

        createIdEditText.filters = arrayOf(inputFilter, lengthFilter)
        passwordEditText.filters = arrayOf(inputFilter, lengthFilter)
        confirmationPassEditText.filters = arrayOf(inputFilter, lengthFilter)

        val editFlg = intent.getIntExtra("EditFlg", -1)

        if (editFlg == 100) {
            userData = intent.extras.getStringArrayList("USERDATA")

            if (userData[0] == "OK") {
                val myApp: MyApplication = this.application as MyApplication
                val bmp = myApp.getBmp()
                myApp.clearBmp()
                iconImageButton.setImageBitmap(bmp)
                userIcon  = "OK"
            }
            createIdEditText.setText(userData[1])
            nameEditText.setText(userData[2])
            passwordEditText.setText(userData[3])
            confirmationPassEditText.setText(userData[3])
            generationSpinner.setSelection((generationSpinner.adapter as ArrayAdapter<String>).getPosition(userData[4]))
            genderSpinner.setSelection((genderSpinner.adapter as ArrayAdapter<String>).getPosition(userData[5]))
        }
    }

    fun onClickImage(v: View) {
        // イメージ画像がクリックされたときに実行される処理
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, RESULT_PICK_IMAGEFILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == RESULT_PICK_IMAGEFILE && resultCode == Activity.RESULT_OK) {
            var uri: Uri? = null
            if (resultData != null) {
                uri = resultData.data
                val intent = Intent(this, CropIconActivity::class.java)
                intent.putExtra("uri", uri)
                intent.putExtra("imageFlg", 2)
                startActivityForResult(intent, RESULT_CROP)
            }
        }

        if (requestCode == RESULT_CROP && resultCode == Activity.RESULT_OK) {
            val myApp: MyApplication = this.application as MyApplication
            val bmp = myApp.getBmp()
            myApp.clearBmp()
            iconImageButton.setImageBitmap(bmp)
            userIcon  = "OK"
        }
    }

    fun onDoneButtonTapped(v: View) {
        var result = ""
        if (createIdEditText.text.toString().isEmpty()) {
            result += "ユーザーIDを入力してください\n"
            checkFailure(result)
        } else {
            val guiat = GetUserIdAsyncTask()
            guiat.setOnCallback(object : GetUserIdAsyncTask.CallbackGetUserIdAsyncTask() {
                override fun callback(resultArray: ArrayList<String>) {
                    super.callback(resultArray)
                    // ここからAsyncTask処理後の処理を記述します。
                    Log.d("test GetSpotCallback", "非同期処理$resultArray")
                    if (resultArray[0] == "RESULT-OK") {
                        resultArray.removeAt(0)
                        for (value in resultArray) {
                            if (createIdEditText.text.toString() == value) {
                                result += "このユーザーIDはすでに使用されています\n"
                                break
                            }
                        }
                        /******************/
                        if (!(nameEditText.text.toString().trim().isEmpty())) {
                        } else {
                            result += "ユーザー名を入力してください\n"
                        }

                        if (!(passwordEditText.text.toString().isEmpty())) {
                            //文字ある
                            if (passwordEditText.text.toString().length < 8) {
                                //7文字以下
                                result += "パスワードは8文字以上で入力してください\n"
                            } else {
                                //確認パスワード
                                if (!(confirmationPassEditText.text.toString().isEmpty())) {
                                    //文字ある
                                    if (confirmationPassEditText.text.toString().length < 8) {
                                        result += "確認用パスワードは8文字以上で入力してください\n"
                                    } else {
                                        if (passwordEditText.text.toString() != confirmationPassEditText.text.toString()) {
                                            result += "パスワードと確認用パスワードが一致しません\n"
                                        }
                                    }
                                } else {
                                    result += "確認用パスワードを入力してください\n"
                                }
                            }
                        } else {
                            result += "パスワードを入力してください\n"
                        }

                        if (generationSpinner.selectedItem.toString() == "あなたの年代を選択してください") {
                            result += "年代を選択してください\n"
                        }
                        if (genderSpinner.selectedItem.toString() == "あなたの性別を選択してください") {
                            result += "性別を選択してください\n"
                        }

                        if (result == "") {
                            //成功
                            checkSuccess()
                        } else {
                            checkFailure(result)
                        }
                        /******************/
                    }else{
                        result += "ユーザーIDを確認してください"
                        checkFailure(result)
                    }
                }
            })
            guiat.execute()
        }
    }

    private fun checkSuccess() {
        userData = arrayListOf(
                userIcon,
                createIdEditText.text.toString().trim(),
                nameEditText.text.toString().trim(),
                passwordEditText.text.toString(),
                generationSpinner.selectedItem.toString(),
                genderSpinner.selectedItem.toString()
        )
        startActivity(Intent(this, ConfirmationActivity::class.java).putStringArrayListExtra("USERDATA", userData))
        if (userIcon == "OK"){
            val myApp = this.application as MyApplication
            myApp.setBmp((iconImageButton.drawable as BitmapDrawable).bitmap)
        }
        finish()
    }

    private fun checkFailure(string: String) {
        AlertDialog.Builder(this).apply {
            setTitle("入力情報が間違っています")
            setMessage(string)
            setPositiveButton("確認", null)
            show()
        }
    }
}
