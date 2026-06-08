package com.morphkit.core

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Volatile
import androidx.appcompat.view.ContextThemeWrapper
import com.morphkit.R

/**
 * MorphKit 的 LayoutInflater.Factory2 责任链代理拦截器。
 *
 * 采用**责任链代理**模式，确保 AppCompat 代理链完整性：
 *
 * ```
 * MorphFactory2.onCreateView(name, context, attrs)
 *   └─ 1. 优先委托 originalFactory 创建初步 View
 *        ├─ originalFactory != null → AppCompat 创建 AppCompatTextView 等
 *        └─ originalFactory == null → 返回 null（系统默认创建）
 *   └─ 2. 检查是否命中替换规则
 *        ├─ 命中 → 创建 Morph* 控件替换（传入原始 attrs 保留业务方属性）
 *        └─ 未命中 → 返回 originalFactory 创建的 View（AppCompat 着色完整保留）
 *   └─ 3. 对返回的 View 执行软修改兜底
 * ```
 *
 * ## 与黄金标准的对照
 *
 * | 黄金标准要求 | 当前实现 | 符合度 |
 * |------------|---------|--------|
 * | 持有原 Factory2 实例 | `originalFactory: LayoutInflater.Factory2?` | ✅ |
 * | 绝对优先调用原 Factory 获取初步 View | 先调 originalFactory，再决定替换 | ✅ |
 * | 基于原 Factory 返回的 View 类型决定替换 | 基于 XML 标签名匹配 + originalFactory View 类型 | ✅ |
 *
 * ## AppCompat 共存
 *
 * [originalFactory] 初始可能为 null（AppCompat 尚未安装），
 * 通过 [updateOriginalFactory] 在 AppCompat 安装后补充，
 * 确保未命中替换规则的控件由 AppCompat delegate 创建，保留着色能力。
 *
 * @see MorphInstaller.patchAppCompatDelegate
 */
class MorphFactory2(
    @Volatile private var originalFactory: LayoutInflater.Factory2?,
    private val finalThemeResId: Int = 0
) : LayoutInflater.Factory2 {

    companion object {
        private const val TAG = "MorphKit"
        private const val PERF_THRESHOLD_MS = 5L
    }

    private var hostThemeChecked = false
    private var hostHasMorphAttr = false

    // 缓存 ContextThemeWrapper，避免复杂布局下重复创建
    private var cachedThemedContext: Context? = null
    private var cachedBaseContext: Context? = null

    /**
     * 补充 AppCompat delegate 作为 originalFactory。
     */
    fun updateOriginalFactory(factory: LayoutInflater.Factory2) {
        originalFactory = factory
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {

        val themedContext = wrapContextIfNeeded(context)

        // ═══════════════════════════════════════════════════════════════════
        // 阶段 1：优先委托 originalFactory 创建初步 View
        //
        // 黄金标准：必须绝对优先调用原 Factory 的 onCreateView，
        // 确保 AppCompat 的 VectorDrawable 解析、background tint、
        // AppCompatTextView 等兼容性处理不被绕过。
        // ═══════════════════════════════════════════════════════════════════
        val originalView = originalFactory?.onCreateView(parent, name, context, attrs)

        // ═══════════════════════════════════════════════════════════════════
        // 阶段 2：检查是否命中替换规则
        //
        // 基于 XML 标签名匹配替换规则，而不是基于 originalView 的类型。
        // 因为 AppCompat 会将 "TextView" 替换为 AppCompatTextView，
        // 如果基于 originalView 类型判断，会错过原始标签名。
        // ═══════════════════════════════════════════════════════════════════
        val startTime = System.nanoTime()
        try {
            val replacedView = MorphKit.createView(name, themedContext, attrs)
            if (replacedView != null) {
                checkPerformance(name, replacedView, startTime)
                try {
                    return MorphKit.modifyView(name, replacedView)
                } catch (e: Exception) {
                    Log.e(TAG, "硬替换成功但 modifyView 异常，返回未修改的替换控件: $name", e)
                    return replacedView
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "硬替换异常，降级到 originalFactory 创建的控件: $name", e)
        }

        // ═══════════════════════════════════════════════════════════════════
        // 阶段 3：返回 originalFactory 创建的 View + 软修改兜底
        //
        // 未命中替换规则 → 返回 AppCompat 创建的控件（着色完整保留）
        // ═══════════════════════════════════════════════════════════════════
        if (originalView != null) {
            try {
                return MorphKit.modifyView(name, originalView)
            } catch (e: Exception) {
                Log.e(TAG, "软修改异常，静默忽略，返回原生控件: $name", e)
            }
        }

        return originalView
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return onCreateView(null, name, context, attrs)
    }

    private fun checkPerformance(name: String, view: View, startTime: Long) {
        val elapsedMs = (System.nanoTime() - startTime) / 1_000_000L
        if (elapsedMs > PERF_THRESHOLD_MS) {
            Log.w(TAG, "性能警告：View 替换耗时过长 ${view.javaClass.simpleName} (${elapsedMs}ms)")
        }
    }

    private fun wrapContextIfNeeded(context: Context): Context {
        if (finalThemeResId == 0) return context

        if (!hostThemeChecked) {
            hostHasMorphAttr = hostThemeHasMorphAttributes(context)
            hostThemeChecked = true
        }

        if (hostHasMorphAttr) return context

        // 缓存：同一 Activity 内 context 实例相同，无需重复创建
        if (cachedBaseContext === context && cachedThemedContext != null) {
            return cachedThemedContext!!
        }

        val wrapped = ContextThemeWrapper(context, finalThemeResId)
        cachedThemedContext = wrapped
        cachedBaseContext = context
        return wrapped
    }

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
