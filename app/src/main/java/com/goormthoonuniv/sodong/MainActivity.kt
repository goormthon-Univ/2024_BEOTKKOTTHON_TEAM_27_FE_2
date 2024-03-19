package com.goormthoonuniv.sodong

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.goormthoonuniv.sodong.databinding.ActivityMainBinding

const val FILE_CHOOSER = 100

class MainActivity: BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private lateinit var webView: WebView
    private val url = "https://sodong.pages.dev/"
//    private val url = "https://89918aec.sodong.pages.dev/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun initAfterBinding() {
        // 상단바 margin 추가
        val params = binding.webView.layoutParams as ViewGroup.MarginLayoutParams
        params.topMargin = getStatusBarHeight(this)
        binding.webView.layoutParams = params

        webView = binding.webView
        webView.loadUrl(url);

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.addJavascriptInterface(JavaScriptInterface(this), "Android")
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient

        // Android 구분을 위한 User Agent 추가
        webView.settings.userAgentString = webView.settings.userAgentString + " sodong_aos"

        // TODO: 크롬 인스펙터 확인
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }
    }

    var mFileChooserCallback : ValueCallback<Array<Uri>>? = null
    private val webChromeClient: WebChromeClient = object : WebChromeClient() {
        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (mFileChooserCallback != null) {
                mFileChooserCallback = null
            }
            mFileChooserCallback = filePathCallback

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, FILE_CHOOSER)

            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == FILE_CHOOSER){
            Log.d("### onActivityResult","$resultCode  $data")
            if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { uri ->
                    mFileChooserCallback?.onReceiveValue(arrayOf(uri))
                }
            } else {
                mFileChooserCallback?.onReceiveValue(null)
            }
            mFileChooserCallback = null
        }
    }
}