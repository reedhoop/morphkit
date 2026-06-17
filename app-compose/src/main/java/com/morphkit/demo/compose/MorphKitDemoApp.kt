package com.morphkit.demo.compose

import android.app.Application
import com.morphkit.core.MorphKit
import com.morphkit.widget.registerDefaultWidgets

class MorphKitDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            MorphKit.autoInit(this) {
                registerDefaultWidgets()
            }
        } catch (t: Throwable) {
            android.util.Log.e("MorphKit", "MorphKitDemoApp autoInit 失败", t)
        }
    }
}
