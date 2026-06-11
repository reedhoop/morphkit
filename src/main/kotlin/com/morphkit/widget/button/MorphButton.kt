package com.morphkit.widget.button

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens

/**
 * MorphKit 多风格按钮 — 引擎与皮肤分离架构核心示例。
 *
 * @see InteractionMode 交互模式枚举
 * @see MorphTheme M3 颜色/形状/排版设计系统
 */
class MorphButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphButtonStyle
) : AppCompatButton(context, attrs, defStyleAttr) {

    enum class Style { FILLED, PLAIN }

    private val interactionMode: InteractionMode
    private val cornerRadius: Float

    var style: Style = Style.FILLED
        set(value) {
            field = value
            applyStyle()
        }

    private var pressAnimator: ValueAnimator? = null
    private var pressOverlayAlpha: Float = 0f
    private val shapeDrawable = GradientDrawable()

    // ── 缓存颜色 ──
    private var cachedPrimaryColor: Int = 0
    private var cachedOnPrimaryColor: Int = 0

    // ── 标记业务方是否设置了自定义背景 ──
    private var hasCustomBackground: Boolean = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MorphButton, defStyleAttr, 0)
        try {
            val modeValue = a.getInt(R.styleable.MorphButton_morphInteractionMode, 0)
            interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
            cornerRadius = a.getDimension(
                R.styleable.MorphButton_morphCornerRadius,
                MorphTheme.cornerMedium(context).toFloat()
            )
            // 从 XML 读取样式变体
            val variantValue = a.getInt(R.styleable.MorphButton_morphButtonVariant, 0)
            style = if (variantValue == 1) Style.PLAIN else Style.FILLED
        } finally {
            a.recycle()
        }

        // ── 检测业务方是否设置了自定义背景 ──
        // 在 super 构造函数执行后，background 已被 XML 属性设置
        // 如果 background 不是默认的（非 null 且非 ColorDrawable），说明业务方设置了自定义背景
        hasCustomBackground = background != null && background !is android.graphics.drawable.ColorDrawable

        cachedPrimaryColor = MorphTheme.morphColorPrimary(context)
        cachedOnPrimaryColor = MorphTheme.morphColorOnPrimary(context)

        val typo = MorphTheme.typography.body
        textSize = typo.fontSize
        typeface = typo.weight.toTypeface()

        when (interactionMode) {
            InteractionMode.IOS -> initIosMode()
            InteractionMode.MATERIAL -> initMaterialMode()
        }

        applyStyle()
    }

    private fun initIosMode() {
        shapeDrawable.cornerRadius = cornerRadius

        // ── 仅在业务方未设置自定义背景时才替换 ──
        if (!hasCustomBackground) {
            background = shapeDrawable
        }

        // ── 无障碍合规：使用 StateListAnimator 分离按压反馈与焦点反馈 ──
        // 黄金标准：按压反馈与焦点反馈必须分离
        // - state_pressed → Alpha 变暗（iOS 按压变色效果）
        // - state_focused → translationZ 抬起（键盘焦点指示器，A11y 合规）
        // - 默认态 → 恢复
        stateListAnimator = android.animation.AnimatorInflater.loadStateListAnimator(
            context,
            R.animator.morph_widget_button_ios_state
        )
    }

    private fun initMaterialMode() {
        shapeDrawable.cornerRadius = cornerRadius
        // Material 模式保留 M3 默认的 StateListAnimator（含 Ripple + 焦点抬升）
    }

    private fun applyStyle() {
        pressOverlayAlpha = 0f
        pressAnimator?.cancel()
        pressAnimator = null

        cachedPrimaryColor = MorphTheme.morphColorPrimary(context)
        cachedOnPrimaryColor = MorphTheme.morphColorOnPrimary(context)

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
        when (interactionMode) {
            InteractionMode.IOS -> {
                if (!hasCustomBackground) {
                    shapeDrawable.setColor(cachedPrimaryColor)
                }
            }
            InteractionMode.MATERIAL -> {
                // 不手动设置 backgroundTintList，让 M3 Style 自行处理
            }
        }
        setTextColor(cachedOnPrimaryColor)
        alpha = 1f
    }

    private fun applyPlainState() {
        when (interactionMode) {
            InteractionMode.IOS -> {
                if (!hasCustomBackground) {
                    shapeDrawable.setColor(Color.TRANSPARENT)
                }
            }
            InteractionMode.MATERIAL -> {
                // 不干预 M3 默认行为
            }
        }
        setTextColor(cachedPrimaryColor)
        alpha = 1f
    }

    private fun applyDisabledState() {
        val baseColor: Int = when (style) {
            Style.FILLED -> cachedPrimaryColor
            Style.PLAIN -> Color.TRANSPARENT
        }
        val baseTextColor: Int = when (style) {
            Style.FILLED -> cachedOnPrimaryColor
            Style.PLAIN -> cachedPrimaryColor
        }

        when (interactionMode) {
            InteractionMode.IOS -> {
                if (!hasCustomBackground) {
                    shapeDrawable.setColor(MorphTheme.adjustAlpha(baseColor, MorphTokens.disabledAlpha))
                }
            }
            InteractionMode.MATERIAL -> {
                // 不干预 M3 默认禁用态
            }
        }
        setTextColor(MorphTheme.adjustAlpha(baseTextColor, MorphTokens.disabledAlpha))
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        applyStyle()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 触控事件 — iOS 模式按压变色
    // ═══════════════════════════════════════════════════════════════════════

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interactionMode == InteractionMode.IOS && isEnabled && !hasCustomBackground) {
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
        if (hasCustomBackground) return

        val baseColor: Int = when (style) {
            Style.FILLED -> cachedPrimaryColor
            Style.PLAIN -> Color.TRANSPARENT
        }

        val isDark = MorphTheme.isDarkMode(context)
        val overlayColor = if (isDark) Color.WHITE else Color.BLACK

        val blended: Int = MorphTheme.overlayColor(baseColor, overlayColor, pressOverlayAlpha)
        shapeDrawable.setColor(blended)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        applyStyle()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Activity 不重建时（configChanges 包含 uiMode），手动刷新颜色
        applyStyle()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        pressAnimator?.cancel()
        pressAnimator = null
    }

    companion object {
        private val PRESS_OVERLAY_MAX_ALPHA = MorphTokens.pressOverlayMaxAlpha
        private val PRESS_IN_DURATION = MorphTokens.motionDurationSm
        private val PRESS_OUT_DURATION = MorphTokens.pressOutDuration
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试专用访问方法 — 避免 reflect 读取私有字段
    // ═══════════════════════════════════════════════════════════════════════

    /** 测试专用：获取交互模式 */
    @VisibleForTesting
    internal val testInteractionMode: InteractionMode get() = interactionMode

    /** 测试专用：获取圆角半径 */
    @VisibleForTesting
    internal val testCornerRadius: Float get() = cornerRadius

    /** 测试专用：获取背景 ShapeDrawable */
    @VisibleForTesting
    internal val testShapeDrawable: GradientDrawable get() = shapeDrawable

    /** 测试专用：是否使用了自定义背景 */
    @VisibleForTesting
    internal val testHasCustomBackground: Boolean get() = hasCustomBackground
}
