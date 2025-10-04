package org.mjdev.plugins.projectplugin.renderer

import androidx.compose.runtime.Composable
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.renderer.components.Columh
import org.mjdev.plugins.projectplugin.renderer.components.Text
import org.mjdev.plugins.projectplugin.renderer.components.TextField
import org.mjdev.plugins.projectplugin.renderer.components.Button
import org.mjdev.plugins.projectplugin.renderer.components.NoComponent

@Composable
fun ComposeNode(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit
) = when (node.optString("type")) {
    "Column" -> Columh(node, state, onAction)
    "Text" -> Text(node, state, onAction)
    "TextField" -> TextField(node, state, onAction)
    "Button" -> Button(node, state, onAction)
    else -> NoComponent(node, state, onAction)
}
