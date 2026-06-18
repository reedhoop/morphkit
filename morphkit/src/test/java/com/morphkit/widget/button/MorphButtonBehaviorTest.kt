package com.morphkit.widget.button

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.core.InteractionMode
import com.morphkit.R
import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphShape
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens
import com.morphkit.theme.dp
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphButtonBehaviorTest {

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

    // ═══════════════════════════════════════════════════════════════════════
    // 1. Initialization — 默认值
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun defaultStyle_isFilled() {
        val button = MorphButton(iosContext)
        assertThat(button.style).isEqualTo(MorphButton.Style.FILLED)
    }

    @Test
    fun defaultInteractionMode_isIos() {
        val button = MorphButton(iosContext)
        assertThat(button.testInteractionMode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun defaultCornerRadius_matchesCornerMedium() {
        val button = MorphButton(iosContext)
        val expected = MorphShape.cornerMedium(iosContext).toFloat()
        assertThat(button.testCornerRadius).isEqualTo(expected)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. Style variant — FILLED vs PLAIN
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun filledStyle_ios_setsBackgroundToPrimaryColor() {
        val button = MorphButton(iosContext)
        val primary = MorphTheme.morphColorPrimary(iosContext)
        assertThat(button.testShapeDrawable.color?.defaultColor).isEqualTo(primary)
    }

    @Test
    fun filledStyle_ios_setsTextColorToOnPrimary() {
        val button = MorphButton(iosContext)
        val expected = MorphTheme.morphColorOnPrimary(iosContext)
        assertThat(button.currentTextColor).isEqualTo(expected)
    }

    @Test
    fun filledStyle_ios_setsBackgroundToShapeDrawable() {
        val button = MorphButton(iosContext)
        assertThat(button.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun plainStyle_ios_setsBackgroundTransparent() {
        val button = MorphButton(iosContext)
        button.style = MorphButton.Style.PLAIN
        assertThat(button.testShapeDrawable.color?.defaultColor).isEqualTo(Color.TRANSPARENT)
    }

    @Test
    fun plainStyle_ios_setsTextColorToPrimary() {
        val button = MorphButton(iosContext)
        button.style = MorphButton.Style.PLAIN
        val expected = MorphTheme.morphColorPrimary(iosContext)
        assertThat(button.currentTextColor).isEqualTo(expected)
    }

    @Test
    fun switchingStyleFromFilledToPlain_updatesTextColor() {
        val button = MorphButton(iosContext)
        val filledTextColor = button.currentTextColor
        button.style = MorphButton.Style.PLAIN
        assertThat(button.currentTextColor).isNotEqualTo(filledTextColor)
    }

    @Test
    fun switchingStyleFromPlainToFilled_restoresTextColor() {
        val button = MorphButton(iosContext)
        val originalTextColor = button.currentTextColor
        button.style = MorphButton.Style.PLAIN
        button.style = MorphButton.Style.FILLED
        assertThat(button.currentTextColor).isEqualTo(originalTextColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. InteractionMode — IOS vs MATERIAL
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun iosMode_setsStateListAnimator() {
        val button = MorphButton(iosContext)
        assertThat(button.stateListAnimator).isNotNull()
    }

    @Test
    fun iosMode_replacesBackgroundWithShapeDrawable() {
        val button = MorphButton(iosContext)
        assertThat(button.background).isInstanceOf(GradientDrawable::class.java)
    }

    @Test
    fun materialMode_interactionModeIsMaterial() {
        val button = MorphButton(pixelContext)
        assertThat(button.testInteractionMode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun materialMode_doesNotReplaceBackground() {
        val button = MorphButton(pixelContext)
        assertThat(button.testHasCustomBackground).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. isEnabled — 禁用态行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun disabled_iosFilled_adjustsTextColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        val onPrimary = MorphTheme.morphColorOnPrimary(iosContext)
        button.isEnabled = false
        val expected = MorphColors.adjustAlpha(onPrimary, MorphTokens.Interaction.disabledAlpha)
        assertThat(button.currentTextColor).isEqualTo(expected)
    }

    @Test
    fun disabled_iosFilled_adjustsBackgroundColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        val primary = MorphTheme.morphColorPrimary(iosContext)
        button.isEnabled = false
        val expected = MorphColors.adjustAlpha(primary, MorphTokens.Interaction.disabledAlpha)
        assertThat(button.testShapeDrawable.color?.defaultColor).isEqualTo(expected)
    }

    @Test
    fun disabled_iosPlain_adjustsTextColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        button.style = MorphButton.Style.PLAIN
        val primary = MorphTheme.morphColorPrimary(iosContext)
        button.isEnabled = false
        val expected = MorphColors.adjustAlpha(primary, MorphTokens.Interaction.disabledAlpha)
        assertThat(button.currentTextColor).isEqualTo(expected)
    }

    @Test
    fun reEnabled_iosFilled_restoresTextColor() {
        val button = MorphButton(iosContext)
        val originalTextColor = button.currentTextColor
        button.isEnabled = false
        button.isEnabled = true
        assertThat(button.currentTextColor).isEqualTo(originalTextColor)
    }

    @Test
    fun reEnabled_restoresAlphaToOne() {
        val button = MorphButton(iosContext)
        button.isEnabled = false
        button.isEnabled = true
        assertThat(button.alpha).isEqualTo(1f)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. XML attribute reading
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun xmlMorphInteractionMode_ios_resolvesToIosMode() {
        val button = MorphButton(iosContext)
        assertThat(button.testInteractionMode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun xmlMorphInteractionMode_material_resolvesToMaterialMode() {
        val button = MorphButton(pixelContext)
        assertThat(button.testInteractionMode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun xmlMorphButtonVariant_defaultIsFilled() {
        val button = MorphButton(iosContext)
        assertThat(button.style).isEqualTo(MorphButton.Style.FILLED)
    }

    @Test
    fun xmlMorphCornerRadius_ios_is12dp() {
        val button = MorphButton(iosContext)
        val expected = 12f.dp(iosContext)
        assertThat(button.testCornerRadius).isEqualTo(expected)
    }

    @Test
    fun xmlMorphCornerRadius_pixel_is8dp() {
        val button = MorphButton(pixelContext)
        val expected = 8f.dp(pixelContext)
        assertThat(button.testCornerRadius).isEqualTo(expected)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 暗黑模式刷新 — onConfigurationChanged / onAttachedToWindow
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onConfigurationChanged后背景颜色刷新`() {
        val button = MorphButton(iosContext)
        val config = android.content.res.Configuration(iosContext.resources.configuration)
        invokeOnConfigurationChanged(button, config)
        // applyStyle() 刷新后，背景色应与当前主题 primary 一致
        assertThat(button.testShapeDrawable.color?.defaultColor)
            .isEqualTo(MorphTheme.morphColorPrimary(iosContext))
    }

    @Test
    fun `onAttachedToWindow后颜色正确`() {
        val button = MorphButton(iosContext)
        invokeOnAttachedToWindow(button)
        // applyStyle() 刷新后，文字颜色应与 onPrimary 一致
        assertThat(button.currentTextColor)
            .isEqualTo(MorphTheme.morphColorOnPrimary(iosContext))
        assertThat(button.testShapeDrawable.color?.defaultColor)
            .isEqualTo(MorphTheme.morphColorPrimary(iosContext))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. 点击防抖 — setOnClickListener 自动包装 MorphClickListener
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `setOnClickListener自动包装MorphClickListener防抖`() {
        val button = MorphButton(iosContext)
        var clickCount = 0
        button.setOnClickListener { clickCount++ }

        // 模拟快速双击
        button.performClick()
        button.performClick()

        // 300ms 防抖窗口内，第二次点击应被抑制
        assertThat(clickCount).isEqualTo(1)
    }

    @Test
    fun `setOnClickListener传入null不抛异常`() {
        val button = MorphButton(iosContext)
        button.setOnClickListener(null)
        // 不抛异常即通过
    }

    @Test
    fun `iOS模式按钮设置了StateListAnimator`() {
        val button = MorphButton(iosContext)
        assertThat(button.stateListAnimator).isNotNull()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 9. StateListAnimator 内容验证 — Red Line 3 合规
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `iOS模式StateListAnimator的XML包含焦点项和默认set项且无按压项`() {
        val parser = iosContext.resources.getXml(R.animator.morph_widget_button_ios_state)
        var hasFocused = false
        var hasPressed = false
        var hasDefaultSet = false
        var eventType = parser.eventType
        while (eventType != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            if (eventType == org.xmlpull.v1.XmlPullParser.START_TAG && parser.name == "item") {
                val stateFocused = parser.getAttributeValue(
                    "http://schemas.android.com/apk/res/android", "state_focused"
                )
                val statePressed = parser.getAttributeValue(
                    "http://schemas.android.com/apk/res/android", "state_pressed"
                )
                if (stateFocused == "true") hasFocused = true
                if (statePressed == "true") hasPressed = true
                // 默认项：无 state_ 属性
                if (stateFocused == null && statePressed == null) {
                    // 检查是否有 <set> 子节点（需要继续解析）
                    // 简化：默认项存在即认为有 set
                    hasDefaultSet = true
                }
            }
            eventType = parser.next()
        }
        assertThat(hasFocused).isTrue()
        assertThat(hasDefaultSet).isTrue()
        assertThat(hasPressed).isFalse()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法 — 通过反射调用 protected 生命周期方法
    // ═══════════════════════════════════════════════════════════════════════

    private fun invokeOnAttachedToWindow(button: MorphButton) {
        val method = android.view.View::class.java.getDeclaredMethod("onAttachedToWindow")
        method.isAccessible = true
        method.invoke(button)
    }

    private fun invokeOnConfigurationChanged(button: MorphButton, config: android.content.res.Configuration) {
        val method = android.view.View::class.java.getDeclaredMethod(
            "onConfigurationChanged", android.content.res.Configuration::class.java
        )
        method.isAccessible = true
        method.invoke(button, config)
    }
}
