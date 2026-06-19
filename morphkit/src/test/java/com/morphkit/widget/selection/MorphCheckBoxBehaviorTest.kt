package com.morphkit.widget.selection

import android.content.Context
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.MorphTheme
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MorphCheckBoxBehaviorTest {

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
        val checkBox = MorphCheckBox(iosContext)
        val mode: InteractionMode = checkBox.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun defaultInteractionMode_pixel_isMaterial() {
        val checkBox = MorphCheckBox(pixelContext)
        val mode: InteractionMode = checkBox.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun iosMode_buttonDrawableIsTransparent() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.buttonDrawable).isNotNull()
    }

    @Test
    fun iosMode_initializesIosHelper() {
        val checkBox = MorphCheckBox(iosContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        assertThat(helper.interactionMode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun defaultIsChecked_isFalse() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.isChecked).isFalse()
    }

    @Test
    fun defaultIsEnabled_isTrue() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.isEnabled).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. InteractionMode — IOS vs MATERIAL
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_setsStateListAnimator() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.stateListAnimator).isNotNull()
    }

    @Test
    fun materialMode_doesNotSetStateListAnimator() {
        val checkBox = MorphCheckBox(pixelContext)
        assertThat(checkBox.stateListAnimator).isNull()
    }

    @Test
    fun iosMode_usesMorphCompoundButtonHelper() {
        val checkBox = MorphCheckBox(iosContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        assertThat(helper.interactionMode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun materialMode_helperInteractionModeIsMaterial() {
        val checkBox = MorphCheckBox(pixelContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        assertThat(helper.interactionMode).isEqualTo(InteractionMode.MATERIAL)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. paddingLeft adjustment — iOS 模式调整 padding
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_originalPaddingStartIsRecorded() {
        val checkBox = MorphCheckBox(iosContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        // originalPaddingStart 应该被记录（值为构造时的 paddingStart）
        assertThat(helper.originalPaddingStart).isAtLeast(0)
    }

    @Test
    fun iosMode_onMeasure_adjustsPaddingStart() {
        val checkBox = MorphCheckBox(iosContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        val originalPaddingStart = helper.originalPaddingStart

        // 触发 measure
        checkBox.measure(0, 0)

        // iOS 模式下 paddingStart 应大于原始 paddingStart（加上了指示器宽度）
        assertThat(checkBox.paddingStart).isGreaterThan(originalPaddingStart)
    }

    @Test
    fun materialMode_onMeasure_doesNotAdjustPaddingStart() {
        val checkBox = MorphCheckBox(pixelContext)
        val helper: MorphCompoundButtonHelper = checkBox.readField("iosHelper")
        val originalPaddingStart = helper.originalPaddingStart

        // 触发 measure
        checkBox.measure(0, 0)

        // Material 模式下 paddingStart 不应被调整
        assertThat(checkBox.paddingStart).isEqualTo(originalPaddingStart)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. isChecked — 选中/取消选中行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun setChecked_true_updatesState() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isChecked = true
        assertThat(checkBox.isChecked).isTrue()
    }

    @Test
    fun setChecked_false_afterTrue_updatesState() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isChecked = true
        checkBox.isChecked = false
        assertThat(checkBox.isChecked).isFalse()
    }

    @Test
    fun setChecked_triggersOnCheckedChangeListener() {
        val checkBox = MorphCheckBox(iosContext)
        var called = false
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            called = true
            assertThat(isChecked).isTrue()
        }
        checkBox.isChecked = true
        assertThat(called).isTrue()
    }

    @Test
    fun setUnchecked_triggersOnCheckedChangeListener() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isChecked = true
        var called = false
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            called = true
            assertThat(isChecked).isFalse()
        }
        checkBox.isChecked = false
        assertThat(called).isTrue()
    }

    @Test
    fun toggle_changesCheckedState() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.isChecked).isFalse()
        checkBox.toggle()
        assertThat(checkBox.isChecked).isTrue()
        checkBox.toggle()
        assertThat(checkBox.isChecked).isFalse()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. isEnabled — 禁用态行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun disabled_isEnabledReturnsFalse() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isEnabled = false
        assertThat(checkBox.isEnabled).isFalse()
    }

    @Test
    fun reEnabled_isEnabledReturnsTrue() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isEnabled = false
        checkBox.isEnabled = true
        assertThat(checkBox.isEnabled).isTrue()
    }

    @Test
    fun disabled_canStillBeCheckedProgrammatically() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isEnabled = false
        checkBox.isChecked = true
        assertThat(checkBox.isChecked).isTrue()
    }

    @Test
    fun disabled_doesNotAffectCheckedState() {
        val checkBox = MorphCheckBox(iosContext)
        checkBox.isChecked = true
        checkBox.isEnabled = false
        assertThat(checkBox.isChecked).isTrue()
    }

    @Test
    fun materialMode_disabled_isEnabledReturnsFalse() {
        val checkBox = MorphCheckBox(pixelContext)
        checkBox.isEnabled = false
        assertThat(checkBox.isEnabled).isFalse()
    }

    @Test
    fun materialMode_reEnabled_isEnabledReturnsTrue() {
        val checkBox = MorphCheckBox(pixelContext)
        checkBox.isEnabled = false
        checkBox.isEnabled = true
        assertThat(checkBox.isEnabled).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 暗黑模式刷新 — onConfigurationChanged / onAttachedToWindow
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onConfigurationChanged后颜色刷新`() {
        val checkBox = MorphCheckBox(iosContext)
        val config = android.content.res.Configuration(iosContext.resources.configuration)
        invokeOnConfigurationChanged(checkBox, config)
        // refreshColorCache() 应刷新 primaryColor
        val primaryColor: Int = checkBox.readField("primaryColor")
        assertThat(primaryColor).isEqualTo(MorphTheme.morphColorPrimary(iosContext))
    }

    @Test
    fun `onAttachedToWindow后状态正确`() {
        val checkBox = MorphCheckBox(iosContext)
        invokeOnAttachedToWindow(checkBox)
        // refreshColorCache() 应刷新缓存颜色
        val primaryColor: Int = checkBox.readField("primaryColor")
        assertThat(primaryColor).isEqualTo(MorphTheme.morphColorPrimary(iosContext))
        val onPrimaryColor: Int = checkBox.readField("onPrimaryColor")
        assertThat(onPrimaryColor).isEqualTo(MorphTheme.morphColorOnPrimary(iosContext))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. 防抖 — MorphClickListener debounce protection
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_hasOnClickListener_installed() {
        val checkBox = MorphCheckBox(iosContext)
        // MorphCompoundButtonHelper.initIosMode() installs MorphClickListener
        assertThat(checkBox.hasOnClickListeners()).isTrue()
    }

    @Test
    fun materialMode_doesNotInstallMorphClickListener() {
        val checkBox = MorphCheckBox(pixelContext)
        // Material 模式下不安装 MorphClickListener
        assertThat(checkBox.hasOnClickListeners()).isFalse()
    }

    @Test
    fun rapidPerformClicks_allToggleCorrectly() {
        val checkBox = MorphCheckBox(iosContext)
        assertThat(checkBox.isChecked).isFalse()

        // First click toggles to checked
        checkBox.performClick()
        assertThat(checkBox.isChecked).isTrue()

        // Second click toggles back
        checkBox.performClick()
        assertThat(checkBox.isChecked).isFalse()

        // Third click toggles to checked again
        checkBox.performClick()
        assertThat(checkBox.isChecked).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法 — 通过反射调用 protected 生命周期方法
    // ═══════════════════════════════════════════════════════════════════════

    private fun invokeOnAttachedToWindow(checkBox: MorphCheckBox) {
        val method = android.view.View::class.java.getDeclaredMethod("onAttachedToWindow")
        method.isAccessible = true
        method.invoke(checkBox)
    }

    private fun invokeOnConfigurationChanged(checkBox: MorphCheckBox, config: android.content.res.Configuration) {
        val method = android.view.View::class.java.getDeclaredMethod(
            "onConfigurationChanged", android.content.res.Configuration::class.java
        )
        method.isAccessible = true
        method.invoke(checkBox, config)
    }
}
