package com.morphkit.widget.button

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.core.InteractionMode
import com.morphkit.R
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
    fun defaultStyle_isFilled() {
        val button = MorphButton(iosContext)
        assertThat(button.style).isEqualTo(MorphButton.Style.FILLED)
    }

    @Test
    fun defaultInteractionMode_isIos() {
        val button = MorphButton(iosContext)
        val mode: InteractionMode = button.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun defaultCornerRadius_matchesCornerMedium() {
        val button = MorphButton(iosContext)
        val cornerRadius: Float = button.readField("cornerRadius")
        val expected = MorphTheme.cornerMedium(iosContext).toFloat()
        assertThat(cornerRadius).isEqualTo(expected)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. Style variant — FILLED vs PLAIN
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun filledStyle_ios_setsBackgroundToPrimaryColor() {
        val button = MorphButton(iosContext)
        val primary = MorphTheme.morphColorPrimary(iosContext)
        val shapeDrawable: GradientDrawable = button.readField("shapeDrawable")
        assertThat(shapeDrawable.color?.defaultColor).isEqualTo(primary)
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
        val shapeDrawable: GradientDrawable = button.readField("shapeDrawable")
        assertThat(shapeDrawable.color?.defaultColor).isEqualTo(Color.TRANSPARENT)
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
        val mode: InteractionMode = button.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun materialMode_doesNotReplaceBackground() {
        val button = MorphButton(pixelContext)
        val hasCustomBg: Boolean = button.readField("hasCustomBackground")
        assertThat(hasCustomBg).isTrue()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. isEnabled — 禁用态行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun disabled_iosFilled_adjustsTextColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        val onPrimary = MorphTheme.morphColorOnPrimary(iosContext)
        button.isEnabled = false
        val expected = MorphTheme.adjustAlpha(onPrimary, MorphTokens.disabledAlpha)
        assertThat(button.currentTextColor).isEqualTo(expected)
    }

    @Test
    fun disabled_iosFilled_adjustsBackgroundColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        val primary = MorphTheme.morphColorPrimary(iosContext)
        button.isEnabled = false
        val shapeDrawable: GradientDrawable = button.readField("shapeDrawable")
        val expected = MorphTheme.adjustAlpha(primary, MorphTokens.disabledAlpha)
        assertThat(shapeDrawable.color?.defaultColor).isEqualTo(expected)
    }

    @Test
    fun disabled_iosPlain_adjustsTextColorWithDisabledAlpha() {
        val button = MorphButton(iosContext)
        button.style = MorphButton.Style.PLAIN
        val primary = MorphTheme.morphColorPrimary(iosContext)
        button.isEnabled = false
        val expected = MorphTheme.adjustAlpha(primary, MorphTokens.disabledAlpha)
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
        val mode: InteractionMode = button.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun xmlMorphInteractionMode_material_resolvesToMaterialMode() {
        val button = MorphButton(pixelContext)
        val mode: InteractionMode = button.readField("interactionMode")
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun xmlMorphButtonVariant_defaultIsFilled() {
        val button = MorphButton(iosContext)
        assertThat(button.style).isEqualTo(MorphButton.Style.FILLED)
    }

    @Test
    fun xmlMorphCornerRadius_ios_is12dp() {
        val button = MorphButton(iosContext)
        val cornerRadius: Float = button.readField("cornerRadius")
        val expected = 12f.dp(iosContext)
        assertThat(cornerRadius).isEqualTo(expected)
    }

    @Test
    fun xmlMorphCornerRadius_pixel_is8dp() {
        val button = MorphButton(pixelContext)
        val cornerRadius: Float = button.readField("cornerRadius")
        val expected = 8f.dp(pixelContext)
        assertThat(cornerRadius).isEqualTo(expected)
    }
}
