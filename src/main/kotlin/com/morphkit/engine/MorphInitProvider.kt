package com.morphkit.engine

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log

/**
 * MorphKit 零侵入自动初始化 ContentProvider。
 *
 * 利用 Android 系统保证 ContentProvider 在 Application.onCreate() 之前
 * 完成初始化的特性，实现宿主 App **零代码接入** MorphKit。
 *
 * ## 工作原理
 *
 * ```
 * App 进程启动
 *   └─ Application.attachBaseContext()
 *   └─ ContentProvider.onCreate()  ← MorphInitProvider 在此触发
 *       └─ MorphKit.autoInit(context)  ← 自动完成引擎初始化
 *           ├─ 读取 OEM 系统设置 (Settings.System "oem_ui_style")
 *           ├─ 解析 StylePolicy → 缓存 finalThemeResId
 *           └─ MorphInstaller.install(application)  ← 注册 Factory2 注入
 *   └─ Application.onCreate()
 *   └─ Activity 启动 → MorphFactory2 自动拦截控件创建
 * ```
 *
 * ## 宿主 App 接入方式
 *
 * **方式一：零代码接入（推荐）**
 *
 * 仅需在 `build.gradle` 中添加 AAR 依赖，无需任何代码修改。
 * MorphInitProvider 会自动完成初始化，使用默认控件替换规则和 OEM 系统设置。
 *
 * **方式二：自定义规则接入**
 *
 * 若宿主 App 需要自定义替换规则，仍可在 Application.onCreate() 中调用
 * [MorphKit.init]，此时 MorphInitProvider 检测到已初始化会自动跳过，
 * 不产生冲突。
 *
 * ## OEM 系统设置读取
 *
 * MorphInitProvider 在自动初始化时，会通过 [MorphStyleResolver] 读取
 * `Settings.System.getInt(contentResolver, "oem_ui_style", 0)`：
 * - 值为 1 → iOS 极简风
 * - 值为 2 → Pixel 原生风
 * - 值为 0（默认）→ AUTO 逻辑（根据 Dynamic Color 可用性自动选择）
 *
 * OEM 厂商只需在系统设置 App 中修改此值，所有接入 MorphKit 的预装 App
 * 即可瞬间整体变装，无需发版。
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
class MorphInitProvider : ContentProvider() {

    companion object {
        private const val TAG = "MorphKit"
    }

    /**
     * ContentProvider 自动初始化入口。
     *
     * 在 Application.onCreate() 之前由系统调用。
     * 执行 [MorphKit.autoInit] 完成引擎的零侵入初始化。
     *
     * 若宿主 App 已通过 [MorphKit.init] 手动初始化，则自动跳过，
     * 不产生冲突。
     *
     * @return true 表示 ContentProvider 初始化成功
     */
    override fun onCreate(): Boolean {
        val context = context ?: run {
            Log.e(TAG, "MorphInitProvider: context 为 null，无法自动初始化")
            return false
        }

        try {
            // ContentProvider 的 context 是 Application 级别的
            val application = context.applicationContext as? Application ?: run {
                Log.e(TAG, "MorphInitProvider: applicationContext 非 Application 类型，无法自动初始化")
                return false
            }
            MorphKit.autoInit(application)
            Log.d(TAG, "MorphInitProvider: 零侵入自动初始化完成")
        } catch (e: Exception) {
            Log.e(TAG, "MorphInitProvider: 自动初始化异常，宿主 App 需手动调用 MorphKit.init()", e)
        }

        return true
    }

    // ── ContentProvider 抽象方法空实现（本 Provider 不提供任何数据） ──

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
