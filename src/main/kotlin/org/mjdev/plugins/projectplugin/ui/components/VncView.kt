package org.mjdev.plugins.projectplugin.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.json.JSONObject
import org.mjdev.plugins.projectplugin.modules.HotModule
import java.awt.BorderLayout
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import javax.swing.JPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread

@Composable
fun VncView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val host = remember(node) { node.optString("host", "") }
    val port = remember(node) { node.optString("port", "5900").toInt() }
    val vncState = remember(host, port) { VncViewState(host, port) }
    var isVisible by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            isVisible = when (event) {
                Lifecycle.Event.ON_START,
                Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_STOP,
                Lifecycle.Event.ON_PAUSE -> false
                else -> isVisible
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(isVisible) {
        if (isVisible && !vncState.isConnected) {
            withContext(Dispatchers.IO) {
                vncState.connect()
            }
        } else if (!isVisible && vncState.isConnected) {
            vncState.disconnect()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SwingPanel(
            modifier = Modifier.fillMaxSize(),
            factory = { vncState.createPanel() }
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            vncState.disconnect()
        }
    }
}

class VncViewState(
    val host: String,
    val port: Int = 5900
) {
    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null
    private var displayPanel: VncDisplayPanel? = null
    private var width = 0
    private var height = 0

    var isConnected: Boolean = false
        private set

    fun createPanel(): JPanel = VncDisplayPanel().also {
        displayPanel = it
    }

    fun connect() {
        if (isConnected) return
        try {
            socket = Socket(host, port)
            input = DataInputStream(socket?.getInputStream())
            output = DataOutputStream(socket?.getOutputStream())
            performHandshake()
            readServerInit()
            setPixelFormat()
            setEncodings()
            requestFramebufferUpdate()
            isConnected = true
            startReceivingUpdates()
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
        }
    }

    private fun performHandshake() {
        val version = ByteArray(12)
        input?.readFully(version)
        output?.write("RFB 003.003\n".toByteArray())
        output?.flush()
        val authScheme = input?.readInt() ?: 0
        if (authScheme != 1) {
            throw Exception("Authentication required")
        }
        output?.writeByte(1)
        output?.flush()
    }

    private fun readServerInit() {
        width = input?.readUnsignedShort() ?: 0
        height = input?.readUnsignedShort() ?: 0
        input?.skipBytes(16)
        val nameLength = input?.readInt() ?: 0
        input?.skipBytes(nameLength)
    }

    private fun setPixelFormat() {
        output?.writeByte(0)
        output?.write(ByteArray(3))
        output?.writeByte(32)
        output?.writeByte(24)
        output?.writeByte(0)
        output?.writeByte(1)
        output?.writeShort(255)
        output?.writeShort(255)
        output?.writeShort(255)
        output?.writeByte(16)
        output?.writeByte(8)
        output?.writeByte(0)
        output?.write(ByteArray(3))
        output?.flush()
    }

    private fun setEncodings() {
        output?.writeByte(2)
        output?.writeByte(0)
        output?.writeShort(1)
        output?.writeInt(0)
        output?.flush()
    }

    private fun requestFramebufferUpdate() {
        output?.writeByte(3)
        output?.writeByte(0)
        output?.writeShort(0)
        output?.writeShort(0)
        output?.writeShort(width)
        output?.writeShort(height)
        output?.flush()
    }

    private fun startReceivingUpdates() {
        thread {
            while (isConnected) {
                try {
                    val messageType = input?.readUnsignedByte() ?: break
                    if (messageType == 0) {
                        input?.skipBytes(1)
                        val numRects = input?.readUnsignedShort() ?: 0
                        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
                        for (i in 0 until numRects) {
                            val x = input?.readUnsignedShort() ?: 0
                            val y = input?.readUnsignedShort() ?: 0
                            val w = input?.readUnsignedShort() ?: 0
                            val h = input?.readUnsignedShort() ?: 0
                            val encoding = input?.readInt() ?: 0
                            if (encoding == 0) {
                                val pixels = ByteArray(w * h * 4)
                                input?.readFully(pixels)
                                for (py in 0 until h) {
                                    for (px in 0 until w) {
                                        val idx = (py * w + px) * 4
                                        val rgb = ((pixels[idx + 2].toInt() and 0xFF) shl 16) or
                                                ((pixels[idx + 1].toInt() and 0xFF) shl 8) or
                                                (pixels[idx].toInt() and 0xFF)
                                        image.setRGB(x + px, y + py, rgb)
                                    }
                                }
                            }
                        }
                        displayPanel?.updateImage(image)
                        requestFramebufferUpdate()
                    }
                } catch (e: Exception) {
                    break
                }
            }
        }
    }

    fun disconnect() {
        if (!isConnected) return
        isConnected = false
        input?.close()
        output?.close()
        socket?.close()
        input = null
        output = null
        socket = null
    }

    private class VncDisplayPanel : JPanel(BorderLayout()) {
        private var currentImage: BufferedImage? = null

        fun updateImage(image: BufferedImage) {
            currentImage = image
            repaint()
        }

        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            currentImage?.let { img ->
                g.drawImage(img, 0, 0, width, height, null)
            }
        }
    }
}
