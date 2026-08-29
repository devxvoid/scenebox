package com.scenebox

import java.io.Closeable
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

/**
 * Minimal localhost HTTP boundary for the native torrent engine.
 *
 * It deliberately does not expose torrent bytes yet: the engine must provide
 * a bounded range reader before this server can safely serve media to Media3.
 */
class TorrentHttpServer : Closeable {
    private val executor = Executors.newCachedThreadPool()
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var running = false

    fun start(): Int {
        if (running) return serverSocket?.localPort ?: -1
        serverSocket = ServerSocket(0, 16, java.net.InetAddress.getByName("127.0.0.1"))
        running = true
        executor.execute {
            while (running) {
                try {
                    val socket = serverSocket?.accept() ?: break
                    executor.execute { handle(socket) }
                } catch (_: Exception) {
                    if (running) throw RuntimeException("Torrent HTTP server failed")
                }
            }
        }
        return serverSocket!!.localPort
    }

    private fun handle(socket: Socket) {
        socket.use { client ->
            val input = client.getInputStream().bufferedReader()
            val request = input.readLine() ?: return
            while (input.readLine()?.isNotEmpty() == true) { }

            // No byte-serving implementation is enabled until a validated
            // TorrentEngine range reader is connected here.
            val body = "SceneBox torrent stream is not ready"
            val response = "HTTP/1.1 503 Service Unavailable\r\n" +
                "Content-Type: text/plain; charset=utf-8\r\n" +
                "Content-Length: ${body.toByteArray().size}\r\n" +
                "Connection: close\r\n\r\n$body"
            client.getOutputStream().write(response.toByteArray())
        }
    }

    override fun close() {
        running = false
        serverSocket?.close()
        serverSocket = null
        executor.shutdownNow()
    }
}
