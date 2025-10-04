package org.mjdev.plugins.projectplugin.renderer

import androidx.compose.runtime.Composable
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import org.mjdev.plugins.projectplugin.renderer.components.Columh
import org.mjdev.plugins.projectplugin.renderer.components.Text
import org.mjdev.plugins.projectplugin.renderer.components.TextField
import org.mjdev.plugins.projectplugin.renderer.components.Button
import org.mjdev.plugins.projectplugin.renderer.components.Label
import org.mjdev.plugins.projectplugin.renderer.components.MarkDownViewView
import org.mjdev.plugins.projectplugin.renderer.components.NoComponent
import org.mjdev.plugins.projectplugin.renderer.components.PdfView
import org.mjdev.plugins.projectplugin.renderer.components.WebView
import org.mjdev.plugins.projectplugin.renderer.components.AdbScreenMirror

@Composable
fun ComposeNode(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit
) = when (node.optString("type")) {
    "Column" -> Columh(module, node, state, onAction)
    "Text" -> Text(module, node, state, onAction)
    "TextField" -> TextField(module, node, state, onAction)
    "Button" -> Button(module, node, state, onAction)
    "Label" -> Label(module, node, state, onAction)
    "PdfView" -> PdfView(module, node, state, onAction)
    "MarkDownViewView" -> MarkDownViewView(module, node, state, onAction)
    "WebView" -> WebView(module, node, state, onAction)
    "AdbScreenMirror" -> AdbScreenMirror(module, node, state, onAction)
    else -> NoComponent(module, node, state, onAction)
}
