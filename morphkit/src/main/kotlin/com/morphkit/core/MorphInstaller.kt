package com.morphkit.core

import android.app.Activity
import android.app.Application
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.morphkit.internal.ReflectionHelper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * MorphKit 全局自动注入器。
 *
 * ## AppCompat 共存策略（核心设计）
 *
 * 现代 App 必定使用 `AppCompatActivity`，它会在 `onCreate()` 中安装自己的
 * `AppCompatDelegateImpl` 作为 `Factory2`。MorphKit 必须与 AppCompat 共存，
 * 而非竞争。
 *
 * **注入时序与共存机制：**
 *
 * ```
 * onActivityPreCreated
 *   └─ MorphKit 通过反射设置 mFactory2 = MorphFactory2(originalFactory=null)
 *      （不设置 mFactorySet，不影响 AppCompat 后续安装判断）
 *
 * Activity.onCreate()
 *   ├─ AppCompatDelegateImpl.installViewFactory()
 *   │   └─ getFactory2() != null → AppCompat 跳过安装（仅日志提示）
 *   └─ setContentView() → MorphFactory2 拦截控件创建
 *       ├─ 命中替换规则 → 创建 Morph* 控件
 *       └─ 未命中 → originalFactory=null → 返回 null → 系统创建原生控件
 *
 * onActivityCreated
 *   └─ MorphKit 补充 AppCompat delegate 作为 originalFactory
 *      └─ morphFactory.updateOriginalFactory(appCompatDelegate)
 *          └─ 后续未命中替换规则的控件由 AppCompat delegate 创建
 *              （保留 AppCompat 着色、矢量图、background tint 等能力）
 * ```
 *
 * **为什么不在 onActivityCreated 注入？**
 *
 * 因为 `setContentView()` 在 `onCreate()` 内执行，如果等到 `onActivityCreated`
 * 再注入，初始布局的所有控件都不会被拦截，MorphKit 形同虚设。
 *
 * **为什么不在 onActivityPreCreated 让 AppCompat 先安装？**
 *
 * 因为 `onActivityPreCreated` 在 `Activity.onCreate()` 之前，
 * AppCompat 尚未安装 Factory2，MorphKit 无法获取到 AppCompat 的 delegate。
 *
 * **当前策略的优势：**
 *
 * 1. 初始布局的控件创建被 MorphKit 拦截（Morph* 控件继承 AppCompat 系控件，功能完整）
 * 2. 未命中替换规则的控件，在 `onActivityCreated` 后由 AppCompat delegate 创建
 * 3. 不与 AppCompat 竞争，不抛 IllegalStateException
 * 4. AppCompat 着色、矢量图、background tint 等能力完整保留
 */
object MorphInstaller {

    private const val TAG = "MorphKit"

    private val installed = AtomicBoolean(false)

    /** install/reset 复合操作同步锁，防止并发导致回调引用丢失或双重注册 */
    private val lock = Any()

    /** 持有已注册的生命周期回调引用，便于 reset() 时注销，避免双重注册 */
    @Volatile
    private var registeredCallback: Application.ActivityLifecycleCallbacks? = null

    /** 持有 Application 引用，用于 reset() 时注销回调 */
    @Volatile
    private var hostApplication: Application? = null

    /**
     * 安装 MorphKit 全局注入器。
     *
     * 注册 ActivityLifecycleCallbacks，在每个 Activity 的 onActivityPreCreated 阶段
     * 自动注入 MorphFactory2，在 onActivityCreated 阶段补充 AppCompat delegate。
     *
     * 重复调用将被 CAS 守卫静默跳过。
     *
     * @param application 应用实例
     */
    fun install(application: Application) = synchronized(lock) {
        if (!installed.compareAndSet(false, true)) {
            Log.d(TAG, "MorphInstaller.install 已执行过，跳过重复注册")
            return
        }

        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                // ── 阶段 1：在 AppCompat 安装之前注入 MorphFactory2 ──
                // minSdk=35，onActivityPreCreated 始终可用（API 29+）
                injectFactory2(activity)
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) {
                // ── 阶段 2：补充 AppCompat delegate 作为 originalFactory ──
                // AppCompat 在 onCreate() 中检查 getFactory2() != null 后跳过安装
                // 但其 delegate 仍可作为 originalFactory 供 MorphFactory2 降级使用
                patchAppCompatDelegate(activity)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
        registeredCallback = callback
        hostApplication = application
        application.registerActivityLifecycleCallbacks(callback)
    }

    /**
     * 阶段 1：在 AppCompat 之前注入 MorphFactory2。
     *
     * 通过反射直接设置 mFactory2 字段，不触发 mFactorySet 标志位，
     * 避免 AppCompat 后续安装时抛 IllegalStateException。
     */
    private fun injectFactory2(activity: Activity) {
        try {
            val inflater = activity.layoutInflater
            val currentFactory2 = inflater.factory2

            if (currentFactory2 is MorphFactory2) {
                return
            }

            // originalFactory 暂时为 null，将在 onActivityCreated 中补充
            val morphFactory = MorphFactory2(currentFactory2, MorphKit.finalThemeResId)
            setFactoryFields(inflater, morphFactory)

            Log.d(TAG, "Factory2 注入成功: ${activity.javaClass.simpleName}")
        } catch (e: Throwable) {
            Log.e(TAG, "Factory2 注入失败，放弃本次注入: ${activity.javaClass.simpleName}", e)
        }
    }

    /**
     * 阶段 2：补充 AppCompat delegate 作为 MorphFactory2 的 originalFactory。
     *
     * AppCompat 在 Activity.onCreate() 中检查 getFactory2() != null 后跳过了安装，
     * 但其 delegate 实现了 Factory2 接口，可以作为降级回退使用。
     *
     * 这样，MorphFactory2 在未命中替换规则时，可以委托给 AppCompat delegate
     * 创建 AppCompat 系控件，保留着色、矢量图、background tint 等能力。
     */
    private fun patchAppCompatDelegate(activity: Activity) {
        try {
            val inflater = activity.layoutInflater
            val factory2 = inflater.factory2

            if (factory2 !is MorphFactory2) return

            // 从 AppCompatActivity 获取 AppCompat delegate
            val appCompatDelegate = (activity as? AppCompatActivity)?.delegate
            if (appCompatDelegate is LayoutInflater.Factory2) {
                factory2.updateOriginalFactory(appCompatDelegate as LayoutInflater.Factory2)
                Log.d(TAG, "AppCompat delegate 已补充为 originalFactory: ${activity.javaClass.simpleName}")
            }
        } catch (e: Throwable) {
            Log.d(TAG, "补充 AppCompat delegate 失败，不影响 MorphKit 基础功能", e)
        }
    }

    private fun setFactoryFields(inflater: LayoutInflater, factory: MorphFactory2) {
        // 委托给 ReflectionHelper.safeSetFactory2，内部已实现完整的降级链路：
        // 1. 反射直接写入 mFactory2 + mFactory（最可靠）
        // 2. 重置 mFactorySet + 公开 API setFactory2（降级）
        if (!ReflectionHelper.safeSetFactory2(inflater, factory)) {
            Log.e(TAG, "所有 Factory2 注入策略均失败，MorphKit 无法生效")
        }
    }

    /** 重置安装状态（仅用于测试），注销已注册的生命周期回调 */
    @androidx.annotation.VisibleForTesting
    internal fun reset() = synchronized(lock) {
        registeredCallback?.let { cb ->
            hostApplication?.unregisterActivityLifecycleCallbacks(cb)
        }
        registeredCallback = null
        hostApplication = null
        installed.set(false)
    }
}
