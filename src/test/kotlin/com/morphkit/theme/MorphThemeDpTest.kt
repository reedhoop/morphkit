package com.morphkit.theme

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * MorphTheme dp 转换与圆角常量的单元测试。
 *
 * 覆盖：
 * 1. [Int.dp] 在不同屏幕密度下的像素转换正确性
 * 2. [Float.dp] 在不同屏幕密度下的像素转换正确性
 * 3. [MorphTheme.cornerSmall] / [MorphTheme.cornerMedium] / [MorphTheme.cornerLarge] 圆角值正确性
 * 4. [MorphTheme.cornerFull] 常量值
 *
 * ## 测试策略
 *
 * 使用 MockK 构建带有不同 [DisplayMetrics.density] 的 mock [Context]，
 * 验证 dp 转换函数在 mdpi(1.0)、xhdpi(2.0)、xxhdpi(3.0)、xxxhdpi(4.0) 下的结果。
 *
 * dp → px 公式：`px = dp × density + 0.5`（TypedValue.applyDimension 内部实现）
 */
class MorphThemeDpTest {

    /** 不同密度的 mock Context 缓存 */
    private lateinit var mdpiContext: Context      // density = 1.0
    private lateinit var xhdpiContext: Context      // density = 2.0
    private lateinit var xxhdpiContext: Context     // density = 3.0
    private lateinit var xxxhdpiContext: Context    // density = 4.0

    @BeforeEach
    fun setUp() {
        mdpiContext = createContextWithDensity(1.0f)
        xhdpiContext = createContextWithDensity(2.0f)
        xxhdpiContext = createContextWithDensity(3.0f)
        xxxhdpiContext = createContextWithDensity(4.0f)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Int.dp(context) 测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Int.dp(context) — 整型 dp 转像素")
    inner class IntDpTest {

        @Test
        @DisplayName("mdpi(1.0): 16dp = 16px")
        fun `mdpi — 16dp equals 16px`() {
            assertEquals(16, 16.dp(mdpiContext))
        }

        @Test
        @DisplayName("xhdpi(2.0): 16dp = 32px")
        fun `xhdpi — 16dp equals 32px`() {
            assertEquals(32, 16.dp(xhdpiContext))
        }

        @Test
        @DisplayName("xxhdpi(3.0): 16dp = 48px")
        fun `xxhdpi — 16dp equals 48px`() {
            assertEquals(48, 16.dp(xxhdpiContext))
        }

        @Test
        @DisplayName("xxxhdpi(4.0): 16dp = 64px")
        fun `xxxhdpi — 16dp equals 64px`() {
            assertEquals(64, 16.dp(xxxhdpiContext))
        }

        @Test
        @DisplayName("0dp 在任何密度下均为 0px")
        fun `zero dp returns 0px at any density`() {
            assertEquals(0, 0.dp(mdpiContext))
            assertEquals(0, 0.dp(xhdpiContext))
            assertEquals(0, 0.dp(xxhdpiContext))
            assertEquals(0, 0.dp(xxxhdpiContext))
        }

        @Test
        @DisplayName("1dp 在不同密度下的结果")
        fun `1dp at various densities`() {
            assertEquals(1, 1.dp(mdpiContext))
            assertEquals(2, 1.dp(xhdpiContext))
            assertEquals(3, 1.dp(xxhdpiContext))
            assertEquals(4, 1.dp(xxxhdpiContext))
        }

        @Test
        @DisplayName("大值 100dp 在 xxhdpi 下正确转换")
        fun `large value — 100dp at xxhdpi`() {
            assertEquals(300, 100.dp(xxhdpiContext))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Float.dp(context) 测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Float.dp(context) — 浮点型 dp 转像素")
    inner class FloatDpTest {

        @Test
        @DisplayName("mdpi(1.0): 12.5dp = 12.5px")
        fun `mdpi — 12_5dp equals 12_5px`() {
            assertEquals(12.5f, 12.5f.dp(mdpiContext), 0.01f)
        }

        @Test
        @DisplayName("xhdpi(2.0): 12.5dp = 25.0px")
        fun `xhdpi — 12_5dp equals 25px`() {
            assertEquals(25.0f, 12.5f.dp(xhdpiContext), 0.01f)
        }

        @Test
        @DisplayName("xxhdpi(3.0): 12.5dp = 37.5px")
        fun `xxhdpi — 12_5dp equals 37_5px`() {
            assertEquals(37.5f, 12.5f.dp(xxhdpiContext), 0.01f)
        }

        @Test
        @DisplayName("0.0dp 在任何密度下均为 0.0px")
        fun `zero dp returns 0px at any density`() {
            assertEquals(0.0f, 0.0f.dp(mdpiContext), 0.01f)
            assertEquals(0.0f, 0.0f.dp(xhdpiContext), 0.01f)
            assertEquals(0.0f, 0.0f.dp(xxhdpiContext), 0.01f)
        }

        @Test
        @DisplayName("小数 dp 值在 xhdpi 下正确转换")
        fun `fractional dp at xhdpi`() {
            assertEquals(1.5f, 0.75f.dp(xhdpiContext), 0.01f)
            assertEquals(5.0f, 2.5f.dp(xhdpiContext), 0.01f)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cornerSmall / cornerMedium / cornerLarge 测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cornerSmall/Medium/Large — 圆角常量")
    inner class CornerTest {

        @Test
        @DisplayName("cornerSmall = 8dp，在各密度下像素值正确")
        fun `cornerSmall is 8dp`() {
            assertEquals(8.dp(mdpiContext), MorphTheme.cornerSmall(mdpiContext))
            assertEquals(8.dp(xhdpiContext), MorphTheme.cornerSmall(xhdpiContext))
            assertEquals(8.dp(xxhdpiContext), MorphTheme.cornerSmall(xxhdpiContext))
        }

        @Test
        @DisplayName("cornerMedium = 12dp，在各密度下像素值正确")
        fun `cornerMedium is 12dp`() {
            assertEquals(12.dp(mdpiContext), MorphTheme.cornerMedium(mdpiContext))
            assertEquals(12.dp(xhdpiContext), MorphTheme.cornerMedium(xhdpiContext))
            assertEquals(12.dp(xxhdpiContext), MorphTheme.cornerMedium(xxhdpiContext))
        }

        @Test
        @DisplayName("cornerLarge = 16dp，在各密度下像素值正确")
        fun `cornerLarge is 16dp`() {
            assertEquals(16.dp(mdpiContext), MorphTheme.cornerLarge(mdpiContext))
            assertEquals(16.dp(xhdpiContext), MorphTheme.cornerLarge(xhdpiContext))
            assertEquals(16.dp(xxhdpiContext), MorphTheme.cornerLarge(xxhdpiContext))
        }

        @Test
        @DisplayName("cornerFull = Int.MAX_VALUE（与密度无关）")
        fun `cornerFull is MAX_VALUE`() {
            assertEquals(Int.MAX_VALUE, MorphTheme.cornerFull)
        }

        @Test
        @DisplayName("xhdpi 下三个圆角的具体像素值")
        fun `concrete pixel values at xhdpi`() {
            assertEquals(16, MorphTheme.cornerSmall(xhdpiContext), "cornerSmall@xhdpi = 8×2 = 16px")
            assertEquals(24, MorphTheme.cornerMedium(xhdpiContext), "cornerMedium@xhdpi = 12×2 = 24px")
            assertEquals(32, MorphTheme.cornerLarge(xhdpiContext), "cornerLarge@xhdpi = 16×2 = 32px")
        }

        @Test
        @DisplayName("xxhdpi 下三个圆角的具体像素值")
        fun `concrete pixel values at xxhdpi`() {
            assertEquals(24, MorphTheme.cornerSmall(xxhdpiContext), "cornerSmall@xxhdpi = 8×3 = 24px")
            assertEquals(36, MorphTheme.cornerMedium(xxhdpiContext), "cornerMedium@xxhdpi = 12×3 = 36px")
            assertEquals(48, MorphTheme.cornerLarge(xxhdpiContext), "cornerLarge@xxhdpi = 16×3 = 48px")
        }

        @Test
        @DisplayName("圆角大小顺序: small < medium < large")
        fun `corner ordering is preserved at all densities`() {
            listOf(mdpiContext, xhdpiContext, xxhdpiContext, xxxhdpiContext).forEach { ctx ->
                val small = MorphTheme.cornerSmall(ctx)
                val medium = MorphTheme.cornerMedium(ctx)
                val large = MorphTheme.cornerLarge(ctx)
                assert(small < medium) { "cornerSmall($small) should < cornerMedium($medium)" }
                assert(medium < large) { "cornerMedium($medium) should < cornerLarge($large)" }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Context-aware 验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Context-aware — 不同 Context 返回不同结果")
    inner class ContextAwareTest {

        @Test
        @DisplayName("同一 dp 值在不同密度 Context 下返回不同像素值")
        fun `same dp value yields different pixels on different density contexts`() {
            val dpValue = 10
            val mdpiResult = dpValue.dp(mdpiContext)
            val xhdpiResult = dpValue.dp(xhdpiContext)

            assertNotEquals(mdpiResult, xhdpiResult,
                "同一 10dp 在 mdpi 和 xhdpi 下应产生不同像素值")
            assertEquals(10, mdpiResult)
            assertEquals(20, xhdpiResult)
        }

        @Test
        @DisplayName("cornerMedium 在不同密度 Context 下返回不同像素值")
        fun `cornerMedium returns different pixels on different contexts`() {
            val mdpiResult = MorphTheme.cornerMedium(mdpiContext)
            val xxhdpiResult = MorphTheme.cornerMedium(xxhdpiContext)

            assertNotEquals(mdpiResult, xxhdpiResult,
                "cornerMedium 在 mdpi 和 xxhdpi 下应产生不同像素值")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 创建具有指定屏幕密度的 mock [Context]。
     *
     * 构建 mock 链：Context → Resources → DisplayMetrics，
     * 使 `context.resources.displayMetrics.density` 返回指定值。
     *
     * @param density 屏幕密度因子（1.0=mdpi, 2.0=xhdpi, 3.0=xxhdpi, 4.0=xxxhdpi）
     */
    private fun createContextWithDensity(density: Float): Context {
        val displayMetrics = DisplayMetrics().apply {
            this.density = density
            this.densityDpi = (density * 160).toInt()
            this.scaledDensity = density
        }
        val resources = mockk<Resources>()
        every { resources.displayMetrics } returns displayMetrics

        val configuration = Configuration()
        every { resources.configuration } returns configuration

        val context = mockk<Context>()
        every { context.resources } returns resources

        return context
    }

    private fun assertNotEquals(expected: Int, actual: Int, message: String) {
        if (expected == actual) {
            throw AssertionError("$message: expected not equal but both were $expected")
        }
    }
}
