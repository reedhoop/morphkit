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
 * - **系统级性能监控打点**：对每次控件替换进行耗时统计，超过阈值时输出性能警告。
 *
 * ## 基线保护声明
 *
 * 本 AAR 仅限于替换本 OEM 预装应用内的控件，不具备干涉第三方应用的能力。
 * 若需全局系统级换肤，请结合 Android RRO (Runtime Resource Overlay) 机制使用。
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
 *                          由 [MorphKit.init] / [MorphKit.autoInit] 通过 [MorphStyleResolver] 计算并缓存。
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

        /** 性能警告阈值：单次 View 替换耗时超过此值（毫秒）将输出警告 */
        private const val PERF_THRESHOLD_MS = 5L
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
     * │ 1. 硬替换阶段（含耗时统计）                         │
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
        val startTime = System.nanoTime()
        try {
            val replacedView = MorphKit.createView(name, themedContext, attrs)
            if (replacedView != null) {
                // ── 性能监控打点 ──
                checkPerformance(name, replacedView, startTime)

                // 硬替换成功：对替换控件执行后置属性修改
                try {
                    return MorphKit.modifyView(name, replacedView)
                } catch (e: Exception) {
                    Log.e(TAG, "硬替换成功但 modifyView 异常，返回未修改的替换控件: $name", e)
                    return replacedView
                }
            }
            // createView 返回 null → 未命中替换规则，进入降级阶段
        } catch (e: Exception) {
            // 硬替换过程中抛异常，立即降级到原始 Factory2
            Log.e(TAG, "硬替换异常，降级到原始 Factory2: $name", e)
        }

        // ═══════════════════════════════════════════════════
        // 阶段 2：降级 — 委托给原始 Factory2 创建原生控件
        // ═══════════════════════════════════════════════════
        val originalView = originalFactory?.onCreateView(parent, name, context, attrs)
            ?: return null

        // ═══════════════════════════════════════════════════
        // 阶段 3：软修改兜底
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
     */
    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 系统级性能监控打点
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 检查单次 View 替换耗时是否超过阈值。
     *
     * 如果耗时超过 [PERF_THRESHOLD_MS] 毫秒，输出性能警告日志，
     * 提醒 OEM 团队优化自定义控件的构造逻辑。
     *
     * 典型输出：
     * ```
     * W/MorphKit: 性能警告：View 替换耗时过长 MorphButton (6ms)
     * ```
     *
     * @param name      原始控件名称
     * @param view      替换后的 View 实例
     * @param startTime 替换开始时间（纳秒，[System.nanoTime]）
     */
    private fun checkPerformance(name: String, view: View, startTime: Long) {
        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000L
        if (elapsedMs > PERF_THRESHOLD_MS) {
            Log.w(TAG, "性能警告：View 替换耗时过长 ${view.javaClass.simpleName} (${elapsedMs}ms)")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Context 主题包装 — 核心技术点
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据条件对 Context 进行主题包装。
     *
     * 核心逻辑：
     * 1. 若 [finalThemeResId] 为 0，说明无需包装。
     * 2. 若宿主 Activity 的 Theme 已经包含了 `morphButtonStyle` 等 MorphKit 属性，
     *    说明宿主 App 已在自己的 Theme 中显式指定了 MorphKit 的样式，尊重宿主选择。
     * 3. 否则，使用 [ContextThemeWrapper] 将 [finalThemeResId] 叠加到 Context 上。
     *
     * @param context 原始上下文
     * @return 包装后的上下文，或原始上下文（若无需包装）
     */
    private fun wrapContextIfNeeded(context: Context): Context {
        if (finalThemeResId == 0) return context

        if (hostThemeHasMorphAttributes(context)) {
            return context
        }

        return ContextThemeWrapper(context, finalThemeResId)
    }

    /**
     * 检查宿主 Activity 的 Theme 是否已包含 MorphKit 的样式属性。
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
            Log.d(TAG, "检测宿主 Theme morphButtonStyle 属性异常，视为未设置", e)
            false
        }
    }
}
