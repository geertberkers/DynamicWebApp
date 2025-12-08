package webview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.UIKit.UIView
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun KmpWebView(url: String, modifier: Modifier) {
    // Cache WKWebView per URL to preserve state (cookies, session, scroll position)
    val webView = remember(url) {
        WKWebView(frame = platform.CoreGraphics.CGRectZero, configuration = WKWebViewConfiguration().apply {
            // Enable data storage for cookies and sessions
            preferences.javaScriptEnabled = true
        })
    }
    
    UIKitView(
        factory = { webView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            if (view is WKWebView && url.isNotBlank()) {
                val currentUrl = view.URL?.absoluteString
                // Only load if URL is different from current URL
                if (currentUrl == null || currentUrl != url) {
                    val nsUrl = NSURL(string = url)
                    val request = NSURLRequest(uRL = nsUrl)
                    view.loadRequest(request)
                }
            }
        }
    )
}

