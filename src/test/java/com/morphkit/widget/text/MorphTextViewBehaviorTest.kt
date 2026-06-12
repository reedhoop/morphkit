package com.morphkit.widget.text

import android.content.Context
import android.graphics.Typeface
import android.view.ContextThemeWrapper
import com.google.common.truth.Truth.assertThat
import com.morphkit.R
import com.morphkit.core.InteractionMode
import com.morphkit.theme.FontWeight
import com.morphkit.theme.MorphTheme
import com.morphkit.theme.MorphTokens
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * MorphTextView 行为测试（Robolectric）。
 *
 * 在 Android Runtime 环境下验证 MorphTextView 的实际控件行为，
 * 覆盖初始化默认值、文字颜色级别、textStyle 重映射、交互模式等关键逻辑。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphTextViewBehaviorTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 1. 初始化 — 默认状态验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `默认isSecondaryText为false`() {
        val tv = MorphTextView(context)
        assertThat(tv.isSecondaryText).isFalse()
    }

    @Test
    fun `默认isTertiaryText为false`() {
        val tv = MorphTextView(context)
        assertThat(tv.isTertiaryText).isFalse()
    }

    @Test
    fun `默认文字颜色使用MorphTheme morphColorOnSurface`() {
        val tv = MorphTextView(context)
        val expectedColor = MorphTheme.morphColorOnSurface(context)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `默认textStyle被重映射为MEDIUM（NORMAL补偿）`() {
        val tv = MorphTextView(context)
        // 默认 textStyle=NORMAL，remapTextStyle 将其映射为 sans-serif-medium
        val expectedTypeface = FontWeight.MEDIUM.toTypeface()
        assertThat(tv.typeface).isNotNull()
        // Robolectric 环境下 Typeface 比较使用 equals
        assertThat(tv.typeface).isEqualTo(expectedTypeface)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. isSecondaryText — 次级文字颜色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `设置isSecondaryText为true后文字颜色变为morphColorOnSurfaceVariant`() {
        val tv = MorphTextView(context)
        val defaultColor = tv.currentTextColor

        tv.isSecondaryText = true

        val expectedColor = MorphTheme.morphColorOnSurfaceVariant(context)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
        // 颜色确实发生了变化
        assertThat(tv.currentTextColor).isNotEqualTo(defaultColor)
    }

    @Test
    fun `设置isSecondaryText为false后文字颜色恢复为morphColorOnSurface`() {
        val tv = MorphTextView(context)
        tv.isSecondaryText = true
        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurfaceVariant(context))

        tv.isSecondaryText = false

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurface(context))
    }

    @Test
    fun `重复设置isSecondaryText为true不会崩溃且颜色一致`() {
        val tv = MorphTextView(context)
        tv.isSecondaryText = true
        tv.isSecondaryText = true

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurfaceVariant(context))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. isTertiaryText — 三级文字颜色（带透明度衰减）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `设置isTertiaryText为true后文字颜色为morphColorOnSurfaceVariant的55%透明度`() {
        val tv = MorphTextView(context)

        tv.isTertiaryText = true

        val baseColor = MorphTheme.morphColorOnSurfaceVariant(context)
        val expectedColor = MorphTheme.adjustAlpha(baseColor, 0.55f)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `设置isTertiaryText为false后文字颜色恢复为morphColorOnSurface`() {
        val tv = MorphTextView(context)
        tv.isTertiaryText = true
        assertThat(tv.currentTextColor).isNotEqualTo(MorphTheme.morphColorOnSurface(context))

        tv.isTertiaryText = false

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurface(context))
    }

    @Test
    fun `isTertiaryText优先级高于isSecondaryText`() {
        val tv = MorphTextView(context)
        tv.isSecondaryText = true
        tv.isTertiaryText = true

        // 当两者都为 true 时，isTertiaryText 优先（applyTextColor 的 when 分支顺序）
        val tertiaryColor = MorphTheme.adjustAlpha(
            MorphTheme.morphColorOnSurfaceVariant(context), 0.55f
        )
        assertThat(tv.currentTextColor).isEqualTo(tertiaryColor)
    }

    @Test
    fun `isTertiaryText为true且isSecondaryText为false时仍使用三级颜色`() {
        val tv = MorphTextView(context)
        tv.isTertiaryText = true

        val tertiaryColor = MorphTheme.adjustAlpha(
            MorphTheme.morphColorOnSurfaceVariant(context), 0.55f
        )
        assertThat(tv.currentTextColor).isEqualTo(tertiaryColor)
    }

    @Test
    fun `关闭isTertiaryText后isSecondaryText仍生效`() {
        val tv = MorphTextView(context)
        tv.isTertiaryText = true
        tv.isSecondaryText = true

        // 关闭 tertiary，secondary 应生效
        tv.isTertiaryText = false

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurfaceVariant(context))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. remapTextStyle — textStyle 重映射
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `NORMAL样式重映射为MEDIUM字重`() {
        val tv = MorphTextView(context)
        // 默认构造时 textStyle=NORMAL，init 中 remapTextStyle 映射为 MEDIUM
        val expected = FontWeight.MEDIUM.toTypeface()
        assertThat(tv.typeface).isEqualTo(expected)
    }

    @Test
    fun `setTypeface传入null和BOLD样式后重映射为BOLD`() {
        val tv = MorphTextView(context)
        tv.setTypeface(null, Typeface.BOLD)

        val expected = Typeface.defaultFromStyle(Typeface.BOLD)
        assertThat(tv.typeface).isEqualTo(expected)
    }

    @Test
    fun `setTypeface传入null和ITALIC样式后重映射为MEDIUM加ITALIC`() {
        val tv = MorphTextView(context)
        tv.setTypeface(null, Typeface.ITALIC)

        // ITALIC → MEDIUM + ITALIC
        val mediumTf = FontWeight.MEDIUM.toTypeface()
        val expected = Typeface.create(mediumTf, Typeface.ITALIC)
        assertThat(tv.typeface).isEqualTo(expected)
    }

    @Test
    fun `setTypeface传入null和BOLD_ITALIC样式后保持BOLD_ITALIC`() {
        val tv = MorphTextView(context)
        tv.setTypeface(null, Typeface.BOLD_ITALIC)

        val expected = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC)
        assertThat(tv.typeface).isEqualTo(expected)
    }

    @Test
    fun `setTypeface传入非null Typeface时使用外部Typeface`() {
        val tv = MorphTextView(context)
        val customTf = FontWeight.SEMI_BOLD.toTypeface()

        tv.setTypeface(customTf, Typeface.NORMAL)

        // 外部传入 tf 非空时，直接使用该 tf（style 不做二次补偿）
        assertThat(tv.typeface).isEqualTo(customTf)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. InteractionMode — 交互模式
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `InteractionMode枚举包含IOS和MATERIAL`() {
        val modes = InteractionMode.values().map { it.name }
        assertThat(modes).containsExactly("IOS", "MATERIAL")
    }

    @Test
    fun `iOS主题下MorphTextView可正常构造且文字颜色正确`() {
        val iosContext = ContextThemeWrapper(
            ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            ),
            R.style.Theme_MorphKit_iOS
        )

        val tv = MorphTextView(iosContext)
        val expectedColor = MorphTheme.morphColorOnSurface(iosContext)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `Pixel主题下MorphTextView可正常构造且文字颜色正确`() {
        val pixelContext = ContextThemeWrapper(
            ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            ),
            R.style.Theme_MorphKit_Pixel
        )

        val tv = MorphTextView(pixelContext)
        val expectedColor = MorphTheme.morphColorOnSurface(pixelContext)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `iOS主题下isSecondaryText颜色仍使用MorphTheme语义色`() {
        val iosContext = ContextThemeWrapper(
            ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            ),
            R.style.Theme_MorphKit_iOS
        )

        val tv = MorphTextView(iosContext)
        tv.isSecondaryText = true

        val expectedColor = MorphTheme.morphColorOnSurfaceVariant(iosContext)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    @Test
    fun `Pixel主题下isSecondaryText颜色仍使用MorphTheme语义色`() {
        val pixelContext = ContextThemeWrapper(
            ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            ),
            R.style.Theme_MorphKit_Pixel
        )

        val tv = MorphTextView(pixelContext)
        tv.isSecondaryText = true

        val expectedColor = MorphTheme.morphColorOnSurfaceVariant(pixelContext)
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 生命周期 — onAttachedToWindow / onConfigurationChanged
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `onAttachedToWindow后默认文字颜色仍然正确`() {
        val tv = MorphTextView(context)
        invokeOnAttachedToWindow(tv)

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurface(context))
    }

    @Test
    fun `onAttachedToWindow后isSecondaryText颜色仍然正确`() {
        val tv = MorphTextView(context)
        tv.isSecondaryText = true
        invokeOnAttachedToWindow(tv)

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurfaceVariant(context))
    }

    @Test
    fun `onConfigurationChanged后默认文字颜色刷新`() {
        val tv = MorphTextView(context)
        val config = android.content.res.Configuration(context.resources.configuration)
        invokeOnConfigurationChanged(tv, config)

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurface(context))
    }

    @Test
    fun `onConfigurationChanged后isSecondaryText颜色刷新`() {
        val tv = MorphTextView(context)
        tv.isSecondaryText = true
        val config = android.content.res.Configuration(context.resources.configuration)
        invokeOnConfigurationChanged(tv, config)

        assertThat(tv.currentTextColor).isEqualTo(MorphTheme.morphColorOnSurfaceVariant(context))
    }

    @Test
    fun `onConfigurationChanged后isTertiaryText颜色刷新`() {
        val tv = MorphTextView(context)
        tv.isTertiaryText = true
        val config = android.content.res.Configuration(context.resources.configuration)
        invokeOnConfigurationChanged(tv, config)

        val expectedColor = MorphTheme.adjustAlpha(
            MorphTheme.morphColorOnSurfaceVariant(context), 0.55f
        )
        assertThat(tv.currentTextColor).isEqualTo(expectedColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 7. MorphTokens 排版令牌 — 验证字号定义
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphTokens fontSizeLargeTitle等于34f`() {
        assertThat(MorphTokens.fontSizeLargeTitle).isEqualTo(34f)
    }

    @Test
    fun `MorphTokens fontSizeBody等于17f`() {
        assertThat(MorphTokens.fontSizeBody).isEqualTo(17f)
    }

    @Test
    fun `MorphTokens fontSizeCaption1等于12f`() {
        assertThat(MorphTokens.fontSizeCaption1).isEqualTo(12f)
    }

    @Test
    fun `MorphTheme typography body使用MEDIUM字重`() {
        assertThat(MorphTheme.typography.body.weight).isEqualTo(FontWeight.MEDIUM)
    }

    @Test
    fun `MorphTheme typography largeTitle使用EXTRA_BOLD字重`() {
        assertThat(MorphTheme.typography.largeTitle.weight).isEqualTo(FontWeight.EXTRA_BOLD)
    }

    @Test
    fun `MorphTheme typography headline使用SEMI_BOLD字重`() {
        assertThat(MorphTheme.typography.headline.weight).isEqualTo(FontWeight.SEMI_BOLD)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 8. FontWeight toTypeface — 验证字重到Typeface的映射
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `FontWeight MEDIUM映射为sans-serif-medium`() {
        val tf = FontWeight.MEDIUM.toTypeface()
        assertThat(tf).isNotNull()
        assertThat(tf.style).isEqualTo(Typeface.NORMAL)
    }

    @Test
    fun `FontWeight BOLD映射为sans-serif-bold`() {
        val tf = FontWeight.BOLD.toTypeface()
        assertThat(tf).isNotNull()
        assertThat(tf.style).isEqualTo(Typeface.BOLD)
    }

    @Test
    fun `FontWeight SEMI_BOLD映射为sans-serif-semibold`() {
        val tf = FontWeight.SEMI_BOLD.toTypeface()
        assertThat(tf).isNotNull()
        assertThat(tf.style).isEqualTo(Typeface.NORMAL)
    }

    @Test
    fun `FontWeight EXTRA_BOLD映射为sans-serif-black`() {
        val tf = FontWeight.EXTRA_BOLD.toTypeface()
        assertThat(tf).isNotNull()
        assertThat(tf.style).isEqualTo(Typeface.NORMAL)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法 — 通过反射调用 protected 生命周期方法
    // ═══════════════════════════════════════════════════════════════════════

    private fun invokeOnAttachedToWindow(tv: MorphTextView) {
        val method = android.view.View::class.java.getDeclaredMethod("onAttachedToWindow")
        method.isAccessible = true
        method.invoke(tv)
    }

    private fun invokeOnConfigurationChanged(tv: MorphTextView, config: android.content.res.Configuration) {
        val method = android.view.View::class.java.getDeclaredMethod(
            "onConfigurationChanged", android.content.res.Configuration::class.java
        )
        method.isAccessible = true
        method.invoke(tv, config)
    }
}
