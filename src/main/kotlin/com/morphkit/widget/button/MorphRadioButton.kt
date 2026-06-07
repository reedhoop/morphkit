package com.morphkit.widget.button

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.CompoundButton
import androidx.appcompat.widget.AppCompatRadioButton
import com.morphkit.R
import com.morphkit.theme.MorphTheme
import com.morphkit.core.MorphClickListener

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

    enum class InteractionMode { IOS, MATERIAL }

    private val interactionMode: InteractionMode

    // ── iOS 指示器绘制 ──
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var ringRadius: Float = 0f
    private var dotRadius: Float = 0f
    private var ringStrokeWidth: Float = 0f

    // ── 缓存颜色 ──
    private var primaryColor: Int = 0
    private var onSurfaceColor: Int = 0
    private var surfaceVariantColor: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MorphRadioButton, defStyleAttr, 0)
        try {
            val modeValue = a.getInt(R.styleable.MorphRadioButton_morphInteractionMode, 0)
            interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
        } finally {
            a.recycle()
        }

        // ── 初始化尺寸 ──
        val density = context.resources.displayMetrics.density
        ringRadius = 10f * density
        dotRadius = 5f * density
        ringStrokeWidth = 2f * density

        // ── 缓存语义颜色 ──
        primaryColor = MorphTheme.morphColorPrimary(context)
        onSurfaceColor = MorphTheme.morphColorOnSurface(context)
        surfaceVariantColor = MorphTheme.morphColorSurfaceVariant(context)

        when (interactionMode) {
            InteractionMode.IOS -> initIosMode()
            InteractionMode.MATERIAL -> { /* 保留 M3 默认 */ }
        }
    }

    private fun initIosMode() {
        // ── 移除默认按钮指示器，改用自定义绘制 ──
        buttonDrawable = null
        setButtonDrawable(android.R.color.transparent)

        // ── 无障碍合规：StateListAnimator 分离按压反馈与焦点反馈 ──
        stateListAnimator = AnimatorInflater.loadStateListAnimator(
            context,
            R.animator.morph_widget_radiobutton_ios_state
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
        // ── 指示器位置：垂直居中，水平在文字左侧 ──
        val cx = ringRadius + ringStrokeWidth / 2 + paddingLeft
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
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (interactionMode == InteractionMode.IOS) {
            // ── 为指示器预留空间 ──
            val indicatorWidth = ((ringRadius + ringStrokeWidth / 2) * 2 + 8f * context.resources.displayMetrics.density).toInt()
            val newWidth = measuredWidth + indicatorWidth
            setMeasuredDimension(newWidth, measuredHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // iOS 模式下按压变色由 StateListAnimator 处理，无需手动干预
        return super.onTouchEvent(event)
    }
}
