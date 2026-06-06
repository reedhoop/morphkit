package com.morphkit.engine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * MorphKit iOS 风格按钮（M3 重构版）。
 *
 * 基于 [AppCompatButton]，完全使用 M3 语义色系统，同时保持 iOS 极简交互视觉：
 *
 * ## 视觉特征
 * - 无 Material Design 阴影与边框
 * - 统一圆角（默认 12dp）
 * - 两种预设样式：[Style.FILLED]（主操作）/ [Style.PLAIN]（次要操作）
 * - 禁用态自动切换为低透明度
 *
 * ## 交互特征（灵魂触控反馈）
 * - 彻底移除 Android Ripple 涟漪
 * - 改用 iOS 风格的「按压整体变暗」
 * - 按下时背景色叠加 20% 遮罩（深色模式叠加白色，浅色模式叠加黑色）
 * - 松开时遮罩平滑渐退，使用 [ValueAnimator] + FastOutSlowIn 插值器
 *
 * ## M3 语义色使用
 * - FILLED 样式：背景 = colorPrimary，文字 = colorOnPrimary
 * - PLAIN 样式：背景 = 透明，文字 = colorPrimary
 * - 禁用态：降低不透明度至 38%
 *
 * @see MorphTheme M3 颜色/形状/排版设计系统
 * @see Style 预设样式枚举
 */
class MorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.buttonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    /**
     * iOS 风格按钮预设样式。
     *
     * | 样式 | 背景 | 文字 | M3 语义属性 |
     * |------|------|------|-------------|
     * | [FILLED] | colorPrimary | colorOnPrimary | 主操作按钮 |
     * | [PLAIN] | 透明 | colorPrimary | 次要操作按钮 |
     */
    enum class Style {
        /**
         * 填充样式 — iOS 主操作按钮。
         *
         * - 背景：?attr/colorPrimary（M3 主色）
         * - 文字：?attr/colorOnPrimary（主色上的文字色）
         * - 按下态：叠加 20% 遮罩（整体变暗/变亮）
         */
        FILLED,

        /**
         * 纯文字样式 — iOS 次要操作按钮。
         *
         * - 背景：透明
         * - 文字：?attr/colorPrimary（M3 主色）
         * - 按下态：叠加 20% 遮罩
         */
        PLAIN
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /** 当前按钮样式 */
    var style: Style = Style.FILLED
        set(value) {
            field = value
            applyStyle()
        }

    /** 按下态遮罩动画器 */
    private var pressAnimator: ValueAnimator? = null

    /** 当前遮罩透明度 [0f, 1f]，0f=无遮罩，1f=满遮罩 */
    private var pressOverlayAlpha: Float = 0f

    /** 背景圆角 Drawable（复用同一实例，避免重复创建） */
    private val shapeDrawable = GradientDrawable().apply {
        cornerRadius = MorphTheme.cornerMedium.toFloat()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // 彻底移除 Material Design 的 Ripple 涟漪效果
        // 核心策略：用自定义 GradientDrawable 替换整个 background
        background = shapeDrawable

        // 移除默认阴影与边框
        elevation = 0f
        stateListAnimator = null

        // 应用排版
        val typo = MorphTheme.typography.body
        textSize = typo.fontSize
        typeface = typo.weight.toTypeface()

        // 应用默认样式
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 样式应用
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前 [style] 与 [isEnabled] 状态，应用完整的视觉配置。
     *
     * 使用 M3 语义色：
     * - FILLED：背景 = morphColorPrimary，文字 = morphColorOnPrimary
     * - PLAIN：背景 = 透明，文字 = morphColorPrimary
     * - 禁用态：降低不透明度至 38%
     */
    private fun applyStyle() {
        // 重置遮罩
        pressOverlayAlpha = 0f
        pressAnimator?.cancel()
        pressAnimator = null

        if (!isEnabled) {
            applyDisabledState()
            return
        }

        when (style) {
            Style.FILLED -> applyFilledState()
            Style.PLAIN -> applyPlainState()
        }
    }

    /** 应用 FILLED 样式 - 使用 M3 主色 */
    private fun applyFilledState() {
        val primaryColor: Int = MorphTheme.morphColorPrimary(context)
        val onPrimaryColor: Int = MorphTheme.morphColorOnPrimary(context)

        shapeDrawable.setColor(primaryColor)
        setTextColor(onPrimaryColor)
        alpha = 1f
    }

    /** 应用 PLAIN 样式 - 透明背景，主色文字 */
    private fun applyPlainState() {
        val primaryColor: Int = MorphTheme.morphColorPrimary(context)

        shapeDrawable.setColor(Color.TRANSPARENT)
        setTextColor(primaryColor)
        alpha = 1f
    }

    /** 应用禁用态 - 降低不透明度 */
    private fun applyDisabledState() {
        // 根据当前样式获取基础色
        val baseColor: Int = when (style) {
            Style.FILLED -> MorphTheme.morphColorPrimary(context)
            Style.PLAIN -> Color.TRANSPARENT
        }
        val baseTextColor: Int = when (style) {
            Style.FILLED -> MorphTheme.morphColorOnPrimary(context)
            Style.PLAIN -> MorphTheme.morphColorPrimary(context)
        }
        
        // 降低不透明度至 38%（Material Design 标准禁用态透明度）
        shapeDrawable.setColor(MorphTheme.adjustAlpha(baseColor, 0.38f))
        setTextColor(MorphTheme.adjustAlpha(baseTextColor, 0.38f))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 启用状态监听
    // ═══════════════════════════════════════════════════════════════════════

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 灵魂触控反馈 — iOS 按压变暗/变亮 + ValueAnimator 平滑渐变
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 触控事件处理。
     *
     * 核心逻辑：监听 [MotionEvent.ACTION_DOWN] / [ACTION_UP] / [ACTION_CANCEL]，
     * 通过 [ValueAnimator] 驱动 [pressOverlayAlpha] 在 0↔1 之间平滑渐变。
     *
     * 遮罩策略：
     * - 浅色模式：叠加黑色遮罩 → 整体变暗
     * - 深色模式：叠加白色遮罩 → 整体变亮
     * - 遮罩透明度：20%
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 禁用态不响应触摸动画，但仍由 super 处理点击事件
        if (!isEnabled) {
            return super.onTouchEvent(event)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 按下：启动遮罩渐入动画（0 → 1）
                animatePressOverlay(targetAlpha = PRESS_OVERLAY_MAX_ALPHA)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // 松开/取消：启动遮罩渐出动画（当前值 → 0）
                animatePressOverlay(targetAlpha = 0f)
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * 执行遮罩透明度渐变动画。
     *
     * 使用 [ValueAnimator] 从当前 [pressOverlayAlpha] 渐变到 [targetAlpha]，
     * 每帧调用 [applyPressOverlay] 实时更新背景色。
     */
    private fun animatePressOverlay(targetAlpha: Float) {
        // 取消上一段动画，从当前 alpha 值无缝衔接
        pressAnimator?.cancel()

        val duration = if (targetAlpha > 0f) PRESS_IN_DURATION else PRESS_OUT_DURATION

        pressAnimator = ValueAnimator.ofFloat(pressOverlayAlpha, targetAlpha).apply {
            this.duration = duration
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { animator ->
                pressOverlayAlpha = animator.animatedValue as Float
                applyPressOverlay()
            }
            start()
        }
    }

    /**
     * 将当前 [pressOverlayAlpha] 应用到背景色。
     *
     * 根据当前主题模式选择遮罩颜色：
     * - 浅色模式：黑色遮罩
     * - 深色模式：白色遮罩
     *
     * FILLED 样式：在主色上叠加遮罩
     * PLAIN 样式：在透明背景上叠加遮罩
     */
    private fun applyPressOverlay() {
        val baseColor: Int = when (style) {
            Style.FILLED -> MorphTheme.morphColorPrimary(context)
            Style.PLAIN -> Color.TRANSPARENT
        }

        val isDark = MorphTheme.isDarkMode(context)
        val overlayColor = if (isDark) Color.WHITE else Color.BLACK

        val blended: Int = MorphTheme.overlayColor(baseColor, overlayColor, pressOverlayAlpha)
        shapeDrawable.setColor(blended)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期 — 适配主题切换
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 重新应用样式，适配 Activity 重建后的主题/暗黑模式变化
        applyStyle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清理动画器，避免 Activity 销毁后动画回调泄漏
        pressAnimator?.cancel()
        pressAnimator = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 常量
    // ═══════════════════════════════════════════════════════════════════════

    companion object {
        /** 按下时遮罩最大透明度（0.2 = 20% 遮罩），模拟 iOS 按压效果 */
        private const val PRESS_OVERLAY_MAX_ALPHA = 0.2f

        /** 按下动画时长（ms）— 快速响应 */
        private const val PRESS_IN_DURATION = 150L

        /** 松开动画时长（ms）— 柔和回弹 */
        private const val PRESS_OUT_DURATION = 200L
    }
}
