package com.morphkit.engine

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import com.morphkit.R

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
 * - **无侵入式皮肤注入**：通过 [ContextThemeWrapper] 将 MorphKit 解析出的 Theme
 *   注入到控件创建的 Context 中，即使宿主 Activity 未设置 MorphKit 主题，
 *   控件仍能读取到正确的 iOS 或 Pixel 风格属性。
 *
 * ## Context 主题包装机制
 *
 * ```
 * MorphFactory2.onCreateView(name, context, attrs)
 *   └─ 1. 检查宿主 Theme 是否已包含 morphButtonStyle 等属性
 *        ├─ 已包含 → 尊重宿主选择，不包装 Context
 *        └─ 未包含 → 用 ContextThemeWrapper(context, finalThemeResId) 包装
 *   └─ 2. 将包装后的 Context 传递给 MorphKit.createView
 *        └─ 控件构造函数中的 obtainStyledAttributes 可正确读取 MorphKit 主题属性
 * ```
 *
 * @param originalFactory   被代理的原始 Factory2，通常为 AppCompatDelegateImpl 内置的 Factory2。
 *                          允许为 null（非 AppCompat 场景），此时降级路径将返回 null 交由系统处理。
 * @param finalThemeResId   MorphKit 解析出的最终 Theme 资源 ID，
 *                          由 [MorphKit.init] 通过 [MorphStyleResolver] 计算并缓存。
 *                          为 0 表示不进行 Context 主题包装。
 *
 * @constructor 创建 MorphFactory2 实例
 */
class MorphFactory2(
    private val originalFactory: LayoutInflater.Factory2?,
    private val finalThemeResId: Int = 0
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
     * │ 0. Context 主题包装                               │
     * │    若宿主未设置 MorphKit 主题属性，                  │
     * │    则用 ContextThemeWrapper 注入 finalThemeResId  │
     * ├──────────────────────────────────────────────────┤
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
        // 阶段 0：Context 主题包装 — 无侵入式皮肤注入
        // ═══════════════════════════════════════════════════
        val themedContext = wrapContextIfNeeded(context)

        // ═══════════════════════════════════════════════════
        // 阶段 1：硬替换 — 尝试用 MorphKit 规则替换原始控件
        // ═══════════════════════════════════════════════════
        try {
            val replacedView = MorphKit.createView(name, themedContext, attrs)
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

    // ═══════════════════════════════════════════════════════════════════════
    // Context 主题包装 — 核心技术点
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据条件对 Context 进行主题包装。
     *
     * 核心逻辑：
     * 1. 若 [finalThemeResId] 为 0，说明无需包装（策略未激活或宿主已完全接管）。
     * 2. 若宿主 Activity 的 Theme 已经包含了 `morphButtonStyle` 等 MorphKit 属性，
     *    说明宿主 App 已在自己的 Theme 中显式指定了 MorphKit 的样式，尊重宿主选择。
     * 3. 否则，使用 [ContextThemeWrapper] 将 [finalThemeResId] 叠加到 Context 上，
     *    确保控件构造函数中的 `obtainStyledAttributes` 可正确读取 MorphKit 主题属性。
     *
     * ## 为什么需要这一步？
     *
     * MorphKit 控件（如 [MorphButton]）的 `defStyleAttr` 为 `R.attr.morphButtonStyle`，
     * 这个属性定义在 `Theme.MorphKit.iOS` / `Theme.MorphKit.Pixel` 中。
     * 如果宿主 Activity 的 Theme 没有继承 MorphKit 的 Theme，
     * `obtainStyledAttributes` 将找不到 `morphButtonStyle` 的值，导致控件回退到默认样式。
     *
     * 通过 [ContextThemeWrapper] 注入，MorphKit 可以在**不修改宿主 Activity Theme** 的前提下，
     * 让控件正确读取到 iOS 或 Pixel 风格属性，实现「无侵入式皮肤注入」。
     *
     * @param context 原始上下文
     * @return 包装后的上下文，或原始上下文（若无需包装）
     */
    private fun wrapContextIfNeeded(context: Context): Context {
        // finalThemeResId 为 0 → 无需包装
        if (finalThemeResId == 0) return context

        // 检查宿主 Theme 是否已包含 MorphKit 属性
        if (hostThemeHasMorphAttributes(context)) {
            return context
        }

        // 宿主未设置 MorphKit 主题属性 → 用 ContextThemeWrapper 注入
        return ContextThemeWrapper(context, finalThemeResId)
    }

    /**
     * 检查宿主 Activity 的 Theme 是否已包含 MorphKit 的样式属性。
     *
     * 通过尝试从 Context 的 Theme 中解析 `morphButtonStyle` 属性来判断：
     * - 若解析成功（返回有效资源 ID），说明宿主 Theme 已声明了 MorphKit 属性，
     *   可能是宿主 App 继承了 `Theme.MorphKit.iOS` / `Pixel`，
     *   或在自己的 Theme 中显式指定了 `morphButtonStyle`，此时尊重宿主选择。
     * - 若解析失败（返回 0 或抛异常），说明宿主 Theme 未涉及 MorphKit，
     *   需要由 MorphKit 通过 [ContextThemeWrapper] 注入主题。
     *
     * @param context 上下文
     * @return true 表示宿主 Theme 已包含 MorphKit 属性，无需强制注入
     */
    private fun hostThemeHasMorphAttributes(context: Context): Boolean {
        return try {
            val a = context.theme.obtainStyledAttributes(intArrayOf(R.attr.morphButtonStyle))
            val hasMorphAttr = a.hasValue(0)
            a.recycle()
            hasMorphAttr
        } catch (e: Exception) {
            // 解析异常，保守起见认为宿主未设置 MorphKit 属性
            Log.d(TAG, "检测宿主 Theme morphButtonStyle 属性异常，视为未设置", e)
            false
        }
    }
}
