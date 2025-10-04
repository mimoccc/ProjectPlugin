package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.runtime.Composable
import org.json.JSONObject
import androidx.compose.material.Button as ComposeButton
import androidx.compose.material.Text as ComposeText

@Composable
fun Button (
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val id = node.optString("id", "button")
    val onClick = node.optString("onClick", "")
    ComposeButton(
        onClick = {
            onAction(id, onClick, state)
        }
    ) {
        ComposeText(
            text = node.optString("text", id)
        )
    }
}