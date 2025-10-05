package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import androidx.compose.material.Text as ComposeText

@Composable
fun WebView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    if (!JBCefApp.isSupported()) {
        ComposeText("JCEF not supported in this IDE")
        return
    }
    val htmlFile = node.optString("file")
    val url = node.optString("url", "")
    val htmlFileData: String? = runCatching {
        module.getFileData(htmlFile)
    }.getOrNull()
    if (htmlFileData != null || url.isNotEmpty()) {
        val lifecycleOwner = LocalLifecycleOwner.current
        var isVisible by remember { mutableStateOf(true) }
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                isVisible = when (event) {
                    Lifecycle.Event.ON_START,
                    Lifecycle.Event.ON_RESUME -> true

                    Lifecycle.Event.ON_STOP,
                    Lifecycle.Event.ON_PAUSE -> false

                    else -> isVisible
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        if (isVisible) {
            val browser = remember {
                JBCefBrowser()
            }
            DisposableEffect(htmlFileData, url) {
                when {
                    url.isNotEmpty() -> browser.loadURL(url)
                    htmlFileData != null -> {
                        browser.loadHTML(htmlFileData)
                    }
                }
                onDispose {
                    browser.dispose()
                }
            }
            SwingPanel(
                factory = { browser.component },
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        ComposeText(
            text = "WebView source not found: ${htmlFile ?: url}"
        )
    }
}
