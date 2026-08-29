package com.scenebox

import android.content.Intent
import android.net.Uri
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val channelName = "scenebox/android"
    private var channel: MethodChannel? = null
    private var player: PlayerBridge? = null

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
        player = PlayerBridge(this)
        channel?.setMethodCallHandler(PlaybackChannel(this, player!!))

        intent?.data?.toString()?.let { link ->
            channel?.invokeMethod("initialDeepLink", link)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.toString()?.let { channel?.invokeMethod("onDeepLink", it) }
    }

    override fun onDestroy() {
        player?.release()
        player = null
        channel?.setMethodCallHandler(null)
        channel = null
        super.onDestroy()
    }
}
