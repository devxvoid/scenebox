package com.scenebox

import android.content.Context
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*
import java.util.Base64

/** Flutter-facing piece scheduling/read contract. */
class TorrentPieceBridge(private val context: Context) : MethodChannel.MethodCallHandler {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val native = NativePieceReader()

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "torrent.pieceAvailable" -> {
                val piece = call.argument<Int>("piece")
                if (piece == null) result.error("INVALID_PIECE", "piece is required", null)
                else result.success(native.hasPiece(piece))
            }
            "torrent.setPiecePriority" -> {
                val piece = call.argument<Int>("piece")
                val priority = call.argument<Int>("priority")
                if (piece == null || priority == null) {
                    result.error("INVALID_ARGUMENT", "piece and priority are required", null)
                } else {
                    native.setPriority(piece, priority)
                    result.success(null)
                }
            }
            "torrent.setPieceDeadline" -> {
                val piece = call.argument<Int>("piece")
                val deadline = call.argument<Int>("deadlineMs")
                if (piece == null || deadline == null) {
                    result.error("INVALID_ARGUMENT", "piece and deadlineMs are required", null)
                } else {
                    native.setDeadline(piece, deadline)
                    result.success(null)
                }
            }
            "torrent.clearPieceDeadline" -> {
                val piece = call.argument<Int>("piece")
                if (piece == null) result.error("INVALID_PIECE", "piece is required", null)
                else { native.clearDeadline(piece); result.success(null) }
            }
            "torrent.readPiece" -> {
                val piece = call.argument<Int>("piece")
                if (piece == null) {
                    result.error("INVALID_PIECE", "piece is required", null)
                    return
                }
                scope.launch {
                    val bytes = native.readPiece(piece)
                    withContext(Dispatchers.Main) {
                        if (bytes == null) result.error("PIECE_UNAVAILABLE", "Piece is not available", null)
                        else result.success(Base64.getEncoder().encodeToString(bytes))
                    }
                }
            }
            else -> result.notImplemented()
        }
    }

    fun close() = scope.cancel()

    private class NativePieceReader {
        fun hasPiece(piece: Int): Boolean = nativeHasPiece(piece)
        fun setPriority(piece: Int, priority: Int) = nativeSetPriority(piece, priority)
        fun setDeadline(piece: Int, deadlineMs: Int) = nativeSetDeadline(piece, deadlineMs)
        fun clearDeadline(piece: Int) = nativeClearDeadline(piece)
        fun readPiece(piece: Int): ByteArray? = nativeReadPiece(piece)

        private external fun nativeHasPiece(piece: Int): Boolean
        private external fun nativeSetPriority(piece: Int, priority: Int)
        private external fun nativeSetDeadline(piece: Int, deadlineMs: Int)
        private external fun nativeClearDeadline(piece: Int)
        private external fun nativeReadPiece(piece: Int): ByteArray?
    }
}
