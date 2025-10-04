package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import org.json.JSONObject
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider as DividerCompose
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.Text as TextCompose
import androidx.compose.foundation.layout.Column as ColumnCompose

@Preview
@Composable
fun Label(
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val padding = PaddingValues(node.optInt("padding", 0).dp)
    ColumnCompose(
        modifier = Modifier.padding(padding),
    ) {
        TextCompose(
            text = node.optString("text"),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
        DividerCompose(
            modifier = Modifier.padding(bottom = 2.dp),
            thickness = 2.dp
        )
    }
}