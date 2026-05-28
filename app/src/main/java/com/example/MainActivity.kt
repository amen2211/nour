package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    // Request Location and Notification permissions after Compose has safely initialized the window
                    requestPermissions()
                }
                WebViewContainer()
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewContainer() {
        var localWebView: WebView? = null

        // Safe Back Gesture handler using modern Compose APIs
        BackHandler(enabled = true) {
            if (localWebView?.canGoBack() == true) {
                localWebView?.goBack()
            } else {
                finish()
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
            factory = { context ->
                WebView(context).apply {
                    localWebView = this
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String,
                            callback: GeolocationPermissions.Callback
                        ) {
                            val fineLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            val coarseLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (fineLocation || coarseLocation) {
                                callback.invoke(origin, true, false)
                            } else {
                                // If not granted, invoke false to trigger JS fallback to IP-based lookup instead of crashing
                                callback.invoke(origin, false, false)
                            }
                        }
                    }
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            return false // Direct all navigation within the WebView itself
                        }
                    }

                    // Register custom secure JavaScript Interface Bridge
                    addJavascriptInterface(AndroidBridge(context), "AndroidBridge")
                    
                    loadUrl("file:///android_asset/index.html")
                }
            }
        )
    }
}
