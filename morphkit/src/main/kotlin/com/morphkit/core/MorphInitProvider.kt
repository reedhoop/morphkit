package com.morphkit.core

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.util.Log

/**
 * MorphKit 零侵入自动初始化 ContentProvider。
 *
 * 利用 Android 系统保证 ContentProvider 在 Application.onCreate() 之前
 * 完成初始化的特性，实现宿主 App **零代码接入** MorphKit 主题能力。
 *
 * ## 能力边界
 *
 * 本 Provider 仅完成**引擎基础初始化**（主题解析、Factory2 注入、内存回调注册），
 * **不注册控件替换规则**。原因是 core 层按架构规范不能依赖 widget 层
 * （`WidgetRegistry.registerDefaultWidgets()` 位于 widget 层）。
 *
 * 若需控件替换能力，宿主须在 `Application.onCreate()` 中手动调用：
 * ```
 * MorphKit.init(this) {
 *     registerDefaultWidgets()
 * }
 * ```
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
            val currentProcessName = getCurrentProcessName()
            if (currentProcessName != application.packageName) {
                Log.d(TAG, "MorphInitProvider: 非主进程 ($currentProcessName)，跳过初始化")
                return true
            }

            MorphKit.autoInit(application) {
                // widget 层注册逻辑由宿主在 Application.onCreate() 中通过 MorphKit.init { registerDefaultWidgets() } 提供
                // core 层不能依赖 widget 层，此处 autoInit 仅完成引擎基础初始化
            }

            // ── 注册内存压力回调：通过 MorphKit 转发给所有注册的 MemoryTrimmable ──
            // core 层不直接依赖 widget 层，widget 层通过 MorphKit.registerMemoryTrimmable 注册
            application.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    // 响应中等及以上内存压力，以及前台运行临界压力（RUNNING_CRITICAL=15）
                    // RUNNING_CRITICAL 表示前台内存紧张，Bitmap 池应立即释放
                    if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE ||
                        level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL
                    ) {
                        MorphKit.onTrimMemory(level)
                    }
                }
                override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {}
                override fun onLowMemory() {
                    MorphKit.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE)
                }
            })

            Log.d(TAG, "MorphInitProvider: 零侵入自动初始化完成")
        } catch (e: Throwable) {
            // 捕获 Throwable 而非 Exception，防止 widget 层 NoClassDefFoundError 导致崩溃
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
    private fun getCurrentProcessName(): String {
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
