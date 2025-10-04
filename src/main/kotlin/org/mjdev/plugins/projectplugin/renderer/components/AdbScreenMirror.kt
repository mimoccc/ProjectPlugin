package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import dadb.Dadb
import kotlinx.coroutines.*
import org.jetbrains.skia.Image as SkiaImage
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule

@Composable
fun AdbScreenMirror(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val deviceSerial = node.optString("deviceSerial").takeIf { it.isNotEmpty() }
    val refreshInterval = node.optLong("refreshIntervalMs", 100)
    val nodeId = node.optString("id")
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isConnecting by remember { mutableStateOf(true) }
    var dadbInstance by remember { mutableStateOf<Dadb?>(null) }
    var screenWidth by remember { mutableStateOf(0) }
    var screenHeight by remember { mutableStateOf(0) }
    DisposableEffect(deviceSerial) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            try {
                val dadb = deviceSerial?.let { serial ->
                    Dadb.list().find { it.toString().contains(serial) }
                } ?: Dadb.discover()
                if (dadb == null) {
                    withContext(Dispatchers.Main) {
                        error = "No device found"
                        isConnecting = false
                        state["error"] = "No device found"
                        state["connected"] = false
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    dadbInstance = dadb
                    isConnecting = false
                    state["connected"] = true
                    state["deviceSerial"] = dadb.toString()
                }
                while (isActive) {
                    try {
                        val stream = dadb.open("exec:screencap -p")
                        val bytes = stream.source.readByteArray()
                        stream.close()
                        if (bytes.isNotEmpty()) {
                            val skiaImage = SkiaImage.makeFromEncoded(bytes)
                            withContext(Dispatchers.Main) {
                                imageBitmap = skiaImage.toComposeImageBitmap()
                                screenWidth = skiaImage.width
                                screenHeight = skiaImage.height
                                state["screenWidth"] = screenWidth
                                state["screenHeight"] = screenHeight
                                state["lastUpdate"] = System.currentTimeMillis()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            error = e.message
                            state["error"] = e.message
                        }
                    }
                    delay(refreshInterval)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    error = e.message
                    isConnecting = false
                    state["error"] = e.message
                    state["connected"] = false
                }
            }
        }
        onDispose {
            job.cancel()
            dadbInstance?.close()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            isConnecting -> CircularProgressIndicator()
            error != null -> Text("Error: $error")
            imageBitmap != null -> {
                val currentImage = imageBitmap!!
                Image(
                    bitmap = currentImage,
                    contentDescription = "Device screen",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dadbInstance) {
                            detectTapGestures { offset ->
                                dadbInstance?.let { dadb ->
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val scaleX = screenWidth.toFloat() / size.width
                                            val scaleY = screenHeight.toFloat() / size.height
                                            val deviceX = (offset.x * scaleX).toInt()
                                            val deviceY = (offset.y * scaleY).toInt()
                                            dadb.shell("input tap $deviceX $deviceY")
                                            withContext(Dispatchers.Main) {
                                                state["lastTapX"] = deviceX
                                                state["lastTapY"] = deviceY
                                                onAction(nodeId, "tap", mapOf(
                                                    "x" to deviceX,
                                                    "y" to deviceY
                                                ))
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                state["error"] = e.message
                                            }
                                        }
                                    }
                                }
                            }
                        }
                )
            }
            else -> Text("Waiting for device...")
        }
    }
}
