package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.json.JSONObject
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.mjdev.plugins.projectplugin.modules.HotModule

@Composable
fun Text(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val padding = PaddingValues(node.optInt("padding", 0).dp)
    Text(
        modifier = Modifier.padding(padding),
        text = node.optString("text"),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
}