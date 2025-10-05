package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m2.markdownColor
import com.mikepenz.markdown.m2.markdownTypography
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.extensions.CoroutineExt.rememberCoroutineScope
import org.mjdev.plugins.projectplugin.modules.HotModule
import androidx.compose.material.Text as ComposeText
import androidx.compose.foundation.layout.Column as ComposeColumn

@Composable
fun MarkDownViewView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val mdFile = node.optString("file")
    val mdFileData: String? = runCatching {
        module.getFileData(mdFile)
    }.getOrNull()
    if (mdFileData != null) {
        val markdownState = mutableStateOf(mdFileData)
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        ComposeColumn(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        coroutineScope.launch {
                            scrollState.scrollBy(-dragAmount.y)
                        }
                    }
                }
        ) {
            Markdown(
                content = markdownState.value,
                colors = markdownColor(),
                typography = markdownTypography()
            )
        }
    } else {
        ComposeText(
            text = "MarkDown file not found: $mdFile"
        )
    }
}
