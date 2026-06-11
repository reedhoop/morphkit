package com.morphkit.widget.selection

import android.animation.AnimatorInflater
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.CompoundButton
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.core.MorphClickListener

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
    val interactionMode: InteractionMode

    var originalPaddingLeft: Int = 0
        private set

    init {
        val a = button.context.obtainStyledAttributes(attrs, styleableArray, defStyleAttr, 0)
        try {
            val modeValue = a.getInt(interactionModeIndex, 0)
            interactionMode = if (modeValue == 1) InteractionMode.MATERIAL else InteractionMode.IOS
        } finally {
            a.recycle()
        }

        originalPaddingLeft = button.paddingLeft

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
            val targetPaddingLeft = originalPaddingLeft + indicatorWidthProvider()
            if (button.paddingLeft != targetPaddingLeft) {
                button.setPadding(targetPaddingLeft, button.paddingTop, button.paddingRight, button.paddingBottom)
            }
        }
    }

    private fun initIosMode() {
        button.buttonDrawable = null
        button.setButtonDrawable(android.R.color.transparent)

        button.stateListAnimator = AnimatorInflater.loadStateListAnimator(
            button.context,
            R.animator.morph_widget_selection_ios_state
        )

        button.setOnClickListener(MorphClickListener { /* 点击由 OnCheckedChangeListener 处理 */ })
    }
}
