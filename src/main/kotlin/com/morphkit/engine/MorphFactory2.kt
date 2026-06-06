package com.morphkit.engine

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View

/**
 * MorphKit 的 LayoutInflater.Factory2 代理拦截器。
 *
 * 采用装饰器模式包装 [originalFactory]（通常是 AppCompatDelegateImpl 内置的 Factory2），
 * 在控件膨胀阶段插入 MorphKit 的替换与修改逻辑，同时保证：
 *
 * - **硬替换失败时无缝降级**：creator 抛异常或返回 null → 立即委托给 [originalFactory]，
 *   绝不让用户看到白屏。
 * - **AppCompat 保留**：[originalFactory] 负责创建 AppCompat 系控件（如 AppCompatTextView），
 *   本类仅做拦截/透传，不破坏 AppCompat 的兼容性处理。
 * - **软修改兜底**：即使硬替换未命中，对原生控件仍可执行 modify 规则统一色调/字体。
 *
 * ## 集成方式
 * 在 `Activity.onCreate` 的 `super.onCreate()` 之前安装：
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     val originalFactory = layoutFactory  // 获取 AppCompat 已设置的 Factory2
 *     LayoutInflater.from(this).factory2 = MorphFactory2(originalFactory)
 *     super.onCreate(savedInstanceState)
 * }
 * ```
 *
 * @param originalFactory 被代理的原始 Factory2，通常为 AppCompatDelegateImpl 内置的 Factory2。
 *                        允许为 null（非 AppCompat 场景），此时降级路径将返回 null 交由系统处理。
 *
 * @constructor 创建 MorphFactory2 实例
 */
class MorphFactory2(
    private val originalFactory: LayoutInflater.Factory2?
) : LayoutInflater.Factory2 {

    companion object {
        private const val TAG = "MorphKit"
    }

    /**
     * 带 parent 参数的控件创建回调（Factory2 核心方法）。
     *
     * 执行顺序如下，任一环节异常均安全降级，绝不白屏：
     *
     * ```
     * ┌──────────────────────────────────────────────────┐
     * │ 1. 硬替换阶段                                     │
     * │    MorphKit.createView(name, context, attrs)     │
     * │    ├─ 返回非 null → 应用 modify → 返回替换控件    │
     * │    └─ 返回 null / 抛异常 → 进入降级阶段 ↓        │
     * ├──────────────────────────────────────────────────┤
     * │ 2. 降级阶段                                       │
     * │    originalFactory?.onCreateView(...)             │
     * │    ├─ 返回原生 View → 尝试软修改兜底               │
     * │    └─ 返回 null → 交由系统处理                    │
     * ├──────────────────────────────────────────────────┤
     * │ 3. 软修改兜底                                     │
     * │    MorphKit.modifyView(name, view)               │
     * │    └─ 若 modifier 抛异常 → 静默捕获，返回原 View  │
     * └──────────────────────────────────────────────────┘
     * ```
     *
     * @param parent  父控件，由 LayoutInflater 传入
     * @param name    正在膨胀的控件标签名（简名或全限定名）
     * @param context 上下文
     * @param attrs   XML 属性集
     * @return 创建/替换后的 View，或 null 交由系统默认处理
     */
    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        // ═══════════════════════════════════════════════════
        // 阶段 1：硬替换 — 尝试用 MorphKit 规则替换原始控件
        // ═══════════════════════════════════════════════════
        try {
            val replacedView = MorphKit.createView(name, context, attrs)
            if (replacedView != null) {
                // 硬替换成功：对替换控件执行后置属性修改
                // 若 modifyView 抛异常，意味着替换控件虽已创建但属性修改失败，
                // 此时替换控件本身可用，仅丢失属性修改，仍返回替换控件
                try {
                    return MorphKit.modifyView(name, replacedView)
                } catch (e: Exception) {
                    Log.e(TAG, "硬替换成功但 modifyView 异常，返回未修改的替换控件: $name", e)
                    return replacedView
                }
            }
            // createView 返回 null → 未命中替换规则，进入降级阶段
        } catch (e: Exception) {
            // 硬替换过程中抛异常（creator 内部错误、ClassNotFound 等），
            // 立即降级到原始 Factory2 创建原生控件，绝不白屏
            Log.e(TAG, "硬替换异常，降级到原始 Factory2: $name", e)
        }

        // ═══════════════════════════════════════════════════
        // 阶段 2：降级 — 委托给原始 Factory2 创建原生控件
        // 保留 AppCompat 兼容性（如 AppCompatTextView 等系统控件的创建）
        // ═══════════════════════════════════════════════════
        val originalView = originalFactory?.onCreateView(parent, name, context, attrs)
            // originalFactory 为 null 或其返回 null → 交由系统 LayoutInflater 默认处理
            ?: return null

        // ═══════════════════════════════════════════════════
        // 阶段 3：软修改兜底 — 对原生控件尝试后置属性修改
        // 即使硬替换未命中，仍可通过 modify 规则统一色调/字体等
        // 若 modifier 抛异常，静默捕获，不影响原生控件正常显示
        // ═══════════════════════════════════════════════════
        try {
            return MorphKit.modifyView(name, originalView)
        } catch (e: Exception) {
            Log.e(TAG, "软修改异常，静默忽略，返回原生控件: $name", e)
            return originalView
        }
    }

    /**
     * 不带 parent 参数的控件创建回调（Factory 接口方法）。
     *
     * 内部直接委托给带 parent 参数的重载版本，传入 `parent = null`。
     * 这保证了无论 LayoutInflater 以哪个重载调用，逻辑一致且不遗漏拦截。
     *
     * @param name    正在膨胀的控件标签名
     * @param context 上下文
     * @param attrs   XML 属性集
     * @return 创建/替换后的 View，或 null 交由系统默认处理
     */
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }
}
