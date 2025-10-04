package org.mjdev.plugins.projectplugin.renderer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule

@Composable
fun RenderLayout(
    module: HotModule,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit
) {
    val state = remember { mutableStateMapOf<String, Any?>() }
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        ComposeNode( module, module.layout, state, onAction)
    }
}