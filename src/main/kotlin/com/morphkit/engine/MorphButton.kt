package com.morphkit.engine

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * MorphKit iOS 17 风格按钮。
 *
 * 基于 [AppCompatButton]，还原 iOS 17 按钮的视觉与交互特征：
 *
 * ## 视觉特征
 * - 无 Material Design 阴影与边框
 * - 统一 [MorphTheme.cornerMedium] 圆角（12dp）
 * - 两种预设样式：[Style.FILLED]（主操作）/ [Style.PLAIN]（次要操作）
 * - 禁用态自动切换为灰色背景 + 灰色文字
 *
 * ## 交互特征（灵魂触控反馈）
 * - **彻底移除 Android Ripple 涟漪**，改用 iOS 风格的「按压整体变暗」
 * - 按下时背景色叠加 20% 白色遮罩（浅色模式）或 20% 黑色遮罩（深色模式）
 * - 松开时遮罩平滑渐退，使用 [ValueAnimator] + FastOutSlowIn 插值器
 * - 禁用态不响应任何触摸动画
 *
 * ## 使用方式
 *
 * ```kotlin
 * // 通过 MorphKit DSL 注册
 * MorphKit.init(this) {
 *     groupReplace(listOf("Button", "AppCompatButton")) { ctx, attrs ->
 *         MorphButton(ctx, attrs).apply { style = MorphButton.Style.FILLED }
 *     }
 * }
 *
 * // 或直接在 XML 中使用
 * <com.morphkit.engine.MorphButton
 *     android:layout_width="match_parent"
 *     android:layout_height="48dp"
 *     android:text="确认" />
 * ```
 *
 * @see MorphTheme 颜色/形状/排版设计系统
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
     * | 样式 | 背景 | 文字 | iOS 对应 |
     * |------|------|------|----------|
     * | [FILLED] | tintColor | 白色 | 主操作按钮（蓝色实心） |
     * | [PLAIN] | 透明 | tintColor | 次要操作（文字按钮） |
     */
    enum class Style {

        /**
         * 填充样式 — iOS 主操作按钮。
         *
         * - 背景：[MorphTheme.tintColor]（iOS 系统蓝）
         * - 文字：纯白
         * - 按下态：叠加 20% 黑色遮罩（整体变暗）
         *
         * 对应 iOS `UIButton.Configuration.filled`。
         */
        FILLED,

        /**
         * 纯文字样式 — iOS 次要操作按钮。
         *
         * - 背景：透明
         * - 文字：[MorphTheme.tintColor]（iOS 系统蓝）
         * - 按下态：叠加 20% 白色遮罩（浅色模式）/ 20% 黑色遮罩（深色模式）
         *
         * 对应 iOS `UIButton.Configuration.plain`。
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

    /** FILLED 样式的原始背景色（缓存，避免每次重算） */
    private var filledBackgroundColor: Int = MorphTheme.tintColor(context)

    /** PLAIN 样式的原始背景色 */
    private var plainBackgroundColor: Int = Color.TRANSPARENT

    /** 禁用态背景色 */
    private var disabledBackgroundColor: Int = MorphTheme.secondarySystemBackground(context)

    /** FILLED 样式文字色 */
    private var filledTextColor: Int = Color.WHITE

    /** PLAIN 样式文字色 */
    private var plainTextColor: Int = MorphTheme.tintColor(context)

    /** 禁用态文字色 */
    private var disabledTextColor: Int = Color.GRAY

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 彻底移除 Material Design 的 Ripple 涟漪效果 ──
        // 核心策略：用自定义 GradientDrawable 替换整个 background，
        // 而非尝试在原有 RippleDrawable 上做修改。
        // RippleDrawable 的 rippleColor 即使设为透明，
        // 仍会在某些 ROM 上产生延迟的 1px 闪烁，
        // 唯一可靠方案是完全不使用 RippleDrawable。
        background = shapeDrawable

        // ── 移除默认阴影与边框 ──
        // AppCompatButton 默认带 elevation 与 stateListAnimator，
        // 这些是 Material Design 的阴影机制，必须显式清除
        elevation = 0f
        stateListAnimator = null

        // ── 应用排版 ──
        val typo = MorphTheme.typography.body
        textSize = typo.fontSize
        typeface = typo.weight.toTypeface()

        // ── 应用默认样式 ──
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 样式应用
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 根据当前 [style] 与 [isEnabled] 状态，应用完整的视觉配置。
     *
     * 包括背景色、文字色、遮罩重置。此方法在以下时机调用：
     * - [style] 被赋值时
     * - [setEnabled] 被调用时
     * - [onAttachedToWindow] 时（适配暗黑模式切换）
     */
    private fun applyStyle() {
        // 刷新暗黑模式相关颜色
        refreshColors()

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

    /**
     * 刷新与暗黑模式相关的颜色缓存。
     *
     * 在 Activity 重建或暗黑模式切换后调用，
     * 确保 tintColor / secondarySystemBackground 等语义色与当前模式一致。
     */
    private fun refreshColors() {
        filledBackgroundColor = MorphTheme.tintColor(context)
        disabledBackgroundColor = MorphTheme.secondarySystemBackground(context)
        plainTextColor = MorphTheme.tintColor(context)
        disabledTextColor = if (MorphTheme.isDarkMode(context)) DARK_DISABLED_TEXT else LIGHT_DISABLED_TEXT
    }

    /** 应用 FILLED 样式 */
    private fun applyFilledState() {
        shapeDrawable.setColor(filledBackgroundColor)
        setTextColor(filledTextColor)
    }

    /** 应用 PLAIN 样式 */
    private fun applyPlainState() {
        shapeDrawable.setColor(plainBackgroundColor)
        setTextColor(plainTextColor)
    }

    /** 应用禁用态 */
    private fun applyDisabledState() {
        shapeDrawable.setColor(disabledBackgroundColor)
        setTextColor(disabledTextColor)
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
     * 通过 [ValueAnimator] 驱动 [pressOverlayAlpha] 在 0↔1 之间平滑渐变，
     * 实现类似 iOS 的「按压整体变暗」效果。
     *
     * ### 为什么不使用 Android StateListAnimator / ColorStateList？
     *
     * - `ColorStateList` 只能实现颜色突变，无法做渐变动画
     * - `StateListAnimator` 只能驱动 elevation/alpha，无法叠加遮罩
     * - iOS 的反馈是**背景色平滑过渡**而非涟漪扩散，必须用 ValueAnimator 精确控制
     *
     * ### 遮罩策略
     *
     * | 样式 | 遮罩颜色 | 效果 |
     * |------|----------|------|
     * | FILLED | 黑色遮罩 | 按下时蓝色背景整体变暗 |
     * | PLAIN + 浅色模式 | 黑色遮罩 | 按下时出现浅灰底色 |
     * | PLAIN + 深色模式 | 白色遮罩 | 按下时出现深灰底色 |
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
     *
     * 动画参数：
     * - 持续时间：按下 150ms（快速响应），松开 200ms（柔和回弹）
     * - 插值器：[FastOutSlowInInterpolator]（Material 设计标准缓动，类似 iOS easeInOut）
     *
     * @param targetAlpha 目标遮罩透明度，按下时为 [PRESS_OVERLAY_MAX_ALPHA]，松开时为 0f
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
     * 根据按钮样式与暗黑模式选择遮罩颜色，通过 [blendColor] 混合
     * 原始背景色与遮罩色，实现 iOS 风格的「整体变暗/变亮」效果。
     *
     * **FILLED 样式**：原始背景为 tintColor（蓝色），叠加黑色遮罩 → 整体变暗
     * **PLAIN 样式**：原始背景为透明，叠加半透明遮罩 → 出现浅灰/深灰底色
     */
    private fun applyPressOverlay() {
        val baseColor = when (style) {
            Style.FILLED -> filledBackgroundColor
            Style.PLAIN -> plainBackgroundColor
        }

        val overlayColor = if (MorphTheme.isDarkMode(context)) {
            DARK_MODE_OVERLAY_COLOR
        } else {
            LIGHT_MODE_OVERLAY_COLOR
        }

        val blended = blendColor(baseColor, overlayColor, pressOverlayAlpha)
        shapeDrawable.setColor(blended)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 生命周期 — 适配暗黑模式切换
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 重新应用样式，适配 Activity 重建后的暗黑模式变化
        applyStyle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 清理动画器，避免 Activity 销毁后动画回调泄漏
        pressAnimator?.cancel()
        pressAnimator = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色混合工具
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 线性混合两个颜色。
     *
     * 在 ARGB 四通道上分别做线性插值，[ratio] 为 0 时返回 [from]，为 1 时返回 [to]。
     * 此方法不预乘 alpha，适用于遮罩叠加场景。
     *
     * @param from  基础色（ARGB）
     * @param to    遮罩色（ARGB）
     * @param ratio 混合比例 [0f, 1f]
     * @return 混合后的颜色
     */
    private fun blendColor(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio
        val r = Color.red(from) * inverseRatio + Color.red(to) * ratio
        val g = Color.green(from) * inverseRatio + Color.green(to) * ratio
        val b = Color.blue(from) * inverseRatio + Color.blue(to) * ratio
        val a = Color.alpha(from) * inverseRatio + Color.alpha(to) * ratio
        return Color.argb(a.toInt(), r.toInt(), g.toInt(), b.toInt())
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 常量
    // ═══════════════════════════════════════════════════════════════════════

    companion object {

        /** 按下时遮罩最大透明度（0.2 = 20% 遮罩），模拟 iOS 按压变暗 */
        private const val PRESS_OVERLAY_MAX_ALPHA = 0.2f

        /** 按下动画时长（ms）— 快速响应 */
        private const val PRESS_IN_DURATION = 150L

        /** 松开动画时长（ms）— 柔和回弹 */
        private const val PRESS_OUT_DURATION = 200L

        /**
         * 浅色模式遮罩色 — 黑色叠加。
         *
         * FILLED 按钮：蓝色背景 + 黑色遮罩 → 整体变暗
         * PLAIN 按钮：透明背景 + 黑色遮罩 → 出现浅灰底色
         */
        private val LIGHT_MODE_OVERLAY_COLOR = 0xFF000000L.toInt()

        /**
         * 深色模式遮罩色 — 白色叠加。
         *
         * 深色模式下背景已暗，叠加白色遮罩 → 变亮而非更暗，
         * 与 iOS 深色模式下的按钮行为一致。
         */
        private val DARK_MODE_OVERLAY_COLOR = 0xFFFFFFFFL.toInt()

        /** 浅色模式禁用态文字色 #C7C7CC */
        private val LIGHT_DISABLED_TEXT = 0xFFC7C7CC.toInt()

        /** 深色模式禁用态文字色 #48484A */
        private val DARK_DISABLED_TEXT = 0xFF48484A.toInt()
    }
}
