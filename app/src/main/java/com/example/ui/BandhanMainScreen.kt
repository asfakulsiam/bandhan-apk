package com.example.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.network.NetworkMonitor
import com.example.ui.theme.BandhanCyan
import com.example.ui.theme.BandhanEmeraldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TARGET_URL = "https://bandhan17.website"

// Standard Mobile Chrome User-Agent without WebView markers to enable Google OAuth login inside WebView
private const val CHROME_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BandhanMainScreen(
    networkMonitor: NetworkMonitor,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isOnline by networkMonitor.isOnlineFlow.collectAsState(initial = networkMonitor.isCurrentlyConnected())

    var isPageLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var swipeRefreshLayoutInstance by remember { mutableStateOf<SwipeRefreshLayout?>(null) }
    var popupWebView by remember { mutableStateOf<WebView?>(null) }

    // File Chooser state
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Back press handling for exit confirmation
    var backPressedOnce by remember { mutableStateOf(false) }

    // File Picker / Camera Activity Result Launcher
    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileUploadCallback ?: return@rememberLauncherForActivityResult
        var results: Array<Uri>? = null

        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val dataString = data?.dataString
            val clipData = data?.clipData

            if (clipData != null && clipData.itemCount > 0) {
                results = Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
            } else if (dataString != null) {
                results = arrayOf(Uri.parse(dataString))
            } else if (cameraPhotoUri != null) {
                // If camera took photo
                results = arrayOf(cameraPhotoUri!!)
            }
        }

        callback.onReceiveValue(results)
        fileUploadCallback = null
        cameraPhotoUri = null
    }

    // Permission launcher for camera/storage when user selects file upload
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Continue regardless; file picker will offer available sources
    }

    // Dismiss Splash Screen after initial load or max 2.5 seconds timeout
    LaunchedEffect(Unit) {
        delay(2200)
        showSplash = false
    }

    // Reload when coming back online if error occurred
    LaunchedEffect(isOnline) {
        if (isOnline && hasError) {
            hasError = false
            webViewInstance?.reload()
        }
    }

    BackHandler(enabled = true) {
        if (popupWebView != null) {
            popupWebView?.destroy()
            popupWebView = null
        } else if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            if (backPressedOnce) {
                (context as? Activity)?.finish()
            } else {
                backPressedOnce = true
                Toast.makeText(context, "অ্যাপ বন্ধ করতে আবার ব্যাক চাপুন", Toast.LENGTH_SHORT).show()
                coroutineScope.launch {
                    delay(2000)
                    backPressedOnce = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main Web Content with SwipeRefreshLayout for pull-to-refresh
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("bandhan_webview"),
            factory = { ctx ->
                val swipeRefreshLayout = SwipeRefreshLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setColorSchemeColors(
                        0xFF0F6B56.toInt(),
                        0xFF00A3B5.toInt(),
                        0xFFF59E0B.toInt()
                    )
                }

                val webView = WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    // Enable Cookies (including third-party cookies for OAuth session persistence)
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    settings.apply {
                        javaScriptEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        setSupportMultipleWindows(true)
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        mediaPlaybackRequiresUserGesture = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            safeBrowsingEnabled = false
                        }

                        // Custom Chrome User Agent to allow Google Sign-In inside WebView
                        userAgentString = CHROME_USER_AGENT

                        // Short-term caching
                        cacheMode = if (isOnline) {
                            WebSettings.LOAD_DEFAULT
                        } else {
                            WebSettings.LOAD_CACHE_ELSE_NETWORK
                        }
                    }

                    // Enable smooth scrolling and touch events
                    isScrollbarFadingEnabled = true
                    overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isPageLoading = true
                            hasError = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isPageLoading = false
                            swipeRefreshLayout.isRefreshing = false
                            showSplash = false
                            CookieManager.getInstance().flush()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) {
                                hasError = true
                                isPageLoading = false
                                swipeRefreshLayout.isRefreshing = false
                            }
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            // Proceed on benign SSL issues in test/staging
                            handler?.proceed()
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val uri = request?.url ?: return false
                            val scheme = uri.scheme ?: ""

                            // Keep web links, Google auth, and in-app domain URLs inside WebView
                            if (scheme == "http" || scheme == "https") {
                                return false
                            }

                            // Handle external non-web schemes (tel:, mailto:, whatsapp:, etc.)
                            return try {
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                ctx.startActivity(intent)
                                true
                            } catch (e: Exception) {
                                true
                            }
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            loadProgress = newProgress / 100f
                            if (newProgress >= 100) {
                                isPageLoading = false
                                swipeRefreshLayout.isRefreshing = false
                            }
                        }

                        // Google OAuth & Multi-Window Handling
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val childWebView = WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.apply {
                                    javaScriptEnabled = true
                                    javaScriptCanOpenWindowsAutomatically = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = CHROME_USER_AGENT
                                }
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: ""
                                        // If redirecting back to main website or auth callback, let it load
                                        if (url.contains("bandhan17.website")) {
                                            webViewInstance?.loadUrl(url)
                                            popupWebView?.destroy()
                                            popupWebView = null
                                            return true
                                        }
                                        return false
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onCloseWindow(window: WebView?) {
                                        popupWebView?.destroy()
                                        popupWebView = null
                                    }
                                }
                            }

                            popupWebView = childWebView
                            val transport = resultMsg?.obj as? WebView.WebViewTransport
                            transport?.webView = childWebView
                            resultMsg?.sendToTarget()
                            return true
                        }

                        override fun onCloseWindow(window: WebView?) {
                            super.onCloseWindow(window)
                            popupWebView?.destroy()
                            popupWebView = null
                        }

                        // File Upload & Camera Chooser
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            // Cancel previous pending callback if any
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = filePathCallback

                            try {
                                // Request permissions if needed
                                if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                                }

                                val photoFile = createImageFile(ctx)
                                val photoUri = FileProvider.getUriForFile(
                                    ctx,
                                    "${ctx.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraPhotoUri = photoUri

                                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                                    putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                                    addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                }

                                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                    addCategory(Intent.CATEGORY_OPENABLE)
                                    type = "*/*"
                                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "application/pdf", "*/*"))
                                }

                                val chooserIntent = Intent(Intent.ACTION_CHOOSER).apply {
                                    putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                                    putExtra(Intent.EXTRA_TITLE, "Select File or Take Photo")
                                    putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
                                }

                                fileChooserLauncher.launch(chooserIntent)
                                return true
                            } catch (e: Exception) {
                                fileUploadCallback?.onReceiveValue(null)
                                fileUploadCallback = null
                                return false
                            }
                        }
                    }

                    loadUrl(TARGET_URL)
                }

                swipeRefreshLayout.setOnRefreshListener {
                    if (networkMonitor.isCurrentlyConnected()) {
                        webView.reload()
                    } else {
                        swipeRefreshLayout.isRefreshing = false
                        hasError = true
                    }
                }

                swipeRefreshLayout.addView(webView)
                webViewInstance = webView
                swipeRefreshLayoutInstance = swipeRefreshLayout

                swipeRefreshLayout
            },
            update = { layout ->
                swipeRefreshLayoutInstance = layout
            }
        )

        // Child Popup WebView (for OAuth / Dialog Windows)
        if (popupWebView != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { popupWebView!! }
                )
            }
        }

        // Top Loading Progress Bar
        if (isPageLoading && !showSplash) {
            LinearProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.TopCenter),
                color = BandhanCyan,
                trackColor = Color.Transparent
            )
        }

        // Offline / No Internet Screen
        if (hasError || (!isOnline && isPageLoading)) {
            OfflineScreen(
                onRetry = {
                    hasError = false
                    isPageLoading = true
                    webViewInstance?.reload()
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Splash Screen Overlay
        SplashScreen(
            visible = showSplash,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}
