package com.example.webcam

import android.graphics.Rect
import android.graphics.YuvImage
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * A lightweight HTTP MJPEG server for fallback compatibility.
 */
class MjpegServer(private val port: Int) {
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newCachedThreadPool()
    private var isRunning = false
    private val clients = mutableListOf<Socket>()

    fun start() {
        isRunning = true
        executor.execute {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val client = serverSocket?.accept()
                    client?.let {
                        clients.add(it)
                        handleClient(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stop() {
        isRunning = false
        serverSocket?.close()
        clients.forEach { it.close() }
        clients.clear()
    }

    private fun handleClient(socket: Socket) {
        executor.execute {
            try {
                val outputStream = socket.getOutputStream()
                outputStream.write("HTTP/1.0 200 OK\r\n".toByteArray())
                outputStream.write("Server: DroidCamPro\r\n".toByteArray())
                outputStream.write("Connection: close\r\n".toByteArray())
                outputStream.write("Content-Type: multipart/x-mixed-replace;boundary=--boundary\r\n".toByteArray())
                outputStream.write("\r\n".toByteArray())
            } catch (e: Exception) {
                clients.remove(socket)
            }
        }
    }

    fun pushFrame(jpegData: ByteArray) {
        val boundary = "\r\n--boundary\r\nContent-Type: image/jpeg\r\nContent-Length: ${jpegData.size}\r\n\r\n".toByteArray()
        val iterator = clients.iterator()
        while (iterator.hasNext()) {
            val client = iterator.next()
            try {
                val out = client.getOutputStream()
                out.write(boundary)
                out.write(jpegData)
                out.flush()
            } catch (e: Exception) {
                client.close()
                iterator.remove()
            }
        }
    }
}
