package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.zt64.compose.pdf.component.PdfPage
import dev.zt64.compose.pdf.rememberLocalPdfState
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import java.io.File
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import androidx.compose.material.Text as ComposeText
import androidx.compose.foundation.layout.Column as ComposeColumn

@Composable
fun PdfView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val pdfFile : File? = runCatching {
        val modulePath = Paths.get(module.moduleDirPath)
        val fileName = node.optString("file")
        File(modulePath.resolve(fileName).absolutePathString())
    }.getOrNull()
    if (pdfFile?.exists() == true) {
        val pdfState = rememberLocalPdfState(pdfFile)
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
            text = "PDF soubor nenalezen: ${pdfFile?.absolutePath}"
        )
    }
}