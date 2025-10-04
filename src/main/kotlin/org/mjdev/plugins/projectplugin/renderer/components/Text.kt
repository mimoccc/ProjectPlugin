package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.json.JSONObject
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text

@Composable
fun Text(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val padding: PaddingValues = PaddingValues(node.optInt("padding", 0).dp)
    Text(
        modifier = Modifier.padding(padding),
        text = node.optString("text")
    )
}