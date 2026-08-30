package com.scenebox

import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/** Registers the Flutter torrent API and owns the Android stream endpoint. */
class TorrentMethodChannel(context: Context) : MethodChannel.MethodCallHandler {
    private val native = NativeTorrentBridge(context)
    private var controller: TorrentStreamController? = null
    private var selectedFile: TorrentFile? = null

    fun attach(channel: MethodChannel) {
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        try {
            when (call.method) {
                "torrent.start" -> native.onMethodCall(call, result)
                "torrent.stop" -> {
                    controller?.close()
                    controller = null
                    native.onMethodCall(call, result)
                }
                "torrent.status" -> native.onMethodCall(call, result)
                "torrent.selectFile" -> {
                    val index = call.argument<Int>("fileIndex")
                    val path = call.argument<String>("path")
                    val offset = call.argument<Long>("offset")
                    val length = call.argument<Long>("length")
                    if (index == null || path == null || offset == null || length == null || length < 0) {
                        result.error("INVALID_FILE", "fileIndex, path, offset and length are required", null)
                        return
                    }
                    selectedFile = TorrentFile(index, path, offset, length)
                    result.success(mapOf("selected" to true))
                }
                "torrent.streamUrl" -> {
                    val file = selectedFile ?: run {
                        val index = call.argument<Int>("fileIndex")
                        val path = call.argument<String>("path")
                        val offset = call.argument<Long>("offset")
                        val length = call.argument<Long>("length")
                        if (index == null || path == null || offset == null || length == null || length < 0) {
                            result.error("INVALID_FILE", "Select a valid torrent file first", null)
                            return
                        }
                        TorrentFile(index, path, offset, length)
                    }
                    selectedFile = file
                    // The concrete byte reader is injected when the native piece
                    // bridge is initialized by the application host.
                    val streamController = controller
                    if (streamController == null) {
                        result.error("STREAM_NOT_INITIALIZED", "Torrent byte reader is not initialized", null)
                        return
                    }
                    result.success(streamController.start(file))
                }
                else -> result.notImplemented()
            }
        } catch (error: Exception) {
            result.error("TORRENT_CHANNEL_ERROR", error.message, null)
        }
    }

    fun setStreamController(value: TorrentStreamController?) {
        controller?.close()
        controller = value
    }

    fun close() {
        controller?.close()
        controller = null
    }
}
