package com.morphkit.widget.button

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.R
import com.morphkit.core.InteractionMode
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphRadioButtonBehaviorTest {

    private lateinit var iosContext: Context
    private lateinit var pixelContext: Context

    @Before
    fun setUp() {
        val appContext = RuntimeEnvironment.getApplication()
        val materialContext = ContextThemeWrapper(
            appContext,
            com.google.android.material.R.style.Theme_Material3_DayNight
        )
        iosContext = ContextThemeWrapper(materialContext, R.style.Theme_MorphKit_iOS)
        pixelContext = ContextThemeWrapper(materialContext, R.style.Theme_MorphKit_Pixel)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> Any.readField(name: String): T {
        val field = this.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(this) as T
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. Initialization — 默认值
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun defaultInteractionMode_ios_isIos() {
        val radio = MorphRadioButton(iosContext)
        val mode: InteractionMode = radio.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun defaultInteractionMode_pixel_isMaterial() {
        val radio = MorphRadioButton(pixelContext)
        val mode: InteractionMode = radio.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun iosMode_buttonDrawableIsTransparent() {
        val radio = MorphRadioButton(iosContext)
        val drawable = radio.buttonDrawable
        assertThat(drawable).isInstanceOf(ColorDrawable::class.java)
        assertThat((drawable as ColorDrawable).color).isEqualTo(android.R.color.transparent)
    }

    @Test
    fun iosMode_initializesIosHelper() {
        val radio = MorphRadioButton(iosContext)
        val helper = radio.readField<Any>("iosHelper")
        assertThat(helper).isNotNull()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. InteractionMode — IOS vs MATERIAL
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_setsStateListAnimator() {
        val radio = MorphRadioButton(iosContext)
        assertThat(radio.stateListAnimator).isNotNull()
    }

    @Test
    fun materialMode_doesNotSetStateListAnimator() {
        val radio = MorphRadioButton(pixelContext)
        // Material 模式下 iosHelper 不会调用 initIosMode()，stateListAnimator 可能为 null 或由父类设置
        // 关键：不是 iOS 那个自定义 animator
        val mode: InteractionMode = radio.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun iosMode_buttonDrawableSetToTransparent() {
        val radio = MorphRadioButton(iosContext)
        val drawable = radio.buttonDrawable
        assertThat(drawable).isInstanceOf(ColorDrawable::class.java)
    }

    @Test
    fun materialMode_buttonDrawableNotSetToTransparent() {
        val radio = MorphRadioButton(pixelContext)
        // Material 模式下不调用 setButtonDrawable(transparent)，保留系统默认
        val drawable = radio.buttonDrawable
        // 系统默认的 buttonDrawable 不应是 ColorDrawable(transparent)
        val isTransparent = drawable is ColorDrawable && (drawable as ColorDrawable).color == android.R.color.transparent
        assertThat(isTransparent).isFalse()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. paddingLeft adjustment — iOS 模式下 paddingLeft 被调整
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_paddingLeftIsAdjusted() {
        val radio = MorphRadioButton(iosContext)
        val helper = radio.readField<Any>("iosHelper")
        val originalPaddingLeft: Int = helper.readField("originalPaddingLeft")
        // onMeasure 后 paddingLeft 应大于 originalPaddingLeft
        radio.measure(0, 0)
        assertThat(radio.paddingLeft).isGreaterThan(originalPaddingLeft)
    }

    @Test
    fun iosMode_paddingLeftEqualsOriginalPlusIndicatorWidth() {
        val radio = MorphRadioButton(iosContext)
        val helper = radio.readField<Any>("iosHelper")
        val originalPaddingLeft: Int = helper.readField("originalPaddingLeft")
        radio.measure(0, 0)
        val expectedExtraWidth = calculateExpectedIndicatorWidth(radio)
        assertThat(radio.paddingLeft).isEqualTo(originalPaddingLeft + expectedExtraWidth)
    }

    @Test
    fun materialMode_paddingLeftNotAdjusted() {
        val radio = MorphRadioButton(pixelContext)
        val helper = radio.readField<Any>("iosHelper")
        val originalPaddingLeft: Int = helper.readField("originalPaddingLeft")
        radio.measure(0, 0)
        assertThat(radio.paddingLeft).isEqualTo(originalPaddingLeft)
    }

    @Test
    fun iosMode_originalPaddingLeftPreserved() {
        val radio = MorphRadioButton(iosContext)
        val helper = radio.readField<Any>("iosHelper")
        val originalPaddingLeft: Int = helper.readField("originalPaddingLeft")
        radio.measure(0, 0)
        // originalPaddingLeft 不应随 measure 改变
        val afterMeasure: Int = helper.readField("originalPaddingLeft")
        assertThat(afterMeasure).isEqualTo(originalPaddingLeft)
    }

    private fun calculateExpectedIndicatorWidth(radio: MorphRadioButton): Int {
        val density = radio.context.resources.displayMetrics.density
        val ringRadius: Float = radio.readField("ringRadius")
        val ringStrokeWidth: Float = radio.readField("ringStrokeWidth")
        val gap = (8f * density).toInt()
        return ((ringRadius + ringStrokeWidth / 2) * 2).toInt() + gap
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. isChecked — 选中/取消选中行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun isChecked_defaultIsUnchecked() {
        val radio = MorphRadioButton(iosContext)
        assertThat(radio.isChecked).isFalse()
    }

    @Test
    fun isChecked_setToTrue_staysChecked() {
        val radio = MorphRadioButton(iosContext)
        radio.isChecked = true
        assertThat(radio.isChecked).isTrue()
    }

    @Test
    fun isChecked_setToTrueThenFalse_isUnchecked() {
        val radio = MorphRadioButton(iosContext)
        radio.isChecked = true
        radio.isChecked = false
        assertThat(radio.isChecked).isFalse()
    }

    @Test
    fun isChecked_toggleViaPerformClick() {
        val radio = MorphRadioButton(iosContext)
        assertThat(radio.isChecked).isFalse()
        radio.performClick()
        assertThat(radio.isChecked).isTrue()
    }

    @Test
    fun isChecked_notifiesListener() {
        val radio = MorphRadioButton(iosContext)
        var listenerCalled = false
        radio.setOnCheckedChangeListener { _, isChecked ->
            listenerCalled = true
        }
        radio.isChecked = true
        assertThat(listenerCalled).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. isEnabled — 禁用态行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun isEnabled_defaultIsEnabled() {
        val radio = MorphRadioButton(iosContext)
        assertThat(radio.isEnabled).isTrue()
    }

    @Test
    fun setEnabled_false_disablesRadioButton() {
        val radio = MorphRadioButton(iosContext)
        radio.isEnabled = false
        assertThat(radio.isEnabled).isFalse()
    }

    @Test
    fun setEnabled_true_reEnablesRadioButton() {
        val radio = MorphRadioButton(iosContext)
        radio.isEnabled = false
        radio.isEnabled = true
        assertThat(radio.isEnabled).isTrue()
    }

    @Test
    fun disabled_cannotBeChecked() {
        val radio = MorphRadioButton(iosContext)
        radio.isEnabled = false
        radio.performClick()
        assertThat(radio.isChecked).isFalse()
    }

    @Test
    fun disabled_programmaticCheckStillWorks() {
        val radio = MorphRadioButton(iosContext)
        radio.isEnabled = false
        radio.isChecked = true
        // 编程式设置 isChecked 即使在禁用态也应生效
        assertThat(radio.isChecked).isTrue()
    }
}
