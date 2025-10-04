package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.renderer.ComposeNode

@Composable
fun Columh(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val padding: PaddingValues = PaddingValues(node.optInt("padding", 0).dp)
    val children: JSONArray = node.optJSONArray("children")
    Column(
        modifier = Modifier.padding(padding)
    ) {
        if (children != null) {
            for (i in 0 until children.length()) {
                val child = children.getJSONObject(i)
                ComposeNode(child, state, onAction)
            }
        }
    }
}