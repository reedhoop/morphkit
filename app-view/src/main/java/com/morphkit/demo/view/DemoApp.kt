package com.morphkit.demo.view

import android.app.Application
import com.morphkit.core.MorphKit
import com.morphkit.widget.registerDefaultWidgets

class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MorphKit.autoInit(this) {
            registerDefaultWidgets()
        }
    }
}
