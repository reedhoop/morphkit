package com.morphkit.widget.selection

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens

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

    private val iosHelper = MorphCompoundButtonHelper(
        button = this,
        attrs = attrs,
        defStyleAttr = defStyleAttr,
        styleableArray = R.styleable.MorphCheckBox,
        interactionModeIndex = R.styleable.MorphCheckBox_morphInteractionMode,
        onRefreshColors = ::refreshColorCache,
        indicatorWidthProvider = {
            val gap = (MorphTokens.spacingSm.toFloat() * context.resources.displayMetrics.density).toInt()
            boxSize.toInt() + gap
        }
    )

    private val interactionMode: InteractionMode get() = iosHelper.interactionMode

    // ── iOS 指示器绘制 ──
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val checkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val checkPath = Path()
    private val tempRect = RectF()

    private var boxSize: Float = 0f
    private var cornerRadius: Float = 0f
    private var strokeWidth: Float = 0f
    private var checkStrokeWidth: Float = 0f

    // ── 缓存颜色 ──
    private var primaryColor: Int = 0
    private var onPrimaryColor: Int = 0
    private var surfaceVariantColor: Int = 0

    init {
        // ── 初始化尺寸 ──
        val density = context.resources.displayMetrics.density
        boxSize = 20f * density
        cornerRadius = 4f * density
        strokeWidth = 2f * density
        checkStrokeWidth = 2.5f * density

        // ── 缓存语义颜色 ──
        refreshColorCache()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        iosHelper.onAttachedToWindow()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        iosHelper.onConfigurationChanged()
    }

    override fun onDraw(canvas: Canvas) {
        if (interactionMode == InteractionMode.IOS) {
            drawIosIndicator(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawIosIndicator(canvas: Canvas) {
        // 注意：paddingLeft 已被增加以推开文字，此处使用 originalPaddingLeft
        val left = iosHelper.originalPaddingLeft.toFloat()
        val top = (height - boxSize) / 2f
        val right = left + boxSize
        val bottom = top + boxSize

        // ── disabled 态：应用 MorphTokens.disabledAlpha 透明度 ──
        val alpha = if (isEnabled) 1f else MorphTokens.disabledAlpha

        if (isChecked) {
            // 选中态：主色填充圆角方形
            boxPaint.style = Paint.Style.FILL
            boxPaint.color = MorphColors.adjustAlpha(primaryColor, alpha)
            drawRoundRect(canvas, left, top, right, bottom)

            // 白色勾选标记（disabled 时同样降低透明度）
            checkPaint.color = MorphColors.adjustAlpha(onPrimaryColor, alpha)
            drawCheckMark(canvas, left, top)
        } else {
            // 未选中态：灰色边框圆角方形
            strokePaint.strokeWidth = strokeWidth
            strokePaint.color = MorphColors.adjustAlpha(surfaceVariantColor, alpha)
            drawRoundRectStroke(canvas, left, top, right, bottom)
        }
    }

    private fun drawRoundRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        tempRect.set(left, top, right, bottom)
        canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, boxPaint)
    }

    private fun drawRoundRectStroke(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val inset = strokeWidth / 2f
        tempRect.set(left + inset, top + inset, right - inset, bottom - inset)
        canvas.drawRoundRect(tempRect, cornerRadius, cornerRadius, strokePaint)
    }

    private fun drawCheckMark(canvas: Canvas, boxLeft: Float, boxTop: Float) {
        // checkPaint.color 由调用方 drawIosIndicator() 设置（含 disabled alpha）
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
        iosHelper.onMeasure()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun refreshColorCache() {
        primaryColor = MorphTheme.morphColorPrimary(context)
        onPrimaryColor = MorphTheme.morphColorOnPrimary(context)
        surfaceVariantColor = MorphTheme.morphColorSurfaceVariant(context)
    }
}
