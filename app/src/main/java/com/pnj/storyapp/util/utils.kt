package com.pnj.storyapp.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ValidateType {
    const val REQUIRED = 0
    const val MIN_CHAR = 1
    const val MAX_CHAR = 2
    const val EMAIL = 3

}

private const val MAXIMAL_SIZE = 1000000

fun File.reduceFileImage(): File {
    val file = this
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > MAXIMAL_SIZE)
    bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun uriToFile(imageUri: Uri, context: Context): File {
    val file = createCustomTempFile(context)
    val inputStream = context.contentResolver.openInputStream(imageUri) as InputStream
    val outputStream = FileOutputStream(file)
    val buffer = ByteArray(1024)
    var length: Int
    while (inputStream.read(buffer).also { length = it } > 0) outputStream.write(buffer, 0, length)
    outputStream.close()
    inputStream.close()
    return file
}

private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
private val timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())

fun createCustomTempFile(context: Context): File {
    val filesDir = context.externalCacheDir
    return File.createTempFile(timeStamp, ".jpg", filesDir)
}

fun ImageView.loadImage(context: Context, url: String) {
    Glide.with(context)
        .load(url)
        .into(this)
}

fun EditText.validate(errorMessage: String, validateType: Int, num: Int = 0): Boolean {
    val text = this.text.toString().trim()
    when (validateType) {
        ValidateType.REQUIRED -> {
            if (text.isEmpty()) {
                this.error = errorMessage
                return false
            }
        }

        ValidateType.MIN_CHAR -> {
            if (text.length < num) {
                this.error = errorMessage
                return false
            }
        }

        ValidateType.MAX_CHAR -> {
            if (text.length > num) {
                this.error = errorMessage
                return false
            }
        }

        ValidateType.EMAIL -> {
            if (!text.contains("@")) {
                this.error = errorMessage
                return false
            }
        }
    }
    return true
}

fun MaterialButton.showLoading(isLoading: Boolean, defaultIcon: Drawable? = null) {
    if (isLoading) {
        val spec =
            CircularProgressIndicatorSpec(
                this.context,
                null,
                0,
                R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
            )
        val progressbar =
            IndeterminateDrawable.createCircularDrawable(
                this.context,
                spec
            )
        this.icon = progressbar
        this.isEnabled = false
    } else {
        this.icon = defaultIcon
        this.isEnabled = true
    }
}

fun AppCompatActivity.showToast(msg: String, isLong: Boolean = false) {
    if (isLong) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    } else {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}