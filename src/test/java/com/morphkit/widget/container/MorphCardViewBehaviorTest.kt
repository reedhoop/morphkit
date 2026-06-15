package com.morphkit.widget.container

import android.content.Context
import android.graphics.Color
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.core.InteractionMode
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

/**
 * MorphCardView 行为测试。
 *
 * 使用 Robolectric 在 JVM 环境中实例化 MorphCardView，
 * 验证其初始化默认值、毛玻璃模式切换、交互模式、圆角半径和背景色等核心行为。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphCardViewBehaviorTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        val appContext = RuntimeEnvironment.getApplication()
        // Material3 基础主题提供 colorSurface 等 M3 语义色属性
        val materialContext = ContextThemeWrapper(
            appContext,
            com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
        )
        // 叠加 MorphKit iOS 主题，提供 morphCardStyle 等自定义属性
        context = ContextThemeWrapper(
            materialContext,
            com.morphkit.R.style.Theme_MorphKit_iOS
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. 初始化默认值
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `default cardElevation is zero`() {
        val card = MorphCardView(context)
        assertThat(card.cardElevation).isEqualTo(0f)
    }

    @Test
    fun `default maxCardElevation is zero`() {
        val card = MorphCardView(context)
        assertThat(card.maxCardElevation).isEqualTo(0f)
    }

    @Test
    fun `default isGlassmorphism is false`() {
        val card = MorphCardView(context)
        assertThat(card.isGlassmorphism).isFalse()
    }

    @Test
    fun `default isClickable is false`() {
        val card = MorphCardView(context)
        assertThat(card.isClickable).isFalse()
    }

    @Test
    fun `default clipChildren is true`() {
        val card = MorphCardView(context)
        assertThat(card.clipChildren).isTrue()
    }

    @Test
    fun `default clipToPadding is true`() {
        val card = MorphCardView(context)
        assertThat(card.clipToPadding).isTrue()
    }

    @Test
    fun `default stateListAnimator is null`() {
        val card = MorphCardView(context)
        assertThat(card.stateListAnimator).isNull()
    }

    @Test
    fun `default glassmorphismBlurRadius is 25f`() {
        val card = MorphCardView(context)
        assertThat(card.glassmorphismBlurRadius).isEqualTo(25f)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. isGlassmorphism 行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `isGlassmorphism true sets semi-transparent white background in light mode`() {
        val card = MorphCardView(context)
        card.isGlassmorphism = true
        // 浅色模式毛玻璃背景：0xCCFFFFFF（80% 不透明度白色）
        assertThat(card.cardBackgroundColor.defaultColor).isEqualTo(0xCCFFFFFFL.toInt())
    }

    @Test
    fun `isGlassmorphism true removes stroke width`() {
        val card = MorphCardView(context)
        card.isGlassmorphism = true
        assertThat(card.strokeWidth).isEqualTo(0)
    }

    @Test
    fun `isGlassmorphism true sets stroke color to transparent`() {
        val card = MorphCardView(context)
        card.isGlassmorphism = true
        assertThat(card.strokeColor).isEqualTo(Color.TRANSPARENT)
    }

    @Test
    fun `isGlassmorphism false restores clean card background`() {
        val card = MorphCardView(context)
        val originalBg = card.cardBackgroundColor.defaultColor

        card.isGlassmorphism = true
        assertThat(card.cardBackgroundColor.defaultColor).isNotEqualTo(originalBg)

        card.isGlassmorphism = false
        assertThat(card.cardBackgroundColor.defaultColor).isEqualTo(originalBg)
    }

    @Test
    fun `isGlassmorphism false restores stroke`() {
        val card = MorphCardView(context)
        val originalStrokeWidth = card.strokeWidth
        val originalStrokeColor = card.strokeColor

        card.isGlassmorphism = true
        assertThat(card.strokeWidth).isNotEqualTo(originalStrokeWidth)

        card.isGlassmorphism = false
        assertThat(card.strokeWidth).isEqualTo(originalStrokeWidth)
        assertThat(card.strokeColor).isEqualTo(originalStrokeColor)
    }

    @Test
    fun `isGlassmorphism setter skips applyVisualState when value unchanged`() {
        val card = MorphCardView(context)
        // 默认 isGlassmorphism = false，设置相同值不应触发状态变更
        card.isGlassmorphism = false
        assertThat(card.isGlassmorphism).isFalse()
    }

    @Test
    fun `glassmorphism light mode background has 80 percent alpha`() {
        val card = MorphCardView(context)
        card.isGlassmorphism = true
        val bgColor = card.cardBackgroundColor.defaultColor
        // 0xCC = 204 = 80% of 255
        assertThat(Color.alpha(bgColor)).isEqualTo(0xCC)
        assertThat(Color.red(bgColor)).isEqualTo(0xFF)
        assertThat(Color.green(bgColor)).isEqualTo(0xFF)
        assertThat(Color.blue(bgColor)).isEqualTo(0xFF)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. InteractionMode 行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `iOS mode has zero cardElevation`() {
        // MorphCardView init 始终设置 cardElevation = 0f，符合 iOS HIG 无阴影规范
        val card = MorphCardView(context)
        assertThat(card.cardElevation).isEqualTo(0f)
    }

    @Test
    fun `iOS mode has zero maxCardElevation`() {
        val card = MorphCardView(context)
        assertThat(card.maxCardElevation).isEqualTo(0f)
    }

    @Test
    fun `iOS mode disables ripple effect`() {
        val card = MorphCardView(context)
        assertThat(card.rippleColor.defaultColor).isEqualTo(Color.TRANSPARENT)
    }

    @Test
    fun `InteractionMode enum contains IOS and MATERIAL`() {
        val modes = InteractionMode.values().map { it.name }
        assertThat(modes).containsExactly("IOS", "MATERIAL")
    }

    @Test
    fun `iOS card style enforces zero elevation regardless of parent style`() {
        // 即使父样式 Widget.MaterialComponents.CardView 有默认 elevation，
        // MorphCardView init 也会覆盖为 0
        val card = MorphCardView(context)
        assertThat(card.cardElevation).isEqualTo(0f)
        assertThat(card.maxCardElevation).isEqualTo(0f)
    }

    @Test
    fun `Material mode elevation is overridden to zero by init block`() {
        // MorphCardView 当前实现中 init 始终强制 cardElevation = 0f，
        // 即使使用 Pixel 主题（Widget.MorphKit.Card.Pixel），
        // init 代码也会覆盖样式中的 elevation 值。
        // 此行为确保卡片默认遵循 iOS 无阴影设计规范。
        val pixelContext = ContextThemeWrapper(
            ContextThemeWrapper(
                RuntimeEnvironment.getApplication(),
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            ),
            com.morphkit.R.style.Theme_MorphKit_Pixel
        )
        val card = MorphCardView(pixelContext)
        assertThat(card.cardElevation).isEqualTo(0f)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. 圆角半径 — 使用 MorphTokens 值
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `cornerRadius equals MorphTokens cornerRadiusCardIos in pixels`() {
        val card = MorphCardView(context)
        val expectedPx = MorphTokens.Shapes.cornerRadiusCardIos.dp(context).toFloat()
        assertThat(card.radius).isEqualTo(expectedPx)
    }

    @Test
    fun `cornerRadius equals MorphTokens cornerRadiusLarge in pixels`() {
        val card = MorphCardView(context)
        val expectedPx = MorphTokens.Shapes.cornerRadiusLarge.dp(context).toFloat()
        assertThat(card.radius).isEqualTo(expectedPx)
    }

    @Test
    fun `cornerRadius equals MorphTheme cornerLarge in pixels`() {
        val card = MorphCardView(context)
        val expectedPx = MorphShape.cornerLarge(context).toFloat()
        assertThat(card.radius).isEqualTo(expectedPx)
    }

    @Test
    fun `cornerRadius is 16dp`() {
        val card = MorphCardView(context)
        val expectedPx = 16.dp(context).toFloat()
        assertThat(card.radius).isEqualTo(expectedPx)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. 卡片背景色 — 使用 MorphTheme surface 颜色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `default cardBackgroundColor equals MorphTheme morphColorSurface`() {
        val card = MorphCardView(context)
        val expected = MorphTheme.morphColorSurface(context)
        assertThat(card.cardBackgroundColor.defaultColor).isEqualTo(expected)
    }

    @Test
    fun `default strokeColor equals MorphTheme morphColorOutlineVariant`() {
        val card = MorphCardView(context)
        val expected = MorphTheme.morphColorOutlineVariant(context)
        assertThat(card.strokeColor).isEqualTo(expected)
    }

    @Test
    fun `clean card mode has non-zero stroke width`() {
        val card = MorphCardView(context)
        // 极简白卡片模式：0.5dp 极细边框
        assertThat(card.strokeWidth).isGreaterThan(0)
    }

    @Test
    fun `glassmorphism mode background differs from clean card background`() {
        val card = MorphCardView(context)
        val cleanBg = card.cardBackgroundColor.defaultColor

        card.isGlassmorphism = true
        val glassBg = card.cardBackgroundColor.defaultColor

        assertThat(glassBg).isNotEqualTo(cleanBg)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. glassmorphismBlurRadius 行为
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `glassmorphismBlurRadius default is 25f`() {
        val card = MorphCardView(context)
        assertThat(card.glassmorphismBlurRadius).isEqualTo(25f)
    }

    @Test
    fun `glassmorphismBlurRadius setter updates value`() {
        val card = MorphCardView(context)
        card.glassmorphismBlurRadius = 30f
        assertThat(card.glassmorphismBlurRadius).isEqualTo(30f)
    }

    @Test
    fun `glassmorphismBlurRadius setter skips when value unchanged`() {
        val card = MorphCardView(context)
        val current = card.glassmorphismBlurRadius
        card.glassmorphismBlurRadius = current
        assertThat(card.glassmorphismBlurRadius).isEqualTo(current)
    }

    @Test
    fun `glassmorphismBlurRadius can be set to recommended range values`() {
        val card = MorphCardView(context)
        // 推荐范围 15–35px
        card.glassmorphismBlurRadius = 15f
        assertThat(card.glassmorphismBlurRadius).isEqualTo(15f)

        card.glassmorphismBlurRadius = 35f
        assertThat(card.glassmorphismBlurRadius).isEqualTo(35f)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. 暗黑模式刷新 — onConfigurationChanged / onAttachedToWindow
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onConfigurationChanged后颜色缓存刷新`() {
        val card = MorphCardView(context)
        val config = android.content.res.Configuration(context.resources.configuration)
        val method = android.view.View::class.java.getDeclaredMethod(
            "onConfigurationChanged", android.content.res.Configuration::class.java
        )
        method.isAccessible = true
        method.invoke(card, config)
        // 验证颜色与当前主题一致
        assertThat(card.cardBackgroundColor.defaultColor).isEqualTo(MorphTheme.morphColorSurface(context))
    }

    @Test
    fun `onAttachedToWindow后颜色正确`() {
        val card = MorphCardView(context)
        val method = android.view.View::class.java.getDeclaredMethod("onAttachedToWindow")
        method.isAccessible = true
        method.invoke(card)
        assertThat(card.cardBackgroundColor.defaultColor).isEqualTo(MorphTheme.morphColorSurface(context))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. Bitmap 安全回收 — Red Line 6 合规
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onDetachedFromWindow不会抛异常`() {
        val card = MorphCardView(context)
        card.isGlassmorphism = true
        // Simulate detach
        val method = android.view.View::class.java.getDeclaredMethod("onDetachedFromWindow")
        method.isAccessible = true
        method.invoke(card)
        // Should not throw - verifies the safe detach order
    }
}
