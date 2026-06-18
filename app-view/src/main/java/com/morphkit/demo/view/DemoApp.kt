package com.morphkit.demo.view

import android.app.Application
import com.morphkit.core.MorphKit
import com.morphkit.core.StylePolicy
import com.morphkit.widget.registerDefaultWidgets

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val pendingPolicy = getSharedPreferences("morphkit_demo", MODE_PRIVATE)
                .getString("pending_style_policy", null)
                ?.let { runCatching { StylePolicy.valueOf(it) }.getOrNull() }
            MorphKit.autoInit(this) {
                pendingPolicy?.let { stylePolicy(it) }
                registerDefaultWidgets()
            }
        } catch (t: Throwable) {
            android.util.Log.e("MorphKit", "DemoApp autoInit 失败", t)
        }
    }
}
