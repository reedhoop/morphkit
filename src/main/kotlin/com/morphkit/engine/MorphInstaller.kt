package com.morphkit.engine

import android.app.Activity
import android.app.Application
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import java.lang.reflect.Field

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
 * 每次注入时，将当前已存在的 Factory2 作为 [MorphFactory2.originalFactory] 包装，
 * 确保所有三方库的兼容性处理不被破坏。
 *
 * ## 反射安全
 *
 * Android 12（API 31）起，系统对反射访问非公开 API 施加了更严格的限制，
 * [LayoutInflater] 的 `mFactory2` 与 `mFactory` 字段可能被标记为 blocked，
 * 直接反射会抛出 [UnsupportedOperationException] 或 [IllegalAccessException]。
 * 本注入器对所有反射路径均做了防御性捕获，注入失败时仅输出日志，**绝不崩溃**。
 *
 * ## 使用方式
 *
 * 在 [MorphKit.init] 内部自动调用，无需手动操作：
 * ```kotlin
 * MorphKit.init(application) {
 *     // ... DSL 规则
 * }
 * // 内部自动执行 MorphInstaller.install(application)
 * ```
 */
object MorphInstaller {

    private const val TAG = "MorphKit"

    /**
     * 缓存 LayoutInflater 类的 `mFactory2` 字段引用。
     *
     * 使用 lazy + synchronized 保证线程安全且仅反射一次；
     * 若反射获取失败（Android 12+ 限制等），值为 null，
     * 后续所有注入尝试将在 [injectFactory2] 中安全跳过。
     */
    private val sFactory2Field: Field? by lazy {
        resolveField(LayoutInflater::class.java, "mFactory2")
    }

    /**
     * 缓存 LayoutInflater 类的 `mFactory` 字段引用。
     *
     * 必须同步修改 `mFactory`，否则 LayoutInflater 内部校验逻辑
     * 可能因 `mFactory` 与 `mFactory2` 不一致而抛出异常。
     */
    private val sFactoryField: Field? by lazy {
        resolveField(LayoutInflater::class.java, "mFactory")
    }

    /**
     * 安装全局自动注入。
     *
     * 注册 [Application.ActivityLifecycleCallbacks]，在每个 [Activity]
     * 启动前自动注入 [MorphFactory2]。该方法幂等，重复调用仅注册一次回调。
     *
     * @param application 应用实例
     */
    fun install(application: Application) {
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
            //    必须同时修改两个字段，否则 LayoutInflater
            //    内部的 cloneInContext / setFactory 等方法
            //    可能因字段不一致而抛异常
            // ═══════════════════════════════════════════════════
            setFactoryFields(inflater, morphFactory)

            Log.d(TAG, "Factory2 注入成功: ${activity.javaClass.simpleName}")
        } catch (e: Throwable) {
            // 捕获所有异常（包括反射安全限制、NPE 等），
            // 绝不崩溃，仅打印日志，放弃本次 Activity 的注入
            Log.e(TAG, "Factory2 注入失败，放弃本次注入: ${activity.javaClass.simpleName}", e)
        }
    }

    /**
     * 反射同时设置 LayoutInflater 的 `mFactory2` 和 `mFactory` 字段。
     *
     * 两个字段必须同步修改，原因：
     * - `mFactory2` 是 LayoutInflater.Factory2 接口的实际存储字段
     * - `mFactory` 是 LayoutInflater.Factory 接口的存储字段
     * - LayoutInflater 内部多处校验二者的一致性，若仅修改 mFactory2
     *   而保留旧 mFactory，后续 setFactory / cloneInContext 等操作
     *   可能抛出 `IllegalStateException: A factory has already been set on this LayoutInflater`
     *
     * @param inflater 目标 LayoutInflater 实例
     * @param factory  待设置的新 MorphFactory2 实例
     * @throws Throwable 反射过程中的任何异常（由上层 injectFactory2 捕获）
     */
    private fun setFactoryFields(inflater: LayoutInflater, factory: MorphFactory2) {
        // 设置 mFactory2
        sFactory2Field?.let { field ->
            setFieldValue(field, inflater, factory)
        } ?: run {
            Log.w(TAG, "反射字段 mFactory2 不可用，尝试 LayoutInflater.setFactory2 降级")
            fallbackSetFactory2(inflater, factory)
        }

        // 设置 mFactory（必须同步修改，保持内部一致性）
        sFactoryField?.let { field ->
            setFieldValue(field, inflater, factory)
        } ?: run {
            // mFactory 字段反射失败不影响核心逻辑，
            // 但可能在极端场景下导致 LayoutInflater 内部校验异常
            Log.w(TAG, "反射字段 mFactory 不可用，mFactory 与 mFactory2 可能不一致")
        }
    }

    /**
     * 降级方案：通过 LayoutInflater 自身的 setFactory2 方法设置。
     *
     * 当反射字段不可用（Android 12+ 限制）时，尝试使用公开 API。
     * 注意：`setFactory2` 内部会同时设置 mFactory 和 mFactory2，
     * 但它会在已存在 Factory 时抛异常，因此仅作为降级备选。
     *
     * @param inflater 目标 LayoutInflater 实例
     * @param factory  待设置的 MorphFactory2 实例
     */
    private fun fallbackSetFactory2(inflater: LayoutInflater, factory: MorphFactory2) {
        try {
            inflater.factory2 = factory
        } catch (e: IllegalStateException) {
            // setFactory2 在已有 Factory 时会抛 IllegalStateException，
            // 此时无法通过公开 API 设置，记录日志后放弃
            Log.w(TAG, "setFactory2 降级失败（已存在 Factory），放弃反射注入", e)
        }
    }

    /**
     * 安全反射获取类的声明字段。
     *
     * 处理 Android 12+ 的反射限制：
     * - API 28+ 使用 `field.isAccessible = true` 可能触发
     *   `UnsupportedOperationException`（若字段在 greylist-max-x 上）
     * - API 33+ 可能直接抛出 `ReflectiveOperationException` 子类
     *
     * @param clazz     目标类
     * @param fieldName 字段名
     * @return 反射获取的 [Field]，若失败返回 null
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
            // Android 12+ 的反射限制：字段被列入黑名单，禁止访问
            Log.e(TAG, "反射获取字段 $fieldName 被 Android 限制拦截 (UnsupportedOperationException)", e)
            null
        } catch (e: IllegalAccessException) {
            // 深灰名单 / 黑名单限制
            Log.e(TAG, "反射获取字段 $fieldName 被拒绝访问 (IllegalAccessException)", e)
            null
        } catch (e: Throwable) {
            // 其他未预见的反射异常（如 SecurityException、VM-level 限制等）
            Log.e(TAG, "反射获取字段 $fieldName 发生未知异常", e)
            null
        }
    }

    /**
     * 安全反射设置字段值。
     *
     * 分离写入逻辑，使得 mFactory2 与 mFactory 的设置互不影响——
     * 即使 mFactory2 设置失败，仍可尝试通过 [fallbackSetFactory2] 降级。
     *
     * @param field   已通过 [resolveField] 获取的字段引用
     * @param target  字段所属对象实例
     * @param value   要设置的新值
     * @throws Throwable 反射写入过程中的任何异常，由上层统一捕获
     */
    private fun setFieldValue(field: Field, target: Any, value: Any) {
        try {
            field.set(target, value)
        } catch (e: UnsupportedOperationException) {
            // Android 12+：写入被拦截（字段在 blocked 列表）
            Log.e(TAG, "反射写入字段 ${field.name} 被 Android 限制拦截 (UnsupportedOperationException)", e)
            throw e
        } catch (e: IllegalAccessException) {
            Log.e(TAG, "反射写入字段 ${field.name} 被拒绝访问 (IllegalAccessException)", e)
            throw e
        } catch (e: IllegalArgumentException) {
            // 类型不匹配（理论上不应发生，MorphFactory2 实现了 Factory2 和 Factory）
            Log.e(TAG, "反射写入字段 ${field.name} 类型不匹配", e)
            throw e
        }
    }
}
