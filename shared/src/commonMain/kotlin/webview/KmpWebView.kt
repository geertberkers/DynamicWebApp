package webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun KmpWebView(url: String, modifier: Modifier = Modifier)

