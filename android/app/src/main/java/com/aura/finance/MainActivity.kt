package com.aura.finance

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val SMS_PERMISSION_REQUEST_CODE = 101

    private val transactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.aura.finance.NEW_TRANSACTION") {
                val id = intent.getStringExtra("id") ?: ""
                val title = intent.getStringExtra("title") ?: ""
                val type = intent.getStringExtra("type") ?: "debit"
                val amount = intent.getDoubleExtra("amount", 0.0)
                val category = intent.getStringExtra("category") ?: "General"
                val date = intent.getStringExtra("date") ?: ""

                // Inject live transaction into WebView JavaScript
                val js = "window.onNativeSmsReceived && window.onNativeSmsReceived('$id', '$title', '$type', $amount, '$category', '$date');"
                webView.post {
                    webView.evaluateJavascript(js, null)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // Attach native JavaScript bridge
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // If permissions are already granted, scan inbox
                if (hasSmsPermissions()) {
                    triggerInboxSync()
                }
            }
        }
        webView.webChromeClient = WebChromeClient()

        // Register local broadcast receiver for incoming SMS
        val filter = IntentFilter("com.aura.finance.NEW_TRANSACTION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(transactionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(transactionReceiver, filter)
        }

        // Check SMS permissions on startup
        checkAndRequestSmsPermissions()

        // Load 100% offline local dashboard from assets
        webView.loadUrl("file:///android_asset/index.html")
    }

    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        val receiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        return readSms == PackageManager.PERMISSION_GRANTED && receiveSms == PackageManager.PERMISSION_GRANTED
    }

    fun checkAndRequestSmsPermissions() {
        if (!hasSmsPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            triggerInboxSync()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                triggerInboxSync()
            }
        }
    }

    private fun triggerInboxSync() {
        Thread {
            val transactions = SmsScanner.scanInbox(this, limit = 50)
            if (transactions.isNotEmpty()) {
                val json = WebAppInterface(this).scanInboxSms()
                runOnUiThread {
                    webView.evaluateJavascript("window.onInboxLoaded && window.onInboxLoaded($json);", null)
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(transactionReceiver)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
