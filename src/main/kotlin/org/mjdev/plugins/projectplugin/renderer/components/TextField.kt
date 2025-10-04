package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import androidx.compose.material.TextField as TextFieldCompose
import androidx.compose.material.Text as TextCompose

@Composable
fun TextField(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val id = node.optString("id", "")
    var v by remember { mutableStateOf(state[id]?.toString() ?: "") }
    TextFieldCompose(
        modifier = Modifier.fillMaxWidth(),
        value = v,
        onValueChange = { v = it; state[id] = it },
        label = {
            TextCompose(
                text = node.optString("label", id).toString()
            )
        }
    )
}