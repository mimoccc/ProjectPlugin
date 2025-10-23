package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.runtime.*
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule

@Composable
fun VncView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
}

class VncViewState(
    val host : String,
    val port:Int = 5900
) {

}

