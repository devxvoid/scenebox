package com.scenebox

import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel

/**
 * Flutter-facing contract for the Android torrent engine.
 *
 * The actual libtorrent implementation will be supplied by the NDK layer.
 * Keeping this boundary explicit prevents torrent lifecycle and native memory
 * management from leaking into Dart.
 */
class NativeTorrentBridge(private val context: Context) : MethodChannel.MethodCallHandler {
    private var activeMagnet: String? = null

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "torrent.start" -> {
                val magnet = call.argument<String>("magnet")
                if (magnet.isNullOrBlank()) {
                    result.error("INVALID_MAGNET", "A magnet URI is required", null)
                    return
                }
                activeMagnet = magnet
                result.success(mapOf("status" to "starting"))
            }
            "torrent.stop" -> {
                activeMagnet = null
                result.success(null)
            }
            "torrent.status" -> {
                result.success(mapOf(
                    "active" to (activeMagnet != null),
                    "magnet" to activeMagnet,
                ))
            }
            else -> result.notImplemented()
        }
    }
}
