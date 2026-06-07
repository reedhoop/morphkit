package com.morphkit.engine

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log

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

            MorphKit.autoInit(application)
            Log.d(TAG, "MorphInitProvider: 零侵入自动初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "MorphInitProvider: 自动初始化异常，宿主 App 需手动调用 MorphKit.init()", e)
        }

        return true
    }

    /**
     * 获取当前进程名。
     *
     * Android 9+ 使用 Application.getProcessName()（无需反射），
     * 低版本降级为 ActivityManager 方案。
     */
    private fun getCurrentProcessName(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            @Suppress("DEPRECATION")
            try {
                val pid = android.os.Process.myPid()
                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                am?.runningAppProcesses?.find { it.pid == pid }?.processName
                    ?: context.packageName
            } catch (e: Exception) {
                context.packageName
            }
        }
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
