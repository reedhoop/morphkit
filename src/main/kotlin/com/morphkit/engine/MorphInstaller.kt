package com.morphkit.engine

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MorphKit 全局自动注入器。
 *
 * 通过注册 [Application.ActivityLifecycleCallbacks]，在每个 [Activity] 的
 * `onActivityPreCreated` 阶段自动将 [MorphFactory2] 注入到该 Activity 的
 * [LayoutInflater] 中，实现无侵入的控件替换拦截。
 *
 * ## 核心难点：三方库 Factory 抢占
 *
 * 多个三方库（如 AppCompat、SkinManager、换肤框架等）可能在同一 Activity 上
 * 先后设置各自的 Factory2。本注入器采用**装饰器链**策略：
 *
 * ```
 * LayoutInflater.mFactory2 → MorphFactory2 → 三方 Factory2 → AppCompat Factory2 → …
 * ```
 *
 * ## 防重复注入保护
 *
 * 系统环境可能比较复杂（多进程、插件化框架等），必须确保 [MorphFactory2]
 * 只被注入一次，避免多重代理导致性能损耗或死循环。本注入器提供两层防护：
 *
 * 1. **Activity 级防重入**：在 [injectFactory2] 中检查当前 LayoutInflater 的
 *    factory2 是否已是 [MorphFactory2] 实例，若是则直接跳过。
 * 2. **全局级防重入锁**：使用 [injected] 原子标记，确保 [install] 方法
 *    仅注册一次 ActivityLifecycleCallbacks，避免重复注册导致多次回调。
 *
 * ## 反射安全
 *
 * Android 12（API 31）起，系统对反射访问非公开 API 施加了更严格的限制，
 * 本注入器对所有反射路径均做了防御性捕获，注入失败时仅输出日志，**绝不崩溃**。
 *
 * ## 基线保护声明
 *
 * 本 AAR 仅限于替换本 OEM 预装应用内的控件，不具备干涉第三方应用的能力。
 * 若需全局系统级换肤，请结合 Android RRO (Runtime Resource Overlay) 机制使用。
 *
 * @see MorphFactory2
 * @see MorphKit
 */
object MorphInstaller {

    private const val TAG = "MorphKit"

    /**
     * 全局防重入标记。
     *
     * 确保全局 [install] 仅执行一次，防止重复注册
     * ActivityLifecycleCallbacks 导致多次回调。
     */
    private val installed = AtomicBoolean(false)

    /**
     * 缓存 LayoutInflater 类的 `mFactory2` 字段引用。
     */
    private val sFactory2Field: Field? by lazy {
        resolveField(LayoutInflater::class.java, "mFactory2")
    }

    /**
     * 缓存 LayoutInflater 类的 `mFactory` 字段引用。
     */
    private val sFactoryField: Field? by lazy {
        resolveField(LayoutInflater::class.java, "mFactory")
    }

    /**
     * 安装全局自动注入。
     *
     * 注册 [Application.ActivityLifecycleCallbacks]，在每个 [Activity]
     * 启动前自动注入 [MorphFactory2]。
     *
     * 使用 [AtomicBoolean] 保证全局只注册一次，即使被多次调用也仅首次生效。
     *
     * @param application 应用实例
     */
    fun install(application: Application) {
        if (!installed.compareAndSet(false, true)) {
            Log.d(TAG, "MorphInstaller.install 已执行过，跳过重复注册")
            return
        }

        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                injectFactory2(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    /**
     * 对单个 Activity 执行 Factory2 注入。
     *
     * 执行流程：
     * 1. 获取 Activity 的 LayoutInflater 实例
     * 2. 读取当前 factory2，若已是 [MorphFactory2] 则跳过（防重复注入）
     * 3. 以当前 factory2 为 originalFactory 创建 [MorphFactory2]
     * 4. 反射同时设置 mFactory2 与 mFactory 字段
     * 5. 任一环节异常均安全降级，不崩溃
     *
     * @param activity 目标 Activity
     */
    private fun injectFactory2(activity: Activity) {
        try {
            val inflater = activity.layoutInflater

            // ═══════════════════════════════════════════════════
            // 1. 读取当前 Factory2
            // ═══════════════════════════════════════════════════
            val currentFactory2 = inflater.factory2

            // 已是 MorphFactory2，说明本次 Activity 已注入过，跳过
            if (currentFactory2 is MorphFactory2) {
                return
            }

            // ═══════════════════════════════════════════════════
            // 2. 创建 MorphFactory2 包装当前 Factory2
            //    传入 MorphKit 解析出的 finalThemeResId，
            //    用于 ContextThemeWrapper 无侵入式皮肤注入
            // ═══════════════════════════════════════════════════
            val morphFactory = MorphFactory2(currentFactory2, MorphKit.finalThemeResId)

            // ═══════════════════════════════════════════════════
            // 3. 反射设置 mFactory2 与 mFactory
            // ═══════════════════════════════════════════════════
            setFactoryFields(inflater, morphFactory)

            Log.d(TAG, "Factory2 注入成功: ${activity.javaClass.simpleName}")
        } catch (e: Throwable) {
            Log.e(TAG, "Factory2 注入失败，放弃本次注入: ${activity.javaClass.simpleName}", e)
        }
    }

    /**
     * 反射同时设置 LayoutInflater 的 `mFactory2` 和 `mFactory` 字段。
     */
    private fun setFactoryFields(inflater: LayoutInflater, factory: MorphFactory2) {
        sFactory2Field?.let { field ->
            setFieldValue(field, inflater, factory)
        } ?: run {
            Log.w(TAG, "反射字段 mFactory2 不可用，尝试 LayoutInflater.setFactory2 降级")
            fallbackSetFactory2(inflater, factory)
        }

        sFactoryField?.let { field ->
            setFieldValue(field, inflater, factory)
        } ?: run {
            Log.w(TAG, "反射字段 mFactory 不可用，mFactory 与 mFactory2 可能不一致")
        }
    }

    /**
     * 降级方案：通过 LayoutInflater 自身的 setFactory2 方法设置。
     */
    private fun fallbackSetFactory2(inflater: LayoutInflater, factory: MorphFactory2) {
        try {
            inflater.factory2 = factory
        } catch (e: IllegalStateException) {
            Log.w(TAG, "setFactory2 降级失败（已存在 Factory），放弃反射注入", e)
        }
    }

    /**
     * 安全反射获取类的声明字段。
     */
    private fun resolveField(clazz: Class<*>, fieldName: String): Field? {
        return try {
            val field = clazz.getDeclaredField(fieldName)
            if (!field.isAccessible) {
                field.isAccessible = true
            }
            field
        } catch (e: NoSuchFieldException) {
            Log.e(TAG, "反射获取字段 $fieldName 不存在: ${clazz.name}", e)
            null
        } catch (e: UnsupportedOperationException) {
            Log.e(TAG, "反射获取字段 $fieldName 被 Android 限制拦截 (UnsupportedOperationException)", e)
            null
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "反射获取字段 $fieldName 被拒绝访问 (IllegalAccessException)", e)
            null
        } catch (e: Throwable) {
            Log.e(TAG, "反射获取字段 $fieldName 发生未知异常", e)
            null
        }
    }

    /**
     * 安全反射设置字段值。
     */
    private fun setFieldValue(field: Field, target: Any, value: Any) {
        try {
            field.set(target, value)
        } catch (e: UnsupportedOperationException) {
            Log.e(TAG, "反射写入字段 ${field.name} 被 Android 限制拦截 (UnsupportedOperationException)", e)
            throw e
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "反射写入字段 ${field.name} 被拒绝访问 (IllegalAccessException)", e)
            throw e
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "反射写入字段 ${field.name} 类型不匹配", e)
            throw e
        }
    }
}
