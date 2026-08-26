package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.VsCodeAccent
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeText
import com.example.ui.theme.VsCodeTextMuted

enum class ViewportMode {
    RESPONSIVE, MOBILE, TABLET
}

@Composable
fun WebPreviewPane(
    htmlContent: String,
    cssContent: String = "",
    jsContent: String = "",
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var viewportMode by remember { mutableStateOf(ViewportMode.RESPONSIVE) }
    var reloadTrigger by remember { mutableStateOf(0) }

    // Combined HTML document
    val fullHtml = remember(htmlContent, cssContent, jsContent, reloadTrigger) {
        val combined = if (htmlContent.contains("<html", ignoreCase = true)) {
            var temp = htmlContent
            if (cssContent.isNotBlank() && !temp.contains("<style>")) {
                temp = temp.replace("</head>", "<style>$cssContent</style></head>")
            }
            if (jsContent.isNotBlank() && !temp.contains("<script>")) {
                temp = temp.replace("</body>", "<script>$jsContent</script></body>")
            }
            temp
        } else {
            """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        margin: 16px;
                        background: #121212;
                        color: #e0e0e0;
                    }
                    $cssContent
                </style>
            </head>
            <body>
                $htmlContent
                <script>
                    $jsContent
                </script>
            </body>
            </html>
            """.trimIndent()
        }
        combined
    }

    Surface(
        color = Color(0xFF1E1E1E),
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, VsCodeBorder)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Browser Chrome Toolbar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFF252526))
                    .padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = VsCodeAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    
                    // URL Pill
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier
                            .height(24.dp)
                            .fillMaxWidth(0.8f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF181818))
                            .border(0.5.dp, VsCodeBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = "http://localhost:3000/index.html",
                            color = VsCodeTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Viewport Switcher
                    IconButton(
                        onClick = { viewportMode = ViewportMode.MOBILE },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = "Mobile View",
                            tint = if (viewportMode == ViewportMode.MOBILE) VsCodeAccent else VsCodeTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewportMode = ViewportMode.TABLET },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Tablet,
                            contentDescription = "Tablet View",
                            tint = if (viewportMode == ViewportMode.TABLET) VsCodeAccent else VsCodeTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewportMode = ViewportMode.RESPONSIVE },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Laptop,
                            contentDescription = "Desktop / Full View",
                            tint = if (viewportMode == ViewportMode.RESPONSIVE) VsCodeAccent else VsCodeTextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            reloadTrigger++
                            webViewInstance?.reload()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload Web View", tint = VsCodeText, modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Web Preview", tint = VsCodeTextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Web Canvas
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF141414))
                    .padding(if (viewportMode != ViewportMode.RESPONSIVE) 12.dp else 0.dp)
            ) {
                val containerWidthModifier = when (viewportMode) {
                    ViewportMode.MOBILE -> Modifier.width(360.dp)
                    ViewportMode.TABLET -> Modifier.width(600.dp)
                    ViewportMode.RESPONSIVE -> Modifier.fillMaxWidth()
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(containerWidthModifier)
                        .clip(RoundedCornerShape(if (viewportMode != ViewportMode.RESPONSIVE) 8.dp else 0.dp))
                        .border(if (viewportMode != ViewportMode.RESPONSIVE) 1.dp else 0.dp, VsCodeBorder, RoundedCornerShape(if (viewportMode != ViewportMode.RESPONSIVE) 8.dp else 0.dp))
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                webChromeClient = WebChromeClient()
                                webViewClient = WebViewClient()
                                loadDataWithBaseURL("http://localhost:3000/", fullHtml, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("http://localhost:3000/", fullHtml, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize().testTag("web_preview_canvas")
                    )
                }
            }
        }
    }
}
