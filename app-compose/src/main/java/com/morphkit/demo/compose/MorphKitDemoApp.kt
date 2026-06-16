package com.morphkit.demo.compose

import android.app.Application
import com.morphkit.core.MorphKit
import com.morphkit.widget.registerDefaultWidgets

class MorphKitDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MorphKit.autoInit(this) {
            registerDefaultWidgets()
        }
    }
}
