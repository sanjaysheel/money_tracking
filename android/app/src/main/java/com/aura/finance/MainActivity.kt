package com.aura.finance

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.provider.Settings
import android.util.Base64
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val SMS_PERMISSION_REQUEST_CODE = 101

    private val transactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.aura.finance.NEW_TRANSACTION") {
                val obj = JSONObject().apply {
                    put("id", intent.getStringExtra("id") ?: "")
                    put("title", intent.getStringExtra("title") ?: "")
                    put("type", intent.getStringExtra("type") ?: "debit")
                    put("amount", intent.getDoubleExtra("amount", 0.0))
                    put("category", intent.getStringExtra("category") ?: "General")
                    put("date", intent.getStringExtra("date") ?: "")
                    put("rawText", intent.getStringExtra("rawText") ?: "")
                    put("balance", intent.getDoubleExtra("balance", -1.0))
                    put("accountTail", intent.getStringExtra("accountTail") ?: "")
                    put("paymentMethod", intent.getStringExtra("paymentMethod") ?: "Card/UPI")
                    put("currencySymbol", intent.getStringExtra("currencySymbol") ?: "₹")
                    put("dedupKey", intent.getStringExtra("dedupKey") ?: "")
                }

                val jsonStr = obj.toString()
                val b64 = Base64.encodeToString(jsonStr.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val js = "window.onNativeSmsReceivedBase64 && window.onNativeSmsReceivedBase64('$b64');"
                webView.post {
                    webView.evaluateJavascript(js, null)
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge window rendering to eliminate system black gaps
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        webView = WebView(this)
        webView.setBackgroundColor(Color.parseColor("#030712"))
        setContentView(webView)

        // Listen for system window insets (status bar, navigation bar & IME keyboard) and bridge to WebView
        ViewCompat.setOnApplyWindowInsetsListener(webView) { _, windowInsets ->
            val statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val density = resources.displayMetrics.density
            val topDp = if (statusBars.top > 0) (statusBars.top / density).toInt() else 36
            val bottomDp = if (navBars.bottom > 0) (navBars.bottom / density).toInt() else 24
            val imeDp = if (ime.bottom > 0) (ime.bottom / density).toInt() else 0
            val isKeyboardUp = windowInsets.isVisible(WindowInsetsCompat.Type.ime()) || (ime.bottom > navBars.bottom && imeDp > 80)
            val activeKeyboardDp = if (isKeyboardUp) imeDp else 0

            val js = "if (document.documentElement) { " +
                     "document.documentElement.style.setProperty('--android-status-bar-height', '${topDp}px'); " +
                     "document.documentElement.style.setProperty('--android-nav-bar-height', '${bottomDp}px'); " +
                     "document.documentElement.style.setProperty('--android-keyboard-height', '${activeKeyboardDp}px'); " +
                     "if (window.onNativeKeyboardChange) { window.onNativeKeyboardChange($activeKeyboardDp, $isKeyboardUp); } " +
                     "window.dispatchEvent(new CustomEvent('nativekeyboardchange', { detail: { height: $activeKeyboardDp, visible: $isKeyboardUp, navBar: $bottomDp } })); }"
            webView.post {
                webView.evaluateJavascript(js, null)
            }
            windowInsets
        }

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        // Native mobile viewport rendering with proper HTML meta tag respect
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true

        // Attach native JavaScript bridge
        webView.addJavascriptInterface(WebAppInterface(this), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                applyInsetsToDom()
                val hasPerm = hasSmsPermissions()
                webView.evaluateJavascript("window.onPermissionResult && window.onPermissionResult($hasPerm, false);", null)
                if (hasPerm) {
                    val prefs = getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
                    val autoScanOnOpen = prefs.getBoolean("autoscan_on_open", true)
                    if (autoScanOnOpen) {
                        triggerInboxSync()
                    }
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

        // Automatic Permission Request on App Launch (postDelayed ensures Activity window is visible & focused)
        if (!hasSmsPermissions()) {
            webView.postDelayed({
                if (!isFinishing && !hasSmsPermissions()) {
                    checkAndRequestSmsPermissions()
                }
            }, 600)
        } else {
            val prefs = getSharedPreferences("aura_settings", Context.MODE_PRIVATE)
            val autoScanOnOpen = prefs.getBoolean("autoscan_on_open", true)
            if (autoScanOnOpen) {
                triggerInboxSync()
            }
        }

        // Load 100% offline local dashboard from assets
        webView.loadUrl("file:///android_asset/index.html")
    }

    private fun applyInsetsToDom() {
        val rootInsets = ViewCompat.getRootWindowInsets(window.decorView)
        val statusBars = rootInsets?.getInsets(WindowInsetsCompat.Type.statusBars())
        val navBars = rootInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())
        val density = resources.displayMetrics.density
        val topDp = if (statusBars != null && statusBars.top > 0) (statusBars.top / density).toInt() else 36
        val bottomDp = if (navBars != null && navBars.bottom > 0) (navBars.bottom / density).toInt() else 24

        val js = "document.documentElement.style.setProperty('--android-status-bar-height', '${topDp}px'); " +
                 "document.documentElement.style.setProperty('--android-nav-bar-height', '${bottomDp}px');"
        webView.post {
            webView.evaluateJavascript(js, null)
        }
    }

    fun setSystemBarTheme(isDark: Boolean) {
        runOnUiThread {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = !isDark
            controller.isAppearanceLightNavigationBars = !isDark
            val bgColor = if (isDark) Color.parseColor("#030712") else Color.parseColor("#f8fafc")
            webView.setBackgroundColor(bgColor)
        }
    }

    fun hideKeyboard() {
        runOnUiThread {
            try {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                imm?.hideSoftInputFromWindow(webView.windowToken, 0)
            } catch (ignored: Exception) {}
        }
    }

    override fun onResume() {
        super.onResume()
        val hasPerm = hasSmsPermissions()
        webView.post {
            webView.evaluateJavascript("window.onPermissionResult && window.onPermissionResult($hasPerm, false);", null)
        }
    }

    fun hasSmsPermissions(): Boolean {
        val readSms = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        return readSms == PackageManager.PERMISSION_GRANTED
    }

    fun hasReceiveSmsPermission(): Boolean {
        val receiveSms = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
        return receiveSms == PackageManager.PERMISSION_GRANTED
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
            val readGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
            runOnUiThread {
                webView.evaluateJavascript("window.onPermissionResult && window.onPermissionResult($readGranted, true);", null)
            }
            if (readGranted) {
                triggerInboxSync()
            }
        }
    }

    fun triggerInboxSync() {
        runOnUiThread {
            webView.evaluateJavascript("window.onSyncStart && window.onSyncStart();", null)
        }
        Thread {
            try {
                val json = WebAppInterface(this).scanInboxSms()
                val b64 = Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                runOnUiThread {
                    webView.evaluateJavascript("window.onNativeInboxLoadedBase64 && window.onNativeInboxLoadedBase64('$b64');", null)
                }
            } catch (e: Exception) {
                runOnUiThread {
                    webView.evaluateJavascript("window.onSyncError && window.onSyncError('${e.message ?: "Sync error"}');", null)
                }
            }
        }.start()
    }

    fun shareOrSaveFile(filename: String, content: String, mimeType: String) {
        try {
            val cacheDir = File(cacheDir, "exports").apply { mkdirs() }
            val file = File(cacheDir, filename)
            file.writeText(content, Charsets.UTF_8)

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, filename)
                putExtra(Intent.EXTRA_TEXT, "Exported from Aura Finance: $filename")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Export $filename"))
        } catch (e: Exception) {
            // Fallback: Copy to clipboard and toast
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(filename, content)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "$filename copied to clipboard!", Toast.LENGTH_LONG).show()
        }
    }

    private var biometricCancellationSignal: CancellationSignal? = null

    fun getBiometricStatus(): JSONObject {
        val result = JSONObject()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val bm = getSystemService(Context.BIOMETRIC_SERVICE) as? BiometricManager
                if (bm != null) {
                    val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or BiometricManager.Authenticators.BIOMETRIC_STRONG
                    val res = bm.canAuthenticate(authenticators)
                    when (res) {
                        BiometricManager.BIOMETRIC_SUCCESS -> {
                            result.put("available", true)
                            result.put("enrolled", true)
                            result.put("status", "READY")
                            result.put("message", "Fingerprint / Biometric sensor ready & enrolled")
                        }
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                            result.put("available", true)
                            result.put("enrolled", false)
                            result.put("status", "NOT_ENROLLED")
                            result.put("message", "Sensor detected, but no fingerprint is enrolled in Android Settings")
                        }
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                            result.put("available", false)
                            result.put("enrolled", false)
                            result.put("status", "NO_HARDWARE")
                            result.put("message", "No biometric sensor hardware found")
                        }
                        else -> {
                            result.put("available", false)
                            result.put("enrolled", false)
                            result.put("status", "UNAVAILABLE")
                            result.put("message", "Biometrics currently unavailable ($res)")
                        }
                    }
                } else {
                    result.put("available", false)
                    result.put("enrolled", false)
                    result.put("status", "NO_HARDWARE")
                    result.put("message", "BiometricManager service unavailable")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val fm = getSystemService(Context.FINGERPRINT_SERVICE) as? android.hardware.fingerprint.FingerprintManager
                if (fm != null && fm.isHardwareDetected) {
                    val enrolled = fm.hasEnrolledFingerprints()
                    result.put("available", true)
                    result.put("enrolled", enrolled)
                    result.put("status", if (enrolled) "READY" else "NOT_ENROLLED")
                    result.put("message", if (enrolled) "Fingerprint enrolled & ready" else "Fingerprint hardware detected, none enrolled")
                } else {
                    result.put("available", false)
                    result.put("enrolled", false)
                    result.put("status", "NO_HARDWARE")
                    result.put("message", "No fingerprint hardware detected")
                }
            } else {
                result.put("available", false)
                result.put("enrolled", false)
                result.put("status", "UNSUPPORTED")
                result.put("message", "Android version below API 23")
            }
        } catch (e: Exception) {
            result.put("available", false)
            result.put("enrolled", false)
            result.put("status", "ERROR")
            result.put("message", e.message ?: "Unknown error")
        }
        return result
    }

    fun authenticateBiometrics() {
        runOnUiThread {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    biometricCancellationSignal?.cancel()
                    val signal = CancellationSignal()
                    biometricCancellationSignal = signal

                    val promptBuilder = BiometricPrompt.Builder(this)
                        .setTitle("Aura Finance Unlock")
                        .setSubtitle("Touch the fingerprint sensor or confirm biometrics")
                        .setDescription("Verify your identity to access your offline financial records")
                        .setNegativeButton("Enter PIN Instead", mainExecutor) { _, _ ->
                            webView.evaluateJavascript("window.onBiometricFallback && window.onBiometricFallback();", null)
                        }

                    val prompt = promptBuilder.build()
                    prompt.authenticate(signal, mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            super.onAuthenticationSucceeded(result)
                            runOnUiThread {
                                webView.evaluateJavascript("window.onBiometricSuccess && window.onBiometricSuccess();", null)
                            }
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                            super.onAuthenticationError(errorCode, errString)
                            val cleanMsg = (errString?.toString() ?: "Biometric canceled").replace("'", "\\'")
                            runOnUiThread {
                                webView.evaluateJavascript("window.onBiometricError && window.onBiometricError('$cleanMsg', $errorCode);", null)
                            }
                        }

                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            runOnUiThread {
                                webView.evaluateJavascript("window.onBiometricFailed && window.onBiometricFailed();", null)
                            }
                        }
                    })
                } catch (e: Exception) {
                    val err = (e.message ?: "Biometric prompt initialization error").replace("'", "\\'")
                    webView.evaluateJavascript("window.onBiometricError && window.onBiometricError('$err', -1);", null)
                }
            } else {
                webView.evaluateJavascript("window.onBiometricFallback && window.onBiometricFallback();", null)
            }
        }
    }

    fun openBiometricEnrollSettings() {
        runOnUiThread {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                        )
                    }
                    startActivity(enrollIntent)
                } else {
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
            } catch (e: Exception) {
                try {
                    startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                } catch (e2: Exception) {
                    Toast.makeText(this, "Please open Android Settings -> Security to set up Fingerprint", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricCancellationSignal?.cancel()
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
