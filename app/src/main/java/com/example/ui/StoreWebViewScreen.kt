package com.example.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.GeolocationPermissions
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.theme.BrandBluePrimary
import com.example.util.NetworkMonitor
import kotlinx.coroutines.launch

private const val STORE_BASE_URL = "https://b-storeksa.com"
private const val STORE_HOST = "b-storeksa.com"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun StoreWebViewScreen(
  networkMonitor: NetworkMonitor,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val isOnline by networkMonitor.isOnline.collectAsState(initial = networkMonitor.isCurrentlyConnected())

  var webViewInstance by remember { mutableStateOf<WebView?>(null) }
  var currentUrl by remember { mutableStateOf(STORE_BASE_URL) }
  var pageTitle by remember { mutableStateOf("B Store KSA") }
  var isLoading by remember { mutableStateOf(true) }
  var progress by remember { mutableIntStateOf(0) }
  var canGoBack by remember { mutableStateOf(false) }
  var canGoForward by remember { mutableStateOf(false) }
  var hasLoadError by remember { mutableStateOf(false) }
  var isInitialLoading by remember { mutableStateOf(true) }

  // File upload chooser callback holder
  var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

  // Launcher for file chooser (receipts, photos, etc.)
  val fileChooserLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    val callback = fileUploadCallback
    if (callback != null) {
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val clipData = result.data?.clipData
        val dataUri = result.data?.data
        val uris = when {
          clipData != null -> {
            val count = clipData.itemCount
            val list = mutableListOf<Uri>()
            for (i in 0 until count) {
              list.add(clipData.getItemAt(i).uri)
            }
            list.toTypedArray()
          }
          dataUri != null -> arrayOf(dataUri)
          else -> null
        }
        callback.onReceiveValue(uris)
      } else {
        callback.onReceiveValue(null)
      }
      fileUploadCallback = null
    }
  }

  // Handle double back to exit when at root of webview
  var lastBackPressTime by remember { mutableLongStateOf(0L) }

  BackHandler {
    val wv = webViewInstance
    if (wv != null && wv.canGoBack()) {
      wv.goBack()
    } else {
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastBackPressTime < 2000) {
        (context as? Activity)?.finish()
      } else {
        lastBackPressTime = currentTime
        scope.launch {
          snackbarHostState.showSnackbar(
            message = "اضغط مرة أخرى للخروج من التطبيق",
            duration = androidx.compose.material3.SnackbarDuration.Short
          )
        }
      }
    }
  }

  // Re-check internet state: if came back online and was in error, reload
  LaunchedEffect(isOnline) {
    if (isOnline && hasLoadError) {
      hasLoadError = false
      webViewInstance?.reload()
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      StoreTopBar(
        currentUrl = currentUrl,
        pageTitle = pageTitle,
        isLoading = isLoading,
        canGoBack = canGoBack,
        canGoForward = canGoForward,
        onBackClick = { webViewInstance?.goBack() },
        onForwardClick = { webViewInstance?.goForward() },
        onRefreshClick = {
          if (isLoading) {
            webViewInstance?.stopLoading()
          } else {
            hasLoadError = false
            webViewInstance?.reload()
          }
        },
        onHomeClick = {
          hasLoadError = false
          webViewInstance?.loadUrl(STORE_BASE_URL)
        },
        onClearCacheClick = {
          webViewInstance?.clearCache(true)
          CookieManager.getInstance().removeAllCookies(null)
          CookieManager.getInstance().flush()
          Toast.makeText(context, "تم مسح الذاكرة المؤقتة وملفات تعريف الارتباط", Toast.LENGTH_SHORT).show()
          webViewInstance?.reload()
        }
      )
    },
    bottomBar = {
      StoreBottomBar(
        currentUrl = currentUrl,
        onNavigateUrl = { targetUrl ->
          hasLoadError = false
          webViewInstance?.loadUrl(targetUrl)
        },
        onContactSupport = {
          // Open WhatsApp support for B Store
          try {
            val waIntent = Intent(Intent.ACTION_VIEW).apply {
              // Standard whatsapp link or search
              data = Uri.parse("https://wa.me/?text=${Uri.encode("السلام عليكم، أود الاستفسار بخصوص خدمات متجر B Store")}")
              setPackage("com.whatsapp")
            }
            context.startActivity(waIntent)
          } catch (_: Exception) {
            try {
              val browserWa = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://wa.me/?text=${Uri.encode("السلام عليكم، أود الاستفسار بخصوص خدمات متجر B Store")}")
              )
              context.startActivity(browserWa)
            } catch (e: Exception) {
              Toast.makeText(context, "تعذر فتح الواتساب", Toast.LENGTH_SHORT).show()
            }
          }
        }
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // WebView Core
      AndroidView(
        modifier = Modifier
          .fillMaxSize()
          .testTag("store_webview"),
        factory = { ctx ->
          WebView(ctx).apply {
            settings.apply {
              javaScriptEnabled = true
              domStorageEnabled = true
              databaseEnabled = true
              allowFileAccess = true
              allowContentAccess = true
              useWideViewPort = true
              loadWithOverviewMode = true
              setSupportZoom(false)
              displayZoomControls = false
              cacheMode = WebSettings.LOAD_DEFAULT
              mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
              userAgentString = "${settings.userAgentString} BStoreKSA-Android/1.0"
            }

            val cm = CookieManager.getInstance()
            cm.setAcceptCookie(true)
            cm.setAcceptThirdPartyCookies(this, true)

            setDownloadListener { downloadUrl, userAgent, contentDisposition, mimetype, _ ->
              try {
                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                  setMimeType(mimetype)
                  addRequestHeader("User-Agent", userAgent)
                  setDescription("تنزيل ملف من متجر بي ستور")
                  setTitle("B Store Download")
                  setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                  setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    Uri.parse(downloadUrl).lastPathSegment ?: "b_store_download"
                  )
                }
                val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                dm?.enqueue(request)
                Toast.makeText(ctx, "جاري تنزيل الملف...", Toast.LENGTH_SHORT).show()
              } catch (e: Exception) {
                try {
                  val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                  ctx.startActivity(intent)
                } catch (ex: Exception) {
                  Toast.makeText(ctx, "تعذر بدء التنزيل", Toast.LENGTH_SHORT).show()
                }
              }
            }

            webChromeClient = object : WebChromeClient() {
              override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progress = newProgress
                isLoading = newProgress < 100
                if (newProgress >= 90) {
                  isInitialLoading = false
                }
              }

              override fun onReceivedTitle(view: WebView?, title: String?) {
                if (!title.isNullOrBlank() && !title.startsWith("http")) {
                  pageTitle = title
                }
              }

              override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
              ): Boolean {
                fileUploadCallback?.onReceiveValue(null)
                fileUploadCallback = filePathCallback

                val intent = try {
                  fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                  }
                } catch (e: Exception) {
                  Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                  }
                }

                return try {
                  fileChooserLauncher.launch(intent)
                  true
                } catch (e: Exception) {
                  fileUploadCallback?.onReceiveValue(null)
                  fileUploadCallback = null
                  false
                }
              }

              override fun onGeolocationPermissionsShowPrompt(
                origin: String?,
                callback: GeolocationPermissions.Callback?
              ) {
                callback?.invoke(origin, true, false)
              }
            }

            webViewClient = object : WebViewClient() {
              override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                isLoading = true
                if (url != null) {
                  currentUrl = url
                }
                canGoBack = canGoBack()
                canGoForward = canGoForward()
              }

              override fun onPageFinished(view: WebView?, url: String?) {
                isLoading = false
                isInitialLoading = false
                if (url != null) {
                  currentUrl = url
                }
                canGoBack = canGoBack()
                canGoForward = canGoForward()
              }

              override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
              ) {
                if (request?.isForMainFrame == true) {
                  hasLoadError = true
                  isLoading = false
                  isInitialLoading = false
                }
              }

              override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
              ): Boolean {
                val url = request?.url?.toString() ?: return false
                val uri = request.url

                // Handle common URL schemes
                val scheme = uri.scheme?.lowercase() ?: ""
                when {
                  scheme == "tel" -> {
                    try {
                      ctx.startActivity(Intent(Intent.ACTION_DIAL, uri))
                    } catch (_: Exception) {}
                    return true
                  }
                  scheme == "mailto" -> {
                    try {
                      ctx.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                    } catch (_: Exception) {}
                    return true
                  }
                  scheme == "whatsapp" || url.contains("wa.me") || url.contains("api.whatsapp.com") -> {
                    try {
                      val waIntent = Intent(Intent.ACTION_VIEW, uri)
                      ctx.startActivity(waIntent)
                    } catch (_: Exception) {
                      view?.loadUrl(url)
                    }
                    return true
                  }
                  scheme == "intent" -> {
                    try {
                      val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                      if (intent != null) {
                        ctx.startActivity(intent)
                        return true
                      }
                    } catch (_: Exception) {
                      val fallback = try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        intent.getStringExtra("browser_fallback_url")
                      } catch (e: Exception) {
                        null
                      }
                      if (!fallback.isNullOrBlank()) {
                        view?.loadUrl(fallback)
                        return true
                      }
                    }
                    return true
                  }
                  scheme != "http" && scheme != "https" -> {
                    // Try external intent (e.g., payment app schemes like stcpay://, etc.)
                    try {
                      val externalIntent = Intent(Intent.ACTION_VIEW, uri)
                      ctx.startActivity(externalIntent)
                      return true
                    } catch (_: Exception) {
                      return true
                    }
                  }
                  else -> {
                    // Normal web URL, let webview load it
                    return false
                  }
                }
              }
            }

            loadUrl(STORE_BASE_URL)
            webViewInstance = this
          }
        },
        update = { wv ->
          webViewInstance = wv
          canGoBack = wv.canGoBack()
          canGoForward = wv.canGoForward()
        }
      )

      // Thin Top Progress Bar
      StoreProgressBar(
        isLoading = isLoading,
        progress = progress,
        modifier = Modifier.align(Alignment.TopCenter)
      )

      // Initial Brand Splash Overlay
      AnimatedVisibility(
        visible = isInitialLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Surface(
              shape = CircleShape,
              color = BrandBluePrimary,
              modifier = Modifier.size(80.dp),
              shadowElevation = 8.dp
            ) {
              Image(
                painter = painterResource(id = R.drawable.b_store_official_logo),
                contentDescription = "B Store Logo",
                modifier = Modifier
                  .fillMaxSize()
                  .padding(12.dp)
              )
            }

            Text(
              text = "B Store KSA",
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )

            Text(
              text = "جاري تحميل المتجر...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CircularProgressIndicator(
              color = BrandBluePrimary,
              modifier = Modifier.size(32.dp),
              strokeWidth = 3.dp
            )
          }
        }
      }

      // Offline or Error Screen Overlay
      if (hasLoadError || (!isOnline && isInitialLoading)) {
        ErrorScreen(
          isOffline = !isOnline,
          onRetry = {
            hasLoadError = false
            webViewInstance?.reload()
          },
          onOpenBrowser = {
            try {
              val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(currentUrl))
              context.startActivity(browserIntent)
            } catch (e: Exception) {
              Toast.makeText(context, "تعذر فتح المتصفح", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        )
      }
    }
  }

  // Cleanup on leave
  DisposableEffect(Unit) {
    onDispose {
      webViewInstance?.destroy()
      webViewInstance = null
    }
  }
}
