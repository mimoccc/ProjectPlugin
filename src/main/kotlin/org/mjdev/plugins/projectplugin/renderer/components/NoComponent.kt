package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.json.JSONObject

@Composable
fun NoComponent(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    Text(
        text = "Unsupported node: ${node.optString("type")}"
    )
}