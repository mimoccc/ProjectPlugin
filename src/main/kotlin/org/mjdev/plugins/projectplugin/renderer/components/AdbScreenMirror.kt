package org.mjdev.plugins.projectplugin.renderer.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toComposeImageBitmap
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
import androidx.compose.ui.input.pointer.pointerInput
import dadb.Dadb
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import org.jetbrains.skia.Image as SkiaImage
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.extensions.CoroutineExt.launch
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
    val deviceState = remember {
        DeviceState(deviceSerial, refreshInterval, state, onAction, nodeId)
    }
    DisposableEffect(deviceSerial) {
        deviceState.start()
        onDispose {
            deviceState.stop()
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        when {
            deviceState.isConnecting -> CircularProgressIndicator()
            deviceState.error != null -> Text("Error: ${deviceState.error}")
            deviceState.imageBitmap != null -> {
                val currentImage = deviceState.imageBitmap!!
                Image(
                    bitmap = currentImage,
                    contentDescription = "Device screen",
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(deviceState.dadbInstance) {
                            detectTapGestures { offset ->
                                deviceState.tapOnDevice(
                                    offset.x.toInt(),
                                    offset.y.toInt(),
                                    size.width,
                                    size.height
                                )
                            }
                        }
                )
            }

            else -> Text("Waiting for device...")
        }
    }
}

class DeviceState(
    private val deviceSerial: String?,
    private val refreshInterval: Long,
    private val state: MutableMap<String, Any?>,
    private val onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
    private val nodeId: String
) {
    var imageBitmap by mutableStateOf<ImageBitmap?>(null)
    var error by mutableStateOf<String?>(null)
    var isConnecting by mutableStateOf(true)
    var dadbInstance by mutableStateOf<Dadb?>(null)
    var screenWidth by mutableStateOf(0)
    var screenHeight by mutableStateOf(0)
    private var job: Job? = null

    fun start() {
        job = launch {
            try {
                val dadb = deviceSerial?.let { serial ->
                    Dadb.list().find { it.toString().contains(serial) }
                } ?: Dadb.discover()
                if (dadb == null) {
                    error = "No device found"
                    isConnecting = false
                    state["error"] = "No device found"
                    state["connected"] = false
                    return@launch
                }
                dadbInstance = dadb
                isConnecting = false
                state["connected"] = true
                state["deviceSerial"] = dadb.toString()
                while (job?.isActive == true) {
                    try {
                        val stream = dadb.open("exec:screencap -p")
                        val bytes = stream.source.readByteArray()
                        stream.close()
                        if (bytes.isNotEmpty()) {
                            val skiaImage = SkiaImage.makeFromEncoded(bytes)
                            imageBitmap = skiaImage.toComposeImageBitmap()
                            screenWidth = skiaImage.width
                            screenHeight = skiaImage.height
                            state["screenWidth"] = screenWidth
                            state["screenHeight"] = screenHeight
                            state["lastUpdate"] = System.currentTimeMillis()
                        }
                    } catch (e: Exception) {
                        error = e.message
                        state["error"] = e.message
                    }
                    delay(refreshInterval)
                }
            } catch (e: Exception) {
                error = e.message
                isConnecting = false
                state["error"] = e.message
                state["connected"] = false
            }
        }
    }

    fun stop() {
        job?.cancel()
        dadbInstance?.close()
    }

    fun tapOnDevice(
        offsetX: Int,
        offsetY: Int,
        sizeWidth: Int,
        sizeHeight: Int
    ) {
        dadbInstance?.let { dadb ->
            launch {
                try {
                    val scaleX = screenWidth.toFloat() / sizeWidth
                    val scaleY = screenHeight.toFloat() / sizeHeight
                    val deviceX = (offsetX * scaleX).toInt()
                    val deviceY = (offsetY * scaleY).toInt()
                    dadb.shell("input tap $deviceX $deviceY")
                    state["lastTapX"] = deviceX
                    state["lastTapY"] = deviceY
                    onAction(
                        nodeId, "tap", mapOf(
                            "x" to deviceX,
                            "y" to deviceY
                        )
                    )
                } catch (e: Exception) {
                    state["error"] = e.message
                }
            }
        }
    }
}
