package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebView as JFXWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import androidx.compose.material.Text as ComposeText
import kotlin.concurrent.thread

@Composable
fun WebView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val htmlFile = node.optString("file")
    val url = node.optString("url", "")
    val htmlFileData: String? = runCatching {
        module.getFileData(htmlFile)
    }.getOrNull()

    if (htmlFileData != null || url.isNotEmpty()) {
        var jfxPanel by remember { mutableStateOf<JFXPanel?>(null) }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                val panel = JFXPanel() // Init JavaFX toolkit

                thread {
                    Thread.sleep(100) // Počkej na JavaFX startup
                    Platform.runLater {
                        val webView = JFXWebView()
                        webView.engine.isJavaScriptEnabled = true

                        when {
                            url.isNotEmpty() -> webView.engine.load(url)
                            htmlFileData != null -> webView.engine.loadContent(htmlFileData)
                        }

                        panel.scene = Scene(webView)
                    }
                }

                jfxPanel = panel
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                jfxPanel?.let {
                    Platform.runLater {
                        it.scene = null
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            jfxPanel?.let { panel ->
                SwingPanel(
                    factory = { panel },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    } else {
        ComposeText(text = "WebView source not found: ${htmlFile ?: url}")
    }
}
