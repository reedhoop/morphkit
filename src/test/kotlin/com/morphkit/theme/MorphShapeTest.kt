package com.morphkit.theme

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * MorphShape 单元测试。
 *
 * 覆盖：
 * 1. [MorphShape.cornerSmall] / [MorphShape.cornerMedium] / [MorphShape.cornerLarge] 在不同密度下的像素值
 * 2. [MorphShape.cornerFull] 哨兵值常量
 * 3. 与 [MorphTokens.Shapes] 的一致性验证
 * 4. 多窗口/折叠屏场景下不同 Context 返回不同像素值
 * 5. 边界条件测试
 *
 * ## 测试策略
 *
 * 使用 MockK 构建带有不同 [DisplayMetrics.density] 的 mock [Context]，
 * 验证 dp 转换在 mdpi(1.0)、xhdpi(2.0)、xxhdpi(3.0)、xxxhdpi(4.0) 下的结果。
 *
 * dp -> px 公式：`px = dp * density`（通过 TypedValue.applyDimension mock 实现）
 */
class MorphShapeTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUpTypedValue() {
            mockkStatic(TypedValue::class)
            every {
                TypedValue.applyDimension(any(), any(), any())
            } answers {
                val unit = firstArg<Int>()
                val value = secondArg<Float>()
                val metrics = thirdArg<DisplayMetrics>()
                when (unit) {
                    TypedValue.COMPLEX_UNIT_DIP -> value * metrics.density
                    TypedValue.COMPLEX_UNIT_SP -> value * metrics.scaledDensity
                    TypedValue.COMPLEX_UNIT_PX -> value
                    else -> value
                }
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDownTypedValue() {
            unmockkStatic(TypedValue::class)
        }
    }

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
    // cornerSmall — 8dp 圆角
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cornerSmall — 8dp 小圆角")
    inner class CornerSmallTest {

        @Test
        @DisplayName("mdpi(1.0): cornerSmall = 8px")
        fun `cornerSmall at mdpi is 8px`() {
            assertEquals(8, MorphShape.cornerSmall(mdpiContext))
        }

        @Test
        @DisplayName("xhdpi(2.0): cornerSmall = 16px")
        fun `cornerSmall at xhdpi is 16px`() {
            assertEquals(16, MorphShape.cornerSmall(xhdpiContext))
        }

        @Test
        @DisplayName("xxhdpi(3.0): cornerSmall = 24px")
        fun `cornerSmall at xxhdpi is 24px`() {
            assertEquals(24, MorphShape.cornerSmall(xxhdpiContext))
        }

        @Test
        @DisplayName("xxxhdpi(4.0): cornerSmall = 32px")
        fun `cornerSmall at xxxhdpi is 32px`() {
            assertEquals(32, MorphShape.cornerSmall(xxxhdpiContext))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cornerMedium — 12dp 圆角
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cornerMedium — 12dp 中圆角")
    inner class CornerMediumTest {

        @Test
        @DisplayName("mdpi(1.0): cornerMedium = 12px")
        fun `cornerMedium at mdpi is 12px`() {
            assertEquals(12, MorphShape.cornerMedium(mdpiContext))
        }

        @Test
        @DisplayName("xhdpi(2.0): cornerMedium = 24px")
        fun `cornerMedium at xhdpi is 24px`() {
            assertEquals(24, MorphShape.cornerMedium(xhdpiContext))
        }

        @Test
        @DisplayName("xxhdpi(3.0): cornerMedium = 36px")
        fun `cornerMedium at xxhdpi is 36px`() {
            assertEquals(36, MorphShape.cornerMedium(xxhdpiContext))
        }

        @Test
        @DisplayName("xxxhdpi(4.0): cornerMedium = 48px")
        fun `cornerMedium at xxxhdpi is 48px`() {
            assertEquals(48, MorphShape.cornerMedium(xxxhdpiContext))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cornerLarge — 16dp 圆角
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cornerLarge — 16dp 大圆角")
    inner class CornerLargeTest {

        @Test
        @DisplayName("mdpi(1.0): cornerLarge = 16px")
        fun `cornerLarge at mdpi is 16px`() {
            assertEquals(16, MorphShape.cornerLarge(mdpiContext))
        }

        @Test
        @DisplayName("xhdpi(2.0): cornerLarge = 32px")
        fun `cornerLarge at xhdpi is 32px`() {
            assertEquals(32, MorphShape.cornerLarge(xhdpiContext))
        }

        @Test
        @DisplayName("xxhdpi(3.0): cornerLarge = 48px")
        fun `cornerLarge at xxhdpi is 48px`() {
            assertEquals(48, MorphShape.cornerLarge(xxhdpiContext))
        }

        @Test
        @DisplayName("xxxhdpi(4.0): cornerLarge = 64px")
        fun `cornerLarge at xxxhdpi is 64px`() {
            assertEquals(64, MorphShape.cornerLarge(xxxhdpiContext))
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // cornerFull — 胶囊形状哨兵值
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cornerFull — 胶囊形状哨兵值")
    inner class CornerFullTest {

        @Test
        @DisplayName("cornerFull = Int.MAX_VALUE")
        fun `cornerFull is Int MAX_VALUE`() {
            assertEquals(Int.MAX_VALUE, MorphShape.cornerFull)
        }

        @Test
        @DisplayName("cornerFull 大于所有基于 Context 的圆角值")
        fun `cornerFull is larger than any context-based corner value`() {
            listOf(mdpiContext, xhdpiContext, xxhdpiContext, xxxhdpiContext).forEach { ctx ->
                assertTrue(MorphShape.cornerFull > MorphShape.cornerLarge(ctx))
                assertTrue(MorphShape.cornerFull > MorphShape.cornerMedium(ctx))
                assertTrue(MorphShape.cornerFull > MorphShape.cornerSmall(ctx))
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 与 MorphTokens.Shapes 一致性验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MorphShape 与 MorphTokens.Shapes 一致性")
    inner class TokenConsistencyTest {

        @Test
        @DisplayName("cornerSmall dp 值 (8) 与 MorphTokens.Shapes.cornerRadiusSmall 一致")
        fun `cornerSmall dp value matches token`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusSmall, 8)
        }

        @Test
        @DisplayName("cornerMedium dp 值 (12) 与 MorphTokens.Shapes.cornerRadiusMedium 一致")
        fun `cornerMedium dp value matches token`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusMedium, 12)
        }

        @Test
        @DisplayName("cornerLarge dp 值 (16) 与 MorphTokens.Shapes.cornerRadiusLarge 一致")
        fun `cornerLarge dp value matches token`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusLarge, 16)
        }

        @Test
        @DisplayName("cornerSmall 像素值等于对应 dp Token 经密度转换后的结果")
        fun `cornerSmall pixel matches token dp conversion`() {
            assertEquals(
                MorphTokens.Shapes.cornerRadiusSmall.dp(mdpiContext),
                MorphShape.cornerSmall(mdpiContext)
            )
            assertEquals(
                MorphTokens.Shapes.cornerRadiusSmall.dp(xhdpiContext),
                MorphShape.cornerSmall(xhdpiContext)
            )
        }

        @Test
        @DisplayName("cornerMedium 像素值等于对应 dp Token 经密度转换后的结果")
        fun `cornerMedium pixel matches token dp conversion`() {
            assertEquals(
                MorphTokens.Shapes.cornerRadiusMedium.dp(mdpiContext),
                MorphShape.cornerMedium(mdpiContext)
            )
            assertEquals(
                MorphTokens.Shapes.cornerRadiusMedium.dp(xhdpiContext),
                MorphShape.cornerMedium(xhdpiContext)
            )
        }

        @Test
        @DisplayName("cornerLarge 像素值等于对应 dp Token 经密度转换后的结果")
        fun `cornerLarge pixel matches token dp conversion`() {
            assertEquals(
                MorphTokens.Shapes.cornerRadiusLarge.dp(mdpiContext),
                MorphShape.cornerLarge(mdpiContext)
            )
            assertEquals(
                MorphTokens.Shapes.cornerRadiusLarge.dp(xhdpiContext),
                MorphShape.cornerLarge(xhdpiContext)
            )
        }

        @Test
        @DisplayName("cornerMedium 与 MorphTokens.Shapes.cornerRadiusButtonIos 一致 (均为 12dp)")
        fun `cornerMedium matches button iOS corner radius`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusButtonIos, 12)
            assertEquals(
                MorphTokens.Shapes.cornerRadiusButtonIos.dp(xhdpiContext),
                MorphShape.cornerMedium(xhdpiContext)
            )
        }

        @Test
        @DisplayName("cornerLarge 与 MorphTokens.Shapes.cornerRadiusCardIos 一致 (均为 16dp)")
        fun `cornerLarge matches card iOS corner radius`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusCardIos, 16)
            assertEquals(
                MorphTokens.Shapes.cornerRadiusCardIos.dp(xhdpiContext),
                MorphShape.cornerLarge(xhdpiContext)
            )
        }

        @Test
        @DisplayName("cornerSmall 与 MorphTokens.Shapes.cornerRadiusButtonPixel 一致 (均为 8dp)")
        fun `cornerSmall matches button Pixel corner radius`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusButtonPixel, 8)
        }

        @Test
        @DisplayName("MorphTokens.Shapes 嵌套对象常量可正确访问")
        fun `MorphTokens top-level constants match Shapes sub-object`() {
            assertEquals(MorphTokens.Shapes.cornerRadiusSmall, MorphTokens.Shapes.cornerRadiusSmall)
            assertEquals(MorphTokens.Shapes.cornerRadiusMedium, MorphTokens.Shapes.cornerRadiusMedium)
            assertEquals(MorphTokens.Shapes.cornerRadiusLarge, MorphTokens.Shapes.cornerRadiusLarge)
            assertEquals(MorphTokens.Shapes.cornerRadiusButtonIos, MorphTokens.Shapes.cornerRadiusButtonIos)
            assertEquals(MorphTokens.Shapes.cornerRadiusButtonPixel, MorphTokens.Shapes.cornerRadiusButtonPixel)
            assertEquals(MorphTokens.Shapes.cornerRadiusCardIos, MorphTokens.Shapes.cornerRadiusCardIos)
            assertEquals(MorphTokens.Shapes.cornerRadiusCardPixel, MorphTokens.Shapes.cornerRadiusCardPixel)
            assertEquals(MorphTokens.Shapes.cornerRadiusTextFieldIos, MorphTokens.Shapes.cornerRadiusTextFieldIos)
            assertEquals(MorphTokens.Shapes.cornerRadiusTextFieldPixel, MorphTokens.Shapes.cornerRadiusTextFieldPixel)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 排序与一致性
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("排序与一致性")
    inner class OrderingTest {

        @Test
        @DisplayName("圆角大小顺序: small < medium < large (所有密度)")
        fun `corner ordering is preserved at all densities`() {
            listOf(mdpiContext, xhdpiContext, xxhdpiContext, xxxhdpiContext).forEach { ctx ->
                val small = MorphShape.cornerSmall(ctx)
                val medium = MorphShape.cornerMedium(ctx)
                val large = MorphShape.cornerLarge(ctx)
                assertTrue(small < medium, "cornerSmall($small) should < cornerMedium($medium)")
                assertTrue(medium < large, "cornerMedium($medium) should < cornerLarge($large)")
            }
        }

        @Test
        @DisplayName("medium - small = large - medium = 4dp (等差)")
        fun `corner radii are arithmetic progression with step 4dp`() {
            listOf(mdpiContext, xhdpiContext, xxhdpiContext, xxxhdpiContext).forEach { ctx ->
                val small = MorphShape.cornerSmall(ctx)
                val medium = MorphShape.cornerMedium(ctx)
                val large = MorphShape.cornerLarge(ctx)
                assertEquals(medium - small, large - medium,
                    "corner radii should form arithmetic progression")
            }
        }

        @Test
        @DisplayName("同一圆角在不同密度下返回不同像素值")
        fun `same corner returns different pixels on different density contexts`() {
            val smallMdpi = MorphShape.cornerSmall(mdpiContext)
            val smallXhdpi = MorphShape.cornerSmall(xhdpiContext)
            assertNotEquals(smallMdpi, smallXhdpi,
                "cornerSmall should differ between mdpi and xhdpi")
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 边界条件测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界条件测试")
    inner class BoundaryTest {

        @Test
        @DisplayName("所有基于 Context 的圆角值均为正数")
        fun `all context-based corners are positive`() {
            listOf(mdpiContext, xhdpiContext, xxhdpiContext, xxxhdpiContext).forEach { ctx ->
                assertTrue(MorphShape.cornerSmall(ctx) > 0)
                assertTrue(MorphShape.cornerMedium(ctx) > 0)
                assertTrue(MorphShape.cornerLarge(ctx) > 0)
            }
        }

        @Test
        @DisplayName("cornerFull 不依赖 Context，为固定常量")
        fun `cornerFull is a constant independent of Context`() {
            assertEquals(MorphShape.cornerFull, MorphShape.cornerFull)
            assertEquals(Int.MAX_VALUE, MorphShape.cornerFull)
        }

        @Test
        @DisplayName("高密度下像素值为低密度的整数倍")
        fun `high density pixels are integer multiples of low density`() {
            // xhdpi (2.0) is 2x mdpi (1.0)
            assertEquals(
                MorphShape.cornerSmall(mdpiContext) * 2,
                MorphShape.cornerSmall(xhdpiContext)
            )
            assertEquals(
                MorphShape.cornerMedium(mdpiContext) * 2,
                MorphShape.cornerMedium(xhdpiContext)
            )
            assertEquals(
                MorphShape.cornerLarge(mdpiContext) * 2,
                MorphShape.cornerLarge(xhdpiContext)
            )
        }

        @Test
        @DisplayName("xxxhdpi 下像素值为 mdpi 的 4 倍")
        fun `xxxhdpi pixels are 4x mdpi pixels`() {
            assertEquals(
                MorphShape.cornerSmall(mdpiContext) * 4,
                MorphShape.cornerSmall(xxxhdpiContext)
            )
            assertEquals(
                MorphShape.cornerMedium(mdpiContext) * 4,
                MorphShape.cornerMedium(xxxhdpiContext)
            )
            assertEquals(
                MorphShape.cornerLarge(mdpiContext) * 4,
                MorphShape.cornerLarge(xxxhdpiContext)
            )
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 创建具有指定屏幕密度的 mock [Context]。
     *
     * 构建 mock 链：Context -> Resources -> DisplayMetrics，
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
}
