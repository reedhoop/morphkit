package com.morphkit.engine

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.morphkit.R

/**
 * MorphKit 多风格按钮 — 引擎与皮肤分离架构核心示例。
 *
 * 本控件通过 [R.attr.morphButtonStyle] 接管主题链路，根据 Theme 中配置的
 * [R.attr.morphInteractionMode] 动态分发交互逻辑：
 *
 * ## 交互模式
 *
 * | 模式 | 触控反馈 | 视觉特征 | 皮肤 |
 * |------|---------|---------|------|
 * | [InteractionMode.IOS] | 按压整体变色，无涟漪 | 圆角 12dp，零阴影 | Widget.MorphKit.Button.iOS |
 * | [InteractionMode.MATERIAL] | 保留系统 Ripple 涟漪 | 圆角 8dp，M3 标准 | Widget.MorphKit.Button.Pixel |
 *
 * ## 主题链路
 *
 * ```
 * Theme.MorphKit.iOS / .Pixel
 *   └─ morphButtonStyle → Widget.MorphKit.Button.iOS / .Pixel
 *        └─ morphInteractionMode → ios / material
 *        └─ morphCornerRadius → 12dp / 8dp
 * ```
 *
 * 控件在 `init` 中通过 `obtainStyledAttributes` 读取这些属性，
 * 完全听从 Theme 指挥，自身不硬编码任何视觉参数。
 *
 * @see InteractionMode 交互模式枚举
 * @see MorphTheme M3 颜色/形状/排版设计系统
 */
class MorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphButtonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    /**
     * 交互模式枚举 — 决定触控反馈行为。
     *
     * 由 [R.attr.morphInteractionMode] 驱动，控件自身不硬编码。
     */
    enum class InteractionMode {
        /** iOS 风格：按压整体变色，无涟漪水波纹 */
        IOS,
        /** Material 风格：保留系统 Ripple 涟漪水波纹 */
        MATERIAL
    }

    /**
     * 按钮视觉样式。
     *
     * | 样式 | 背景 | 文字 | 用途 |
     * |------|------|------|------|
     * | [FILLED] | colorPrimary | colorOnPrimary | 主操作按钮 |
     * | [PLAIN] | 透明 | colorPrimary | 次要操作按钮 |
     */
    enum class Style { FILLED, PLAIN }

    // ═══════════════════════════════════════════════════════════════════════
    // 从 Theme 读取的配置
    // ═══════════════════════════════════════════════════════════════════════

    /** 交互模式 — 由 Theme 中的 morphInteractionMode 决定 */
    private val interactionMode: InteractionMode

    /** 圆角半径 — 由 Theme 中的 morphCornerRadius 决定 */
    private val cornerRadius: Float

    // ═══════════════════════════════════════════════════════════════════════
    // 内部状态
    // ═══════════════════════════════════════════════════════════════════════

    /** 当前按钮样式 */
    var style: Style = Style.FILLED
        set(value) {
            field = value
            applyStyle()
        }

    /** iOS 模式：按下态遮罩动画器 */
    private var pressAnimator: ValueAnimator? = null

    /** iOS 模式：当前遮罩透明度 [0f, 1f] */
    private var pressOverlayAlpha: Float = 0f

    /** iOS 模式：背景圆角 Drawable */
    private val shapeDrawable = GradientDrawable()

    // ═══════════════════════════════════════════════════════════════════════
    // 初始化
    // ═══════════════════════════════════════════════════════════════════════

    init {
        // ── 从 Theme 读取交互模式与圆角 ──
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.MorphButton, defStyleAttr, 0
        )
        val modeValue = a.getInt(R.styleable.MorphButton_morphInteractionMode, 0)
        interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
        cornerRadius = a.getDimension(R.styleable.MorphButton_morphCornerRadius,
            MorphTheme.cornerMedium.toFloat()
        )
        a.recycle()

        // ── 应用排版 ──
        val typo = MorphTheme.typography.body
        textSize = typo.fontSize
        typeface = typo.weight.toTypeface()

        // ── 根据交互模式分发初始化 ──
        when (interactionMode) {
            InteractionMode.IOS -> initIosMode()
            InteractionMode.MATERIAL -> initMaterialMode()
        }

        // ── 应用默认样式 ──
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // iOS 模式初始化
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * iOS 模式初始化：移除 Ripple，设置自定义背景。
     *
     * 核心策略：
     * 1. 用 GradientDrawable 替换整个 background，彻底消灭 Ripple
     * 2. 移除阴影与 stateListAnimator
     * 3. 在 [onTouchEvent] 中通过 ValueAnimator 驱动按压遮罩
     */
    private fun initIosMode() {
        shapeDrawable.cornerRadius = cornerRadius
        background = shapeDrawable
        elevation = 0f
        stateListAnimator = null
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Material 模式初始化
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Material 模式初始化：保留系统 Ripple。
     *
     * 不干预 background 与 foreground，让 M3 的 RippleDrawable 正常工作。
     * 仅设置圆角裁剪。
     */
    private fun initMaterialMode() {
        // Material 模式下不替换 background，保留 M3 默认的 RippleDrawable
        // 仅确保圆角一致
        shapeDrawable.cornerRadius = cornerRadius
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 样式应用
    // ═══════════════════════════════════════════════════════════════════════

    private fun applyStyle() {
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

    private fun applyFilledState() {
        val primaryColor: Int = MorphTheme.morphColorPrimary(context)
        val onPrimaryColor: Int = MorphTheme.morphColorOnPrimary(context)

        when (interactionMode) {
            InteractionMode.IOS -> {
                shapeDrawable.setColor(primaryColor)
            }
            InteractionMode.MATERIAL -> {
                // Material 模式使用 backgroundTint 而非直接设置 background
                backgroundTintList = ColorStateList.valueOf(primaryColor)
            }
        }
        setTextColor(onPrimaryColor)
        alpha = 1f
    }

    private fun applyPlainState() {
        val primaryColor: Int = MorphTheme.morphColorPrimary(context)

        when (interactionMode) {
            InteractionMode.IOS -> {
                shapeDrawable.setColor(Color.TRANSPARENT)
            }
            InteractionMode.MATERIAL -> {
                backgroundTintList = null
                background = null
            }
        }
        setTextColor(primaryColor)
        alpha = 1f
    }

    private fun applyDisabledState() {
        val baseColor: Int = when (style) {
            Style.FILLED -> MorphTheme.morphColorPrimary(context)
            Style.PLAIN -> Color.TRANSPARENT
        }
        val baseTextColor: Int = when (style) {
            Style.FILLED -> MorphTheme.morphColorOnPrimary(context)
            Style.PLAIN -> MorphTheme.morphColorPrimary(context)
        }

        when (interactionMode) {
            InteractionMode.IOS -> {
                shapeDrawable.setColor(MorphTheme.adjustAlpha(baseColor, 0.38f))
            }
            InteractionMode.MATERIAL -> {
                backgroundTintList = ColorStateList.valueOf(MorphTheme.adjustAlpha(baseColor, 0.38f))
            }
        }
        setTextColor(MorphTheme.adjustAlpha(baseTextColor, 0.38f))
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 触控事件 — 交互模式分发
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 触控事件分发。
     *
     * - [InteractionMode.IOS]：拦截触控事件，通过 [ValueAnimator] 驱动按压遮罩
     * - [InteractionMode.MATERIAL]：不干预，让系统 Ripple 正常响应
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactionMode == InteractionMode.IOS && isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    animatePressOverlay(targetAlpha = PRESS_OVERLAY_MAX_ALPHA)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    animatePressOverlay(targetAlpha = 0f)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // iOS 按压遮罩动画
    // ═══════════════════════════════════════════════════════════════════════

    private fun animatePressOverlay(targetAlpha: Float) {
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
    // 生命周期
    // ═══════════════════════════════════════════════════════════════════════

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyStyle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pressAnimator?.cancel()
        pressAnimator = null
    }

    companion object {
        private const val PRESS_OVERLAY_MAX_ALPHA = 0.2f
        private const val PRESS_IN_DURATION = 150L
        private const val PRESS_OUT_DURATION = 200L
    }
}
