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
fun RdpView(
    module: HotModule,
    node: JSONObject,
    state: MutableMap<String, Any?>,
    onAction: (id: String, action: String, state: Map<String, Any?>) -> Unit,
) {
    val host = remember(node) { node.optString("host", "") }
    val port = remember(node) { node.optString("port", "3389").toInt() }
    val username = remember(node) { node.optString("username", "") }
    val password = remember(node) { node.optString("password", "") }
    val rdpState = remember(host, port, username) {
        RdpViewState(host, port, username, password)
    }
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
        if (isVisible && !rdpState.isConnected) {
            withContext(Dispatchers.IO) {
                rdpState.connect()
            }
        } else if (!isVisible && rdpState.isConnected) {
            rdpState.disconnect()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SwingPanel(
            modifier = Modifier.fillMaxSize(),
            factory = { rdpState.createPanel() }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            rdpState.disconnect()
        }
    }
}

class RdpViewState(
    val host: String,
    val port: Int = 3389,
    val username: String = "",
    val password: String = ""
) {
    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: DataOutputStream? = null
    private var displayPanel: RdpDisplayPanel? = null
    private var width = 1024
    private var height = 768
    var isConnected: Boolean = false
        private set

    fun createPanel(): JPanel = RdpDisplayPanel().also { displayPanel = it }

    fun connect() {
        if (isConnected) return

        try {
            socket = Socket(host, port)
            input = DataInputStream(socket?.getInputStream())
            output = DataOutputStream(socket?.getOutputStream())

            performHandshake()
            isConnected = true
            startReceivingUpdates()
        } catch (e: Exception) {
            e.printStackTrace()
            disconnect()
        }
    }

    private fun performHandshake() {
        output?.writeByte(0x03)
        output?.flush()

        val response = input?.readByte() ?: 0
        if (response.toInt() != 0x03) {
            throw Exception("RDP handshake failed")
        }

        sendConnectionRequest()
        sendClientInfo()
    }

    private fun sendConnectionRequest() {
        val request = byteArrayOf(
            0x03.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x13.toByte(),
            0x0e.toByte(), 0xe0.toByte(),
            0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x00.toByte(),
            0x00.toByte(), 0x01.toByte(),
            0x00.toByte(), 0x08.toByte(),
            0x00.toByte(), 0x03.toByte(),
            0x00.toByte(), 0x00.toByte(),
            0x00.toByte()
        )
        output?.write(request)
        output?.flush()

        val responseLength = input?.readUnsignedShort() ?: 0
        input?.skipBytes(responseLength - 2)
    }

    private fun sendClientInfo() {
        val buffer = ByteArray(256)
        var offset = 0

        buffer[offset++] = (width and 0xFF).toByte()
        buffer[offset++] = ((width shr 8) and 0xFF).toByte()
        buffer[offset++] = (height and 0xFF).toByte()
        buffer[offset++] = ((height shr 8) and 0xFF).toByte()

        val usernameBytes = username.toByteArray()
        System.arraycopy(usernameBytes, 0, buffer, offset, minOf(usernameBytes.size, 32))
        offset += 32

        val passwordBytes = password.toByteArray()
        System.arraycopy(passwordBytes, 0, buffer, offset, minOf(passwordBytes.size, 32))
        offset += 32

        output?.write(buffer, 0, offset)
        output?.flush()
    }

    private fun startReceivingUpdates() {
        thread {
            val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

            while (isConnected) {
                try {
                    val updateType = input?.readUnsignedByte() ?: break

                    when (updateType) {
                        0x01 -> {
                            val x = input?.readUnsignedShort() ?: 0
                            val y = input?.readUnsignedShort() ?: 0
                            val w = input?.readUnsignedShort() ?: 0
                            val h = input?.readUnsignedShort() ?: 0

                            val pixels = ByteArray(w * h * 3)
                            input?.readFully(pixels)

                            for (py in 0 until h) {
                                for (px in 0 until w) {
                                    val idx = (py * w + px) * 3
                                    val rgb = ((pixels[idx].toInt() and 0xFF) shl 16) or
                                            ((pixels[idx + 1].toInt() and 0xFF) shl 8) or
                                            (pixels[idx + 2].toInt() and 0xFF)
                                    if (x + px < width && y + py < height) {
                                        image.setRGB(x + px, y + py, rgb)
                                    }
                                }
                            }

                            displayPanel?.updateImage(image)
                        }
                        else -> Thread.sleep(10)
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

    private class RdpDisplayPanel : JPanel(BorderLayout()) {
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
