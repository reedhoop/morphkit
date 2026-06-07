package com.morphkit.widget.selection

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatCheckBox
import com.morphkit.R
import com.morphkit.theme.MorphTheme
import com.morphkit.core.MorphClickListener

/**
 * MorphKit 多风格多选按钮 — iOS 圆角方形勾选 / Material 原生涟漪。
 *
 * iOS 模式：
 * - 移除默认 Ripple，使用自定义圆角方形指示器绘制
 * - 选中态：主色填充圆角方形 + 白色勾选标记
 * - 未选中态：灰色边框圆角方形
 * - 按压反馈：StateListAnimator alpha 变暗
 * - 焦点补偿：StateListAnimator translationZ 抬升（A11y 合规）
 *
 * Material 模式：
 * - 保留 M3 默认 Ripple + 系统指示器
 */
class MorphCheckBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.morphCheckBoxStyle
) : AppCompatCheckBox(context, attrs, defStyleAttr) {

    enum class InteractionMode { IOS, MATERIAL }

    private val interactionMode: InteractionMode

    // ── iOS 指示器绘制 ──
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val checkPath = Path()

    private var boxSize: Float = 0f
    private var cornerRadius: Float = 0f
    private var strokeWidth: Float = 0f
    private var checkStrokeWidth: Float = 0f

    // ── 缓存颜色 ──
    private var primaryColor: Int = 0
    private var onPrimaryColor: Int = 0
    private var surfaceVariantColor: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.MorphCheckBox, defStyleAttr, 0)
        try {
            val modeValue = a.getInt(R.styleable.MorphCheckBox_morphInteractionMode, 0)
            interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
        } finally {
            a.recycle()
        }

        // ── 初始化尺寸 ──
        val density = context.resources.displayMetrics.density
        boxSize = 20f * density
        cornerRadius = 4f * density
        strokeWidth = 2f * density
        checkStrokeWidth = 2.5f * density

        // ── 缓存语义颜色 ──
        primaryColor = MorphTheme.morphColorPrimary(context)
        onPrimaryColor = MorphTheme.morphColorOnPrimary(context)
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
            R.animator.morph_widget_checkbox_ios_state
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
        val left = paddingLeft.toFloat()
        val top = (height - boxSize) / 2f
        val right = left + boxSize
        val bottom = top + boxSize

        if (isChecked) {
            // 选中态：主色填充圆角方形
            boxPaint.style = Paint.Style.FILL
            boxPaint.color = primaryColor
            drawRoundRect(canvas, left, top, right, bottom)

            // 白色勾选标记
            drawCheckMark(canvas, left, top)
        } else {
            // 未选中态：灰色边框圆角方形
            strokePaint.strokeWidth = strokeWidth
            strokePaint.color = surfaceVariantColor
            drawRoundRectStroke(canvas, left, top, right, bottom)
        }
    }

    private fun drawRoundRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val rect = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, boxPaint)
    }

    private fun drawRoundRectStroke(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val inset = strokeWidth / 2f
        val rect = android.graphics.RectF(left + inset, top + inset, right - inset, bottom - inset)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
    }

    private fun drawCheckMark(canvas: Canvas, boxLeft: Float, boxTop: Float) {
        checkPaint.color = onPrimaryColor
        checkPaint.strokeWidth = checkStrokeWidth

        // 勾选标记路径：短左下斜线 + 长右斜线
        val padding = boxSize * 0.22f
        val checkLeft = boxLeft + padding
        val checkMidX = boxLeft + boxSize * 0.42f
        val checkRight = boxLeft + boxSize - padding
        val checkTopY = boxTop + boxSize * 0.52f
        val checkBottomY = boxTop + boxSize - padding
        val checkTopRightY = boxTop + padding + boxSize * 0.08f

        checkPath.reset()
        checkPath.moveTo(checkLeft, checkTopY)
        checkPath.lineTo(checkMidX, checkBottomY)
        checkPath.lineTo(checkRight, checkTopRightY)

        canvas.drawPath(checkPath, checkPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (interactionMode == InteractionMode.IOS) {
            // ── 为指示器预留空间 ──
            val indicatorWidth = (boxSize + 8f * context.resources.displayMetrics.density).toInt()
            val newWidth = measuredWidth + indicatorWidth
            setMeasuredDimension(newWidth, measuredHeight)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // iOS 模式下按压变色由 StateListAnimator 处理，无需手动干预
        return super.onTouchEvent(event)
    }
}
