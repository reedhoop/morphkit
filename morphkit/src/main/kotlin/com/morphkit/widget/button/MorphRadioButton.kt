package com.morphkit.widget.button

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatRadioButton
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.core.MorphClickListener
import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens
import com.morphkit.widget.selection.MorphCompoundButtonHelper

/**
 * MorphKit 多风格单选按钮 — iOS 圆形指示器 / Material 原生涟漪。
 *
 * iOS 模式：
 * - 移除默认 Ripple，使用自定义圆形指示器绘制
 * - 选中态：主色圆环 + 主色实心圆点
 * - 未选中态：灰色圆环
 * - 按压反馈：StateListAnimator alpha 变暗
 * - 焦点补偿：StateListAnimator translationZ 抬升（A11y 合规）
 *
 * Material 模式：
 * - 保留 M3 默认 Ripple + 系统指示器
 */
class MorphRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphRadioButtonStyle
) : AppCompatRadioButton(context, attrs, defStyleAttr) {

    private val iosHelper = MorphCompoundButtonHelper(
        button = this,
        attrs = attrs,
        defStyleAttr = defStyleAttr,
        styleableArray = R.styleable.MorphRadioButton,
        interactionModeIndex = R.styleable.MorphRadioButton_morphInteractionMode,
        onRefreshColors = ::refreshColorCache,
        indicatorWidthProvider = {
            val gap = (MorphTokens.Spacing.spacingSm.toFloat() * context.resources.displayMetrics.density).toInt()
            ((ringRadius + ringStrokeWidth / 2) * 2).toInt() + gap
        }
    )

    private val interactionMode: InteractionMode get() = iosHelper.interactionMode

    // ── iOS 指示器绘制 ──
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var ringRadius: Float = 0f
    private var dotRadius: Float = 0f
    private var ringStrokeWidth: Float = 0f

    // ── 缓存颜色 ──
    private var primaryColor: Int = 0
    private var surfaceVariantColor: Int = 0

    init {
        // ── 初始化尺寸 ──
        val density = context.resources.displayMetrics.density
        ringRadius = 10f * density
        dotRadius = 5f * density
        ringStrokeWidth = 2f * density

        // ── 缓存语义颜色 ──
        refreshColorCache()

        when (interactionMode) {
            InteractionMode.IOS -> initIosMode()
            InteractionMode.MATERIAL -> { /* 保留 M3 默认 */ }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        iosHelper.onAttachedToWindow()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        iosHelper.onConfigurationChanged()
    }

    private fun initIosMode() {
        // MorphCompoundButtonHelper.initIosMode() 已完成：
        // - setButtonDrawable(transparent)
        // - StateListAnimator 加载
        // - MorphClickListener 防抖包装
        // 此处无需重复，避免 StateListAnimator 二次加载浪费内存
    }

    override fun onDraw(canvas: Canvas) {
        if (interactionMode == InteractionMode.IOS) {
            drawIosIndicator(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawIosIndicator(canvas: Canvas) {
        // ── 指示器位置：垂直居中，水平在原始 paddingStart 偏移处（支持 RTL）──
        // 注意：paddingStart 已被增加以推开文字，此处使用 originalPaddingStart
        val indicatorOffset = ringRadius + ringStrokeWidth / 2 + iosHelper.originalPaddingStart
        val cx = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            width - indicatorOffset
        } else {
            indicatorOffset
        }
        val cy = height / 2f

        ringPaint.strokeWidth = ringStrokeWidth

        // ── disabled 态：应用 MorphTokens.Interaction.disabledAlpha 透明度 ──
        val alpha = if (isEnabled) 1f else MorphTokens.Interaction.disabledAlpha

        if (isChecked) {
            // 选中态：主色圆环 + 主色实心圆点
            ringPaint.color = MorphColors.adjustAlpha(primaryColor, alpha)
            dotPaint.color = MorphColors.adjustAlpha(primaryColor, alpha)
            canvas.drawCircle(cx, cy, ringRadius, ringPaint)
            canvas.drawCircle(cx, cy, dotRadius, dotPaint)
        } else {
            // 未选中态：灰色圆环
            ringPaint.color = MorphColors.adjustAlpha(surfaceVariantColor, alpha)
            canvas.drawCircle(cx, cy, ringRadius, ringPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        iosHelper.onMeasure()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun refreshColorCache() {
        primaryColor = MorphTheme.morphColorPrimary(context)
        surfaceVariantColor = MorphTheme.morphColorSurfaceVariant(context)
    }

    /**
     * 覆写 setOnClickListener，使用 MorphClickListener 包装提供防抖保护。
     * 注意：CompoundButton.toggle() 在 performClick() 中独立执行，
     * 防抖仅影响业务回调的触发频率，不影响选中状态切换。
     */
    override fun setOnClickListener(l: OnClickListener?) {
        if (l == null) {
            super.setOnClickListener(null)
        } else {
            super.setOnClickListener(MorphClickListener { l.onClick(this) })
        }
    }
}
