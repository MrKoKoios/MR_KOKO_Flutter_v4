package com.mrkoko.signalpro

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val CHANNEL = "com.mrkoko.signalpro/accessibility"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "startScan" -> {
                    val svc = MarketAccessibilityService.getInstance()
                    if (svc != null) {
                        svc.startChartScan()
                        result.success(true)
                    } else {
                        result.error("NO_SERVICE",
                            "Accessibility service not connected", null)
                    }
                }
                "stopScan" -> {
                    val svc = MarketAccessibilityService.getInstance()
                    svc?.stopChartScan()
                    result.success(true)
                }
                "isConnected" -> {
                    result.success(MarketAccessibilityService.getInstance() != null)
                }
                else -> result.notImplemented()
            }
        }
    }
}
