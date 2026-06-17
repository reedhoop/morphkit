package com.morphkit.demo.view

import android.app.Application
import com.morphkit.core.MorphKit
import com.morphkit.widget.registerDefaultWidgets

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            MorphKit.autoInit(this) {
                registerDefaultWidgets()
            }
        } catch (t: Throwable) {
            android.util.Log.e("MorphKit", "DemoApp autoInit 失败", t)
        }
    }
}
