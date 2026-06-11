package com.morphkit.widget.button

import android.animation.AnimatorInflater
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.core.MorphClickListener
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
            val gap = (MorphTokens.spacingSm.toFloat() * context.resources.displayMetrics.density).toInt()
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
        // ── 移除默认按钮指示器，改用自定义绘制 ──
        buttonDrawable = null
        setButtonDrawable(android.R.color.transparent)

        // ── 无障碍合规：StateListAnimator 分离按压反馈与焦点反馈 ──
        stateListAnimator = AnimatorInflater.loadStateListAnimator(
            context,
            R.animator.morph_widget_selection_ios_state
        )

        // ── 防抖包装 ──
        setOnClickListener(MorphClickListener { /* 点击由 CompoundButton.OnCheckedChangeListener 处理 */ })
    }

    override fun onDraw(canvas: Canvas) {
        if (interactionMode == InteractionMode.IOS) {
            drawIosIndicator(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawIosIndicator(canvas: Canvas) {
        // ── 指示器位置：垂直居中，水平在原始 paddingLeft 偏移处 ──
        // 注意：paddingLeft 已被增加以推开文字，此处使用 originalPaddingLeft
        val cx = ringRadius + ringStrokeWidth / 2 + iosHelper.originalPaddingLeft
        val cy = height / 2f

        ringPaint.strokeWidth = ringStrokeWidth

        if (isChecked) {
            // 选中态：主色圆环 + 主色实心圆点
            ringPaint.color = primaryColor
            dotPaint.color = primaryColor
            canvas.drawCircle(cx, cy, ringRadius, ringPaint)
            canvas.drawCircle(cx, cy, dotRadius, dotPaint)
        } else {
            // 未选中态：灰色圆环
            ringPaint.color = surfaceVariantColor
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
}
