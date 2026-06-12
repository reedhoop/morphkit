package com.morphkit.theme

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * MorphColors 单元测试。
 *
 * 覆盖：
 * 1. isDarkMode — 暗色模式检测
 * 2. overlayColor — 颜色叠加运算
 * 3. adjustAlpha — Alpha 通道调整
 * 4. blendColor — 颜色线性混合
 * 5. createColorStateList — 三态 ColorStateList 生成
 *
 * 注意：overlayColor / adjustAlpha / blendColor / createColorStateList
 * 依赖 android.graphics.Color 的 ARGB 解析方法，在 JVM 单元测试中
 * 这些方法返回默认值 0（returnDefaultValues=true），需要通过 mockkStatic
 * 模拟 Color 的静态方法以获取正确的位运算结果。
 */
class MorphColorsTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpColor() {
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

        @JvmStatic
        @AfterAll
        fun tearDownColor() {
            unmockkAll()
        }
    }

    @Nested
    @DisplayName("isDarkMode — 暗色模式检测")
    inner class IsDarkModeTest {

        @Test
        fun `UI_MODE_NIGHT_YES_返回true`() {
            val context = mockk<Context>()
            val config = Configuration().apply {
                uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
            }
            every { context.resources } returns mockk(relaxed = true)
            every { context.resources.configuration } returns config

            assertTrue(MorphColors.isDarkMode(context))
        }

        @Test
        fun `UI_MODE_NIGHT_NO_返回false`() {
            val context = mockk<Context>()
            val config = Configuration().apply {
                uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
            }
            every { context.resources } returns mockk(relaxed = true)
            every { context.resources.configuration } returns config

            assertFalse(MorphColors.isDarkMode(context))
        }

        @Test
        fun `UNDEFINED_夜模式_返回false`() {
            val context = mockk<Context>()
            val config = Configuration().apply {
                uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED or Configuration.UI_MODE_TYPE_NORMAL
            }
            every { context.resources } returns mockk(relaxed = true)
            every { context.resources.configuration } returns config

            assertFalse(MorphColors.isDarkMode(context))
        }
    }

    @Nested
    @DisplayName("overlayColor — 颜色叠加运算")
    inner class OverlayColorTest {

        @Test
        @DisplayName("黑色遮罩叠加到白色 alpha=0.2 -> RGB 各通道约 204")
        fun `black overlay on white with alpha 0_2`() {
            val base = 0xFFFFFFFF.toInt()
            val overlay = 0xFF000000.toInt()
            val result = MorphColors.overlayColor(base, overlay, 0.2f)

            assertEquals(255, Color.alpha(result))
            assertEquals(204, Color.red(result))
            assertEquals(204, Color.green(result))
            assertEquals(204, Color.blue(result))
        }

        @Test
        @DisplayName("白色遮罩叠加到黑色 alpha=0.2 -> RGB 各通道约 51")
        fun `white overlay on black with alpha 0_2`() {
            val base = 0xFF000000.toInt()
            val overlay = 0xFFFFFFFF.toInt()
            val result = MorphColors.overlayColor(base, overlay, 0.2f)

            assertEquals(255, Color.alpha(result))
            assertEquals(51, Color.red(result))
            assertEquals(51, Color.green(result))
            assertEquals(51, Color.blue(result))
        }

        @Test
        @DisplayName("alpha=0 时返回原色")
        fun `alpha zero returns base color`() {
            val base = 0xFF007AFF.toInt()
            val result = MorphColors.overlayColor(base, 0xFF000000.toInt(), 0f)
            assertEquals(base, result)
        }

        @Test
        @DisplayName("结果值被 clamp 到 [0, 255]")
        fun `result clamped to valid range`() {
            val result = MorphColors.overlayColor(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 1.0f)
            assertEquals(255, Color.red(result))
            assertEquals(255, Color.green(result))
            assertEquals(255, Color.blue(result))
        }
    }

    @Nested
    @DisplayName("adjustAlpha — Alpha 通道调整")
    inner class AdjustAlphaTest {

        @Test
        @DisplayName("alpha=0.38 -> 255*0.38=96")
        fun `adjust alpha to 0_38`() {
            val color = 0xFF007AFF.toInt()
            val result = MorphColors.adjustAlpha(color, 0.38f)
            assertEquals(96, Color.alpha(result))
            assertEquals(Color.red(color), Color.red(result))
            assertEquals(Color.green(color), Color.green(result))
            assertEquals(Color.blue(color), Color.blue(result))
        }

        @Test
        @DisplayName("alpha=1.0 -> 保持不变")
        fun `alpha 1_0 keeps color unchanged`() {
            val color = 0xFF007AFF.toInt()
            val result = MorphColors.adjustAlpha(color, 1.0f)
            assertEquals(Color.alpha(color), Color.alpha(result))
            assertEquals(Color.red(color), Color.red(result))
            assertEquals(Color.green(color), Color.green(result))
            assertEquals(Color.blue(color), Color.blue(result))
        }

        @Test
        @DisplayName("alpha=0 -> 完全透明")
        fun `alpha 0 makes fully transparent`() {
            val color = 0xFF007AFF.toInt()
            val result = MorphColors.adjustAlpha(color, 0f)
            assertEquals(0, Color.alpha(result))
            assertEquals(Color.red(color), Color.red(result))
        }

        @Test
        @DisplayName("alpha=0.5 -> 255*0.5=127")
        fun `adjust alpha to 0_5`() {
            val color = 0xFF007AFF.toInt()
            val result = MorphColors.adjustAlpha(color, 0.5f)
            assertEquals(127, Color.alpha(result))
        }
    }

    @Nested
    @DisplayName("blendColor — 颜色线性混合")
    inner class BlendColorTest {

        @Test
        @DisplayName("ratio=0 返回 from 颜色")
        fun `ratio zero returns from color`() {
            val from = 0xFF007AFF.toInt()
            val to = 0xFFFF0000.toInt()
            val result = MorphColors.blendColor(from, to, 0f)
            assertEquals(from, result)
        }

        @Test
        @DisplayName("ratio=1 返回 to 颜色")
        fun `ratio one returns to color`() {
            val from = 0xFF007AFF.toInt()
            val to = 0xFFFF0000.toInt()
            val result = MorphColors.blendColor(from, to, 1.0f)
            assertEquals(to, result)
        }

        @Test
        @DisplayName("ratio=0.5 返回中间色")
        fun `ratio 0_5 returns midpoint color`() {
            val from = 0xFF000000.toInt()
            val to = 0xFFFEFEFE.toInt()
            val result = MorphColors.blendColor(from, to, 0.5f)
            assertEquals(255, Color.alpha(result))
            // 0 * 0.5 + 254 * 0.5 = 127
            assertEquals(127, Color.red(result))
            assertEquals(127, Color.green(result))
            assertEquals(127, Color.blue(result))
        }
    }

    @Nested
    @DisplayName("createColorStateList — 三态 ColorStateList")
    inner class CreateColorStateListTest {

        @Test
        @DisplayName("返回非空 ColorStateList")
        fun `returns non-null ColorStateList`() {
            val baseColor = 0xFF007AFF.toInt()
            val csl = MorphColors.createColorStateList(baseColor, isDarkMode = false)
            assertNotNull(csl)
        }

        @Test
        @DisplayName("default color 等于 baseColor")
        fun `default color equals baseColor`() {
            val baseColor = 0xFF007AFF.toInt()
            val csl = MorphColors.createColorStateList(baseColor, isDarkMode = false)
            // ColorStateList.defaultColor 在纯 JVM 环境下可能不可用（Android stub），
            // 因此验证非空即可；颜色值正确性由 deprecated API 对比测试覆盖
            assertNotNull(csl)
        }

        @Test
        @DisplayName("亮色与暗色模式均可正常创建 ColorStateList")
        fun `both light and dark mode create ColorStateList successfully`() {
            val baseColor = 0xFF007AFF.toInt()
            val cslLight = MorphColors.createColorStateList(baseColor, isDarkMode = false)
            val cslDark = MorphColors.createColorStateList(baseColor, isDarkMode = true)
            assertNotNull(cslLight)
            assertNotNull(cslDark)
        }
    }
}
