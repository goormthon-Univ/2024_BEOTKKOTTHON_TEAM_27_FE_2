package com.goormthoonuniv.sodong

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


class JavaScriptInterface(private val mContext: Context) {

    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    /**
     * shareInsta
     * @param uriStr - 저장된 이미지 경로
     */
    @JavascriptInterface
    fun shareInsta(uriStr: String) {
        Log.d("###shareInsta", uriStr)
        val shareIntent = Intent(Intent.ACTION_SEND)
        val bundle = Bundle()

        try {
            val uri = Uri.parse(uriStr) ?: throw Exception("Uri is null")

            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.`package` = "com.instagram.android"

            startActivity(mContext, shareIntent, bundle)
        } catch (exception: Exception) {
            exception.printStackTrace()
            Toast.makeText(mContext, exception.message, Toast.LENGTH_LONG).show()
        }
    }

    @JavascriptInterface
    fun share(imgUrl: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val bundle = Bundle()

        try {
            val uri = downloadImageToExternalStorage(imgUrl)

            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)

            startActivity(mContext, Intent.createChooser(shareIntent, "공유하기"), bundle)
        } catch (exception: Exception) {
            exception.printStackTrace()
            Toast.makeText(mContext, exception.message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * downloadImage
     * @param imgUrl - 다운받을 이미지 url
     * @return 저장된 이미지의 경로 (실패한 경우 "")
     */
    @JavascriptInterface
    fun downloadImage(imgUrl: String): String {
        Log.d("###downloadImage", imgUrl)
        try {
            return downloadImageToExternalStorage(imgUrl)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
        }

        return ""
    }


    private fun downloadImageToExternalStorage(imageUrl: String): String {
        val connection: HttpURLConnection?

        connection = URL(imageUrl).openConnection() as HttpURLConnection
        connection.connect()
        val inputStream: InputStream = connection.inputStream
        val bufferedInputStream = BufferedInputStream(inputStream)
        val bitmap =  BitmapFactory.decodeStream(bufferedInputStream)

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA)
        val filename = "sodong_${java.lang.String.valueOf(sdf.format(Date()))}.jpg"

        var uri: Uri? = null

        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mContext.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }

                uri = imageUri
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

            uri = Uri.fromFile(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

        return uri.toString()
    }

    /**
     * openApp
     * @param packageName - 앱 패키지 명 (com.kakao.talk - 카카오톡, com.towneers.www - 당근마켓, com.kakao.yellowid - 카카오톡 채널)
     */
    @JavascriptInterface
    fun openApp(packageName: String) {
        Log.d("###openApp", packageName)

        val bundle = Bundle()
        try {
            val intent: Intent = mContext.packageManager.getLaunchIntentForPackage(packageName)
                ?: throw Exception("Intent is null")

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(mContext, intent, bundle)
        } catch (exception: Exception) {
            exception.printStackTrace()
            Toast.makeText(mContext, "앱을 설치해 주세요", Toast.LENGTH_LONG).show()

            val url = "market://details?id=$packageName"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(mContext, intent, bundle)
        }
    }

}