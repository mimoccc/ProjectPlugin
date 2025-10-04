package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule

@Composable
fun NoComponent(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    Text(
        text = "Unsupported node: ${node.optString("type")}"
    )
}