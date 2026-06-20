package com.morphkit.widget.text

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatEditText
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphShape
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens
import com.morphkit.theme.MorphTypography

/**
 * MorphKit iOS 极简风格输入框。
 *
 * 基于 [AppCompatEditText]，还原 iOS 输入框的视觉特征：
 *
 * ## 视觉特征
 * - **去除 Android 原生底线**：默认背景是透明，无下划线、无边框
 * - **搜索栏样式**：[Style.SEARCH] 模式下使用 [MorphTheme.morphColorSurfaceVariant]
 *   浅灰圆角背景，还原 iOS 搜索栏的极简观感
 * - **光标颜色**：跟随 [MorphTheme.morphColorPrimary]（iOS 系统蓝），与整体设计系统一致
 *
 * ## 交互特征
 * - 获取焦点时背景色透明度微调，提供 iOS 风格的「轻微变亮/变暗」反馈
 * - 失去焦点时恢复原始背景色
 * - 禁用态不响应焦点动画
 *
 * ## 使用方式
 *
 * ```kotlin
 * MorphKit.init(this) {
 *     groupReplace(listOf("EditText", "AppCompatEditText")) { ctx, attrs ->
 *         MorphEditText(ctx, attrs).apply { style = MorphEditText.Style.SEARCH }
 *     }
 * }
 * ```
 *
 * @see MorphTheme 颜色/形状/排版设计系统
 * @see Style 预设样式枚举
 */
class MorphEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphEditTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    /**
     * iOS 风格输入框预设样式。
     *
     * | 样式 | 背景 | 焦点反馈 | iOS 对应 |
     * |------|------|----------|----------|
     * | [BARE] | 透明 | 无 | 无背景输入框 |
     * | [SEARCH] | morphColorSurfaceVariant | 透明度微调 | UISearchBar 内部输入框 |
     */
    enum class Style {

        /**
         * 裸输入样式 — 无背景。
         *
         * - 背景：完全透明
         * - 焦点反馈：无
         * - 适用于自定义布局中的内嵌输入框
         */
        BARE,

        /**
         * 搜索栏样式 — iOS 极简搜索框。
         *
         * - 背景：[MorphTheme.morphColorSurfaceVariant] 浅灰圆角
         * - 焦点反馈：获取焦点时背景色轻微变化
         * - 无下划线、无边框
         *
         * 对应 iOS UISearchBar 内部的 UITextField。
         */
        SEARCH
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 交互模式，从 XML 属性 `morphInteractionMode` 读取。
     *
     * - [InteractionMode.IOS]（默认）：启用 iOS 搜索栏样式、自定义排版和焦点反馈
     * - [InteractionMode.MATERIAL]：跳过 iOS 特定处理，保持 M3 默认 EditText 外观
     */
    val interactionMode: InteractionMode

    /** 当前输入框样式 */
    var style: Style = Style.SEARCH
        set(value) {
            field = value
            applyStyle()
        }

    /** 搜索栏背景 Drawable（复用同一实例，在 init 中根据 morphCornerRadius 创建） */
    private lateinit var searchBackgroundDrawable: GradientDrawable

    /** 缓存：搜索栏原始背景色 */
    private var searchBackgroundColor: Int = MorphTheme.morphColorSurfaceVariant(context)

    /** 焦点态背景色（基于原始色微调透明度） */
    private var searchBackgroundFocusedColor: Int = 0

    /** 焦点过渡动画器（与其他交互组件统一使用动画过渡） */
    private var focusAnimator: ValueAnimator? = null

    /** 当前显示的背景色（用于动画起始值） */
    private var currentBgColor: Int = 0

    /** 是否已完成首次 attach（init 期间 View 未 attach，应跳过动画直接设色） */
    private var isAttached: Boolean = false

    /** 业务方是否在 XML 中显式设置了自定义背景（Step 6 — 极度克制）。
     *  提升为成员字段，使 applySearchState() 在生命周期回调中也能尊重宿主自定义背景。 */
    private var hasCustomBackground: Boolean = false

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 读取 XML 属性 ──
        var resolvedMode = InteractionMode.IOS
        var customCornerRadius = -1f
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.MorphEditText, defStyleAttr, 0)
            try {
                val variantValue = a.getInt(R.styleable.MorphEditText_morphEditTextVariant, 0)
                style = if (variantValue == 1) Style.BARE else Style.SEARCH

                val modeValue = a.getInt(R.styleable.MorphEditText_morphInteractionMode, 0)
                resolvedMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS

                // 读取 morphCornerRadius（未设置时为 -1，使用默认 cornerMedium）
                customCornerRadius = a.getDimensionPixelSize(
                    R.styleable.MorphEditText_morphCornerRadius, -1
                ).toFloat()
            } finally {
                a.recycle()
            }
        }
        interactionMode = resolvedMode

        // ── 初始化搜索栏背景 Drawable（根据 morphCornerRadius 或默认值） ──
        val cornerRadius = if (customCornerRadius >= 0) customCornerRadius
        else MorphShape.cornerMedium(context).toFloat()
        searchBackgroundDrawable = GradientDrawable().apply {
            setCornerRadius(cornerRadius)
        }

        if (interactionMode == InteractionMode.IOS) {
            // ── 检测业务方是否在 XML 中显式设置了自定义背景（Step 6 — 极度克制） ──
            hasCustomBackground = attrs?.getAttributeValue(
                "http://schemas.android.com/apk/res/android", "background"
            ) != null

            // ── 去除 Android 原生底线 ──
            // AppCompatEditText 默认带一条底部横线（EditText 底线），
            // iOS 输入框无此线条，必须彻底清除。
            // 但若业务方显式设置了 android:background，则尊重其自定义
            if (!hasCustomBackground) {
                background = null
            }

            // ── 应用排版 ──
            val typo = MorphTypography.body
            textSize = typo.fontSize
            typeface = typo.weight.toTypeface()

            // ── 提示文字颜色 ──
            setHintTextColor(MorphTheme.morphColorOnSurfaceVariant(context))

            // ── 默认文字颜色 ──
            setTextColor(MorphTheme.morphColorOnSurface(context))

            // ── 应用样式 ──
            applyStyle()
        }
        // MATERIAL mode: skip iOS-specific overrides, keep Android's default
        // EditText appearance with M3 theme (underline, ripple, default colors)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 样式应用
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前 [style] 应用完整的视觉配置。
     */
    private fun applyStyle() {
        refreshColors()

        when (style) {
            Style.BARE -> applyBareState()
            Style.SEARCH -> applySearchState(hasFocus())
        }
    }

    /**
     * 刷新与暗黑模式相关的颜色缓存。
     */
    private fun refreshColors() {
        searchBackgroundColor = MorphTheme.morphColorSurfaceVariant(context)

        // 焦点态背景色：在原始色基础上微调透明度
        // 浅色模式：略加深（叠加少量黑色，在浅灰背景上更明显）
        // 深色模式：略提亮（叠加少量白色）
        searchBackgroundFocusedColor = MorphColors.blendColor(
            searchBackgroundColor,
            if (MorphColors.isDarkMode(context)) Color.WHITE else Color.BLACK,
            MorphTokens.Interaction.focusOverlayAlpha
        )

        // 刷新提示文字颜色（暗黑模式切换后）
        setHintTextColor(MorphTheme.morphColorOnSurfaceVariant(context))
        setTextColor(MorphTheme.morphColorOnSurface(context))
    }

    /** 应用裸输入样式 */
    private fun applyBareState() {
        // 尊重宿主自定义背景：若业务方在 XML 显式设置了 android:background，
        // 不覆盖其背景（Step 6 — 极度克制）。守卫贯穿生命周期回调。
        if (hasCustomBackground) return
        background = null
    }

    /**
     * 应用搜索栏样式。
     *
     * @param focused 是否处于焦点态
     */
    private fun applySearchState(focused: Boolean) {
        // 尊重宿主自定义背景：若业务方在 XML 显式设置了 android:background，
        // 不覆盖其背景（Step 6 — 极度克制）。守卫贯穿生命周期回调。
        if (hasCustomBackground) return
        val targetColor = if (focused) searchBackgroundFocusedColor else searchBackgroundColor
        animateFocusColor(targetColor)
        background = searchBackgroundDrawable
    }

    /**
     * 动画过渡焦点背景色（150ms DecelerateInterpolator），
     * 与其他交互组件的动画过渡风格一致。
     *
     * L10 修复：init 期间 View 未 attach，启动 ValueAnimator 是无意义开销
     * （动画更新的是 drawable 颜色，但此时 drawable 尚未设为 background）。
     * 改为：未 attach 时直接 setColor，跳过动画。
     */
    private fun animateFocusColor(targetColor: Int) {
        focusAnimator?.cancel()

        // 未 attach 时直接设色，跳过动画（L10 修复）
        if (!isAttached) {
            currentBgColor = targetColor
            searchBackgroundDrawable.setColor(targetColor)
            return
        }

        val startColor = if (currentBgColor != 0) currentBgColor else searchBackgroundColor

        focusAnimator = ValueAnimator.ofArgb(startColor, targetColor).apply {
            duration = MorphTokens.Interaction.focusAnimationDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                val color = animator.animatedValue as Int
                currentBgColor = color
                searchBackgroundDrawable.setColor(color)
            }
            start()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 焦点反馈
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 焦点变化回调。
     *
     * 在 SEARCH 样式下，获取焦点时背景色轻微变化，提供 iOS 风格的微弱反馈：
     * - 获取焦点：背景色叠加少量白色遮罩 → 略微提亮
     * - 失去焦点：恢复原始背景色
     *
     * BARE 样式下不做任何视觉反馈。
     *
     * @param focused 当前是否获得焦点
     * @param direction 焦点来源方向
     * @param previouslyFocusedRect 前一个焦点 View 的矩形
     */
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (!isEnabled) return

        if (interactionMode == InteractionMode.IOS && style == Style.SEARCH) {
            applySearchState(focused)
        }
    }

    /**
     * 覆写 setEnabled：禁用态刷新视觉，避免焦点态背景永久停留。
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (interactionMode == InteractionMode.IOS) {
            applyStyle()
            // 禁用时降低透明度，与 MorphButton 行为一致
            alpha = if (enabled) 1f else MorphTokens.Interaction.disabledAlpha
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        if (interactionMode == InteractionMode.IOS) {
            applyStyle()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Activity 不重建时（configChanges 包含 uiMode），手动刷新颜色（仅 iOS 模式）
        if (interactionMode == InteractionMode.IOS) {
            applyStyle()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAttached = false
        focusAnimator?.cancel()
        focusAnimator = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试专用访问方法 — 避免 reflect 调用 protected 方法
    // ═══════════════════════════════════════════════════════════════════════

    /** 测试专用：分发焦点变化事件 */
    @VisibleForTesting
    internal fun dispatchFocusChangedForTest(focused: Boolean) {
        onFocusChanged(focused, View.FOCUS_FORWARD, null)
    }

    /** 测试专用：分发 onAttachedToWindow */
    @VisibleForTesting
    internal fun dispatchAttachedToWindowForTest() {
        onAttachedToWindow()
    }

    /** 测试专用：分发 onConfigurationChanged */
    @VisibleForTesting
    internal fun dispatchConfigChangedForTest(newConfig: Configuration) {
        onConfigurationChanged(newConfig)
    }

}
