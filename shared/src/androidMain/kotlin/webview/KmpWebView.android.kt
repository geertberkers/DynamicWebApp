package webview

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun KmpWebView(url: String, modifier: Modifier) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
        }
    }
    
    DisposableEffect(url) {
        if (webView.url != url && url.isNotBlank()) {
            webView.loadUrl(url)
        }
        onDispose { }
    }
    
    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

