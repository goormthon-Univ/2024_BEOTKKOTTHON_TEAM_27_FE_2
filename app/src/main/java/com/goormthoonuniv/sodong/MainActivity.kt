package com.goormthoonuniv.sodong

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.goormthoonuniv.sodong.databinding.ActivityMainBinding


class MainActivity: BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private lateinit var webView: WebView
    private val url = "https://sodong.pages.dev/"

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

        // TODO: 크롬 인스펙터 확인
        WebView.setWebContentsDebuggingEnabled(true)
    }

    private val webViewClient: WebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return false
        }
    }
}