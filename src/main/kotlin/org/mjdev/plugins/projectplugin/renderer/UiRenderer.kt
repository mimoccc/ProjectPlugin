package org.mjdev.plugins.projectplugin.renderer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@Composable
fun RenderLayout(
    json: String,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit
) {
    val root = JSONObject(json)
    val state = remember { mutableStateMapOf<String, Any?>() }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        ComposeNode(root, state, onAction)
    }
}

@Composable
private fun ComposeNode(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit
) {
    when (node.optString("type")) {
        "Column" -> {
            Column(
                modifier = Modifier.padding(
                    PaddingValues(
                        node.optInt(
                            "padding",
                            0
                        ).dp
                    )
                )
            ) {
                val children = node.optJSONArray("children")
                if (children != null) {
                    for (i in 0 until children.length()) {
                        val child = children.getJSONObject(i)
                        ComposeNode(child, state, onAction)
                    }
                }
            }
        }

        "Text" -> {
            Text(
                text = node.optString("text")
            )
        }

        "TextField" -> {
            val id = node.optString("id", "")
            var v by remember { mutableStateOf(state[id]?.toString() ?: "") }
            TextField(
                value = v,
                onValueChange = { v = it; state[id] = it },
                label = {
                    Text(
                        text = node.optString("label", id)
                    )
                }
            )
        }

        "Button" -> {
            val id = node.optString("id", "button")
            val onClick = node.optString("onClick", "")
            Button(
                onClick = {
                    onAction(id, onClick, state)
                }
            ) {
                Text(
                    text = node.optString("text", id)
                )
            }
        }

        else -> {
            Text(
                text = "Unsupported node: ${node.optString("type")}"
            )
        }
    }
}
