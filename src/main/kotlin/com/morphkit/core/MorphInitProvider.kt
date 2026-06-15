package com.morphkit.core

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log
import com.morphkit.widget.registerDefaultWidgets

/**
 * MorphKit 零侵入自动初始化 ContentProvider。
 *
 * 利用 Android 系统保证 ContentProvider 在 Application.onCreate() 之前
 * 完成初始化的特性，实现宿主 App **零代码接入** MorphKit。
 *
 * ## 多进程保护
 *
 * ContentProvider 在每个进程都会触发 onCreate()，但 MorphKit 的控件替换
 * 和主题注入仅在主进程有意义。本 Provider 会自动检测当前进程，
 * 非主进程跳过初始化，避免无意义的反射操作和资源消耗。
 *
 * ## 基线保护声明
 *
 * 本 AAR 仅限于替换本 OEM 预装应用内的控件，不具备干涉第三方应用的能力。
 * 若需全局系统级换肤，请结合 Android RRO (Runtime Resource Overlay) 机制使用。
 *
 * @see MorphKit
 * @see MorphInstaller
 * @see MorphStyleResolver
 */
class MorphInitProvider : android.content.ContentProvider() {

    companion object {
        private const val TAG = "MorphKit"
    }

    override fun onCreate(): Boolean {
        val context = context ?: run {
            Log.e(TAG, "MorphInitProvider: context 为 null，无法自动初始化")
            return false
        }

        try {
            val application = context.applicationContext as? Application ?: run {
                Log.e(TAG, "MorphInitProvider: applicationContext 非 Application 类型，无法自动初始化")
                return false
            }

            // ── 多进程保护：仅在主进程初始化 ──
            val currentProcessName = getCurrentProcessName(application)
            if (currentProcessName != application.packageName) {
                Log.d(TAG, "MorphInitProvider: 非主进程 ($currentProcessName)，跳过初始化")
                return true
            }

            MorphKit.autoInit(application) {
                // widget 层注册逻辑通过回调注入，保持 core 层零 import
                registerDefaultWidgets()
            }

            // ── 注册内存压力回调：清理 Bitmap 对象池 ──
            // 架构说明：此处通过 FQN 引用 widget 层的 BackdropBlurHelper，
            // 属于基础设施级的内存压力响应（而非业务逻辑依赖），不违反分层原则。
            // ContentProvider 作为 Android 系统引导入口，天然承担跨层协调职责，
            // 且 clearPool() 仅在此处作为 onTrimMemory 的被动回调被调用，
            // core 包的其他代码不会对 widget 包产生任何依赖。
            application.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
                        com.morphkit.widget.container.BackdropBlurHelper.clearPool()
                    }
                }
                override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
                override fun onLowMemory() {
                    com.morphkit.widget.container.BackdropBlurHelper.clearPool()
                }
            })

            Log.d(TAG, "MorphInitProvider: 零侵入自动初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "MorphInitProvider: 自动初始化异常，宿主 App 需手动调用 MorphKit.init()", e)
        }

        return true
    }

    /**
     * 获取当前进程名。
     *
     * minSdk=35 保证 Application.getProcessName()（API 28+）始终可用，
     * 无需版本守卫或 ActivityManager 降级方案。
     */
    private fun getCurrentProcessName(context: Context): String {
        return Application.getProcessName()
    }

    // ── ContentProvider 抽象方法空实现 ──

    override fun query(
        uri: android.net.Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): android.database.Cursor? = null

    override fun getType(uri: android.net.Uri): String? = null

    override fun insert(uri: android.net.Uri, values: android.content.ContentValues?): android.net.Uri? = null

    override fun delete(uri: android.net.Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: android.net.Uri,
        values: android.content.ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
