package org.mjdev.plugins.projectplugin.ui.components.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import netscape.javascript.JSObject

@Composable
fun JavaFxWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageLoaded: () -> Unit = {},
    onJavaScriptResult: (Any?) -> Unit = {}
) {
    val jfxPanel = remember { JFXPanel() }
    var webView: WebView? by remember { mutableStateOf(null) }
    Box(modifier = modifier) {
        SwingPanel(
            factory = { jfxPanel },
            modifier = Modifier.fillMaxSize()
        )
    }
    DisposableEffect(url) {
        Platform.runLater {
            webView = WebView().apply {
                engine.load(url)
                engine.loadWorker.stateProperty().addListener { _, _, newState ->
                    if (newState == Worker.State.SUCCEEDED) {
                        onPageLoaded()
                    }
                }
                jfxPanel.scene = Scene(this)
            }
        }
        onDispose {
            Platform.runLater {
                webView?.engine?.loadContent("")
            }
        }
    }
}

class KotlinBridge {
    fun log(message: String) {
        println("JS → Kotlin: $message")
    }

    fun getData(): String = "Data from Kotlin"
}

fun WebEngine.setupJsBridge() {
    loadWorker.stateProperty().addListener { _, _, state ->
        if (state == Worker.State.SUCCEEDED) {
            val window = executeScript("window") as JSObject
            window.setMember("kotlin", KotlinBridge())
        }
    }
}

fun WebEngine.executeJs(script: String, callback: (Any?) -> Unit = {}) {
    Platform.runLater {
        val result = executeScript(script)
        callback(result)
    }
}