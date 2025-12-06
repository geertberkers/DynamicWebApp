package webview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var currentUrl by remember { mutableStateOf<String?>(null) }
    
    UIKitView(
        factory = {
            WKWebView(frame = platform.CoreGraphics.CGRectZero, configuration = WKWebViewConfiguration())
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            if (view is WKWebView && url.isNotBlank() && currentUrl != url) {
                currentUrl = url
                val nsUrl = NSURL(string = url)
                val request = NSURLRequest(uRL = nsUrl)
                view.loadRequest(request)
            }
        }
    )
}

