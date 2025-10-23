package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.foundation.layout.Column as ComposeColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import org.mjdev.plugins.projectplugin.ui.ComposeNode

@Composable
fun Columh(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val padding: PaddingValues = PaddingValues(node.optInt("padding", 0).dp)
    val children: JSONArray = node.optJSONArray("children")
    ComposeColumn(
        modifier = Modifier.padding(padding)
    ) {
        for (i in 0 until children.length()) {
            val child = children.getJSONObject(i)
            ComposeNode(module, child, state, onAction)
        }
    }
}