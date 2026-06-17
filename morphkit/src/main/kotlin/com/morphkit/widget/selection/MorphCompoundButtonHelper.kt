package com.morphkit.widget.selection

import android.animation.AnimatorInflater
import android.util.AttributeSet
import android.widget.CompoundButton
import com.morphkit.R
import com.morphkit.core.InteractionMode

/**
 * iOS 模式 CompoundButton 共享逻辑辅助类。
 *
 * 封装 MorphRadioButton / MorphCheckBox 共享的 iOS 模式初始化、
 * 颜色刷新、paddingLeft 调整等逻辑，消除两个控件间的代码重复。
 *
 * 使用方式：
 * ```kotlin
 * class MorphRadioButton(...) : AppCompatRadioButton(...) {
 *     private val iosHelper = MorphCompoundButtonHelper(this, attrs, defStyleAttr,
 *         R.styleable.MorphRadioButton, R.styleable.MorphRadioButton_morphInteractionMode,
 *         ::refreshColorCache, indicatorWidth = { ... })
 *     // iosHelper.onAttachedToWindow(), iosHelper.onConfigurationChanged(), etc.
 * }
 * ```
 */
internal class MorphCompoundButtonHelper(
    private val button: CompoundButton,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    styleableArray: IntArray,
    interactionModeIndex: Int,
    private val onRefreshColors: () -> Unit,
    private val indicatorWidthProvider: () -> Int
) {
    /**
     * 预计算的指示器宽度，避免 onMeasure 中每次调用 indicatorWidthProvider()。
     *
     * 使用 lazy 延迟求值：宿主控件构造时 boxSize/ringRadius 等尺寸字段尚未在 init 块中赋值，
     * 若在 Helper 构造时立即求值会得到 0。lazy 保证首次 onMeasure（此时宿主 init 已完成）才计算。
     */
    private val cachedIndicatorWidth: Int by lazy { indicatorWidthProvider() }

    val interactionMode: InteractionMode

    var originalPaddingStart: Int = 0
        private set

    init {
        val a = button.context.obtainStyledAttributes(attrs, styleableArray, defStyleAttr, 0)
        try {
            val modeValue = a.getInt(interactionModeIndex, 0)
            interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
        } finally {
            a.recycle()
        }

        originalPaddingStart = button.paddingStart

        if (interactionMode == InteractionMode.IOS) {
            initIosMode()
        }
    }

    fun onAttachedToWindow() {
        if (interactionMode == InteractionMode.IOS) {
            onRefreshColors()
            button.invalidate()
        }
    }

    fun onConfigurationChanged() {
        if (interactionMode == InteractionMode.IOS) {
            onRefreshColors()
            button.invalidate()
        }
    }

    fun onMeasure() {
        if (interactionMode == InteractionMode.IOS) {
            val targetPaddingStart = originalPaddingStart + cachedIndicatorWidth
            if (button.paddingStart != targetPaddingStart) {
                button.setPaddingRelative(
                    targetPaddingStart, button.paddingTop,
                    button.paddingEnd, button.paddingBottom
                )
            }
        }
    }

    private fun initIosMode() {
        button.setButtonDrawable(android.R.color.transparent)

        button.stateListAnimator = AnimatorInflater.loadStateListAnimator(
            button.context,
            R.animator.morph_widget_selection_ios_state
        )
        // 注意：CompoundButton 的 toggle() 在 performClick() 中独立于 OnClickListener 执行，
        // MorphClickListener 防抖无法阻止 toggle。因此此处不设置空回调防抖，
        // 由 MorphRadioButton/MorphCheckBox 覆写 setOnClickListener 为业务监听器提供防抖。
    }
}
