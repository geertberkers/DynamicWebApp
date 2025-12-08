package ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.SiteConfig
import webview.KmpWebView

@Composable
fun SiteScreen(site: SiteConfig, modifier: Modifier = Modifier) {
    // Use key to preserve WebView state when switching tabs
    // The key ensures the WebView instance is preserved based on site ID
    key(site.id) {
        if (site.isConfigured) {
            KmpWebView(
                url = site.url,
                modifier = modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Configure this slot in Settings.")
            }
        }
    }
}

