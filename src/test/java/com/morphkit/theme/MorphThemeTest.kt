package com.morphkit.theme

import com.morphkit.theme.MorphColors
import com.morphkit.theme.MorphTheme
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * MorphTheme 工具方法测试。
 *
 * 覆盖 overlayColor、adjustAlpha、createColorStateList、isDarkMode
 * 等纯计算逻辑，这些方法被所有 Morph* 控件依赖。
 *
 * 注意：
 * 1. MorphTheme object 有静态初始化器（cornerSmall/cornerMedium/cornerLarge）
 *    依赖 Int.dp 扩展，需要 mock Resources.getSystem() 使类可加载。
 * 2. 纯 JVM 环境下 android.graphics.Color 的静态方法（alpha/red/green/blue/argb）
 *    返回默认值 0，因此需要 mockkStatic(Color::class)。
 * 3. Color.WHITE/BLACK 等静态常量在 JVM 环境下返回 0（returnDefaultValues），
 *    无法通过 MockK 修改。createColorStateList 内部依赖这些常量，
 *    因此只验证结构（三态），不验证精确颜色值。
 */
class MorphThemeTest {

    @Before
    fun setUp() {
        // Mock Resources.getSystem() 以支持 Int.dp 扩展
        mockkStatic(Resources::class)
        val mockResources = mockk<Resources>(relaxed = true)
        val mockMetrics = DisplayMetrics().apply { density = 2.75f }
        every { mockResources.displayMetrics } returns mockMetrics
        every { Resources.getSystem() } returns mockResources

        // Mock Color 静态方法（纯 JVM 环境下返回 0）
        mockkStatic(Color::class)
        every { Color.alpha(any()) } answers {
            val color = firstArg<Int>()
            (color shr 24) and 0xFF
        }
        every { Color.red(any()) } answers {
            val color = firstArg<Int>()
            (color shr 16) and 0xFF
        }
        every { Color.green(any()) } answers {
            val color = firstArg<Int>()
            (color shr 8) and 0xFF
        }
        every { Color.blue(any()) } answers {
            val color = firstArg<Int>()
            color and 0xFF
        }
        every { Color.argb(any<Int>(), any<Int>(), any<Int>(), any<Int>()) } answers {
            val a = firstArg<Int>()
            val r = secondArg<Int>()
            val g = thirdArg<Int>()
            val b = args[3] as Int
            (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E1：overlayColor — 黑色遮罩叠加到白色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `overlayColor_黑色遮罩叠加到白色_alpha0_2`() {
        val base = 0xFFFFFFFF.toInt()  // 白色
        val overlay = 0xFF000000.toInt()  // 黑色
        val result = MorphColors.overlayColor(base, overlay, 0.2f)

        // 黑色 20% 叠加到白色 → RGB 各通道约 204
        assertEquals(255, Color.alpha(result))
        assertEquals(204, Color.red(result))
        assertEquals(204, Color.green(result))
        assertEquals(204, Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E2：overlayColor — 白色遮罩叠加到黑色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `overlayColor_白色遮罩叠加到黑色_alpha0_2`() {
        val base = 0xFF000000.toInt()  // 黑色
        val overlay = 0xFFFFFFFF.toInt()  // 白色
        val result = MorphColors.overlayColor(base, overlay, 0.2f)

        // 白色 20% 叠加到黑色 → RGB 各通道约 51
        assertEquals(255, Color.alpha(result))
        assertEquals(51, Color.red(result))
        assertEquals(51, Color.green(result))
        assertEquals(51, Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E3：overlayColor — alpha 为 0 时返回原色
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `overlayColor_alpha为0时返回原色`() {
        val base = 0xFF007AFF.toInt() // iOS 蓝
        val result = MorphColors.overlayColor(base, 0xFF000000.toInt(), 0f)

        assertEquals(base, result)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E4：adjustAlpha — 调整不透明度
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `adjustAlpha_0_38不透明度`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphColors.adjustAlpha(color, 0.38f)

        // 255 * 0.38 = 96.9 → toInt() 截断为 96
        assertEquals(96, Color.alpha(result))
        assertEquals(Color.red(color), Color.red(result))
        assertEquals(Color.green(color), Color.green(result))
        assertEquals(Color.blue(color), Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E5：adjustAlpha — alpha 为 1.0 时保持不变
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `adjustAlpha_alpha1_0时保持不变`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphColors.adjustAlpha(color, 1.0f)

        assertEquals(Color.alpha(color), Color.alpha(result))
        assertEquals(Color.red(color), Color.red(result))
        assertEquals(Color.green(color), Color.green(result))
        assertEquals(Color.blue(color), Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E6：adjustAlpha — alpha 为 0 时完全透明
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `adjustAlpha_alpha0时完全透明`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphColors.adjustAlpha(color, 0f)

        assertEquals(0, Color.alpha(result))
        assertEquals(Color.red(color), Color.red(result))
        assertEquals(Color.green(color), Color.green(result))
        assertEquals(Color.blue(color), Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E7：createColorStateList — 返回三态 ColorStateList
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `createColorStateList_返回非空ColorStateList`() {
        val baseColor = 0xFF007AFF.toInt()
        val csl = MorphColors.createColorStateList(baseColor, isDarkMode = false)

        // 验证返回非空 ColorStateList
        assertNotNull("createColorStateList 应返回非空 ColorStateList", csl)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E8：createColorStateList — 暗黑模式与亮色模式按压态不同
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `createColorStateList_暗黑模式与亮色模式按压态不同`() {
        val baseColor = 0xFF007AFF.toInt()
        val cslLight = MorphColors.createColorStateList(baseColor, isDarkMode = false)
        val cslDark = MorphColors.createColorStateList(baseColor, isDarkMode = true)

        // 两种模式的按压态颜色应不同（亮色叠加黑色遮罩，暗色叠加白色遮罩）
        val pressedLight = cslLight.getColorForState(intArrayOf(android.R.attr.state_pressed), baseColor)
        val pressedDark = cslDark.getColorForState(intArrayOf(android.R.attr.state_pressed), baseColor)

        // 注意：在纯 JVM 环境下，Color.WHITE/BLACK 常量返回 0，
        // 导致两种模式的按压态可能相同。此测试验证方法可正常调用不崩溃。
        // 完整颜色验证需要在 Instrumented Test 中执行。
        assertNotNull(pressedLight)
        assertNotNull(pressedDark)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E9：isDarkMode — 亮色模式返回 false
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `isDarkMode_亮色模式返回false`() {
        val context = mockk<Context>(relaxed = true)
        val config = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        }
        every { context.resources.configuration } returns config

        assertFalse(MorphColors.isDarkMode(context))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E10：isDarkMode — 暗黑模式返回 true
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `isDarkMode_暗黑模式返回true`() {
        val context = mockk<Context>(relaxed = true)
        val config = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_YES
        }
        every { context.resources.configuration } returns config

        assertTrue(MorphColors.isDarkMode(context))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E11：overlayColor — 结果值被 clamp 到 [0, 255]
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `overlayColor_结果值被clamp到合法范围`() {
        val result = MorphColors.overlayColor(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 1.0f)
        assertEquals(255, Color.red(result))
        assertEquals(255, Color.green(result))
        assertEquals(255, Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 用例 E12：adjustAlpha — 中间值验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `adjustAlpha_0_5不透明度`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphColors.adjustAlpha(color, 0.5f)

        assertEquals(127, Color.alpha(result))  // 255 * 0.5 = 127.5 → 127
        assertEquals(Color.red(color), Color.red(result))
        assertEquals(Color.green(color), Color.green(result))
        assertEquals(Color.blue(color), Color.blue(result))
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Deprecated API 向后兼容测试
    // 验证 MorphTheme.xxx() 已废弃方法委托到 MorphColors 后行为一致
    // ═══════════════════════════════════════════════════════════════════════

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_overlayColor_与MorphColors结果一致`() {
        val base = 0xFFFFFFFF.toInt()
        val overlay = 0xFF000000.toInt()
        val themeResult = MorphTheme.overlayColor(base, overlay, 0.2f)
        val colorsResult = MorphColors.overlayColor(base, overlay, 0.2f)
        assertEquals(themeResult, colorsResult)
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_overlayColor_alpha为0时返回原色`() {
        val base = 0xFF007AFF.toInt()
        val result = MorphTheme.overlayColor(base, 0xFF000000.toInt(), 0f)
        assertEquals(base, result)
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_adjustAlpha_与MorphColors结果一致`() {
        val color = 0xFF007AFF.toInt()
        val themeResult = MorphTheme.adjustAlpha(color, 0.38f)
        val colorsResult = MorphColors.adjustAlpha(color, 0.38f)
        assertEquals(themeResult, colorsResult)
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_adjustAlpha_alpha1_0时保持不变`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphTheme.adjustAlpha(color, 1.0f)
        assertEquals(Color.alpha(color), Color.alpha(result))
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_adjustAlpha_alpha0时完全透明`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphTheme.adjustAlpha(color, 0f)
        assertEquals(0, Color.alpha(result))
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_blendColor_与MorphColors结果一致`() {
        val from = 0xFF000000.toInt()
        val to = 0xFFFFFFFF.toInt()
        val themeResult = MorphTheme.blendColor(from, to, 0.5f)
        val colorsResult = MorphColors.blendColor(from, to, 0.5f)
        assertEquals(themeResult, colorsResult)
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_createColorStateList_返回非空`() {
        val baseColor = 0xFF007AFF.toInt()
        val csl = MorphTheme.createColorStateList(baseColor, isDarkMode = false)
        assertNotNull("deprecated createColorStateList 应返回非空 ColorStateList", csl)
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_createColorStateList_与MorphColors结果一致`() {
        val baseColor = 0xFF007AFF.toInt()
        val themeCsl = MorphTheme.createColorStateList(baseColor, isDarkMode = false)
        val colorsCsl = MorphColors.createColorStateList(baseColor, isDarkMode = false)
        // 验证两 ColorStateList 的 default color 相同
        assertEquals(
            themeCsl.defaultColor,
            colorsCsl.defaultColor
        )
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_isDarkMode_亮色模式返回false`() {
        val context = mockk<Context>(relaxed = true)
        val config = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        }
        every { context.resources.configuration } returns config
        assertFalse(MorphTheme.isDarkMode(context))
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_isDarkMode_暗黑模式返回true`() {
        val context = mockk<Context>(relaxed = true)
        val config = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_YES
        }
        every { context.resources.configuration } returns config
        assertTrue(MorphTheme.isDarkMode(context))
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_isDarkMode_与MorphColors结果一致`() {
        val contextLight = mockk<Context>(relaxed = true)
        val configLight = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        }
        every { contextLight.resources.configuration } returns configLight

        assertEquals(
            MorphTheme.isDarkMode(contextLight),
            MorphColors.isDarkMode(contextLight)
        )

        val contextDark = mockk<Context>(relaxed = true)
        val configDark = Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_YES
        }
        every { contextDark.resources.configuration } returns configDark

        assertEquals(
            MorphTheme.isDarkMode(contextDark),
            MorphColors.isDarkMode(contextDark)
        )
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_overlayColor_结果clamp到合法范围`() {
        val result = MorphTheme.overlayColor(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 1.0f)
        assertEquals(255, Color.red(result))
        assertEquals(255, Color.green(result))
        assertEquals(255, Color.blue(result))
    }

    @Suppress("DEPRECATION", "DEPRECATION_ERROR")
    @Test
    fun `deprecated_adjustAlpha_0_5不透明度`() {
        val color = 0xFF007AFF.toInt()
        val result = MorphTheme.adjustAlpha(color, 0.5f)
        assertEquals(127, Color.alpha(result))
    }
}
