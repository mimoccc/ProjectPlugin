package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.zt64.compose.pdf.component.PdfPage
import dev.zt64.compose.pdf.rememberLocalPdfState
import kotlinx.coroutines.launch
import org.codehaus.plexus.util.StringInputStream
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.extensions.CoroutineExt.rememberCoroutineScope
import org.mjdev.plugins.projectplugin.modules.HotModule
import androidx.compose.material.Text as ComposeText
import androidx.compose.foundation.layout.Column as ComposeColumn

@Composable
fun PdfView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val pdfFile = node.optString("file")
    val pdfFileData: String? = runCatching {
        module.getFileData(pdfFile)
    }.getOrNull()
    if (pdfFile != null) {
        val pdfState = rememberLocalPdfState(StringInputStream(pdfFile))
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
            repeat(pdfState.pageCount) { index ->
                PdfPage(
                    state = pdfState,
                    index = index,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
        }
    } else {
        ComposeText(
            text = "PDF file not found: $pdfFile"
        )
    }
}