package webview

import android.webkit.CookieManager
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
    
    // Cache WebView per URL to preserve state (cookies, session, scroll position)
    val webView = remember(url) {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true // Enable localStorage
            webViewClient = WebViewClient()
        }
    }
    
    // Enable cookies
    DisposableEffect(Unit) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        onDispose { }
    }
    
    // Only load URL if it's different from current URL
    DisposableEffect(url) {
        val currentUrl = webView.url
        if (url.isNotBlank() && (currentUrl == null || currentUrl != url)) {
            webView.loadUrl(url)
        }
        onDispose { }
    }
    
    AndroidView(
        factory = { webView },
        modifier = modifier.fillMaxSize()
    )
}

