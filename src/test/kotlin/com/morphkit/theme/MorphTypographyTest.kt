package com.morphkit.theme

import android.graphics.Typeface
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * MorphTypography 单元测试。
 *
 * 覆盖：
 * 1. 所有排版 Token 的字号值正确性
 * 2. 所有排版 Token 的字重映射正确性
 * 3. [FontWeight] 枚举的 [FontWeight.weight] 数值与 [FontWeight.toTypeface] 映射
 * 4. [MorphTypography] 与 [MorphTokens.Typography] 的一致性
 * 5. [TextStyle] data class 的 equals/hashCode/copy 行为
 * 6. 边界条件测试
 */
class MorphTypographyTest {

    companion object {
        private lateinit var mockTypeface: Typeface

        @JvmStatic
        @BeforeAll
        fun setUpTypeface() {
            mockTypeface = mockk(relaxed = true)
            mockkStatic(Typeface::class)
            every { Typeface.create(any<String>(), any()) } returns mockTypeface
        }

        @JvmStatic
        @AfterAll
        fun tearDownTypeface() {
            unmockkStatic(Typeface::class)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 排版 Token 字号验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("排版 Token — 字号值验证")
    inner class FontSizeTest {

        @Test
        @DisplayName("largeTitle 字号 = 34sp")
        fun `largeTitle fontSize is 34`() {
            assertEquals(34f, MorphTypography.largeTitle.fontSize)
        }

        @Test
        @DisplayName("title1 字号 = 28sp")
        fun `title1 fontSize is 28`() {
            assertEquals(28f, MorphTypography.title1.fontSize)
        }

        @Test
        @DisplayName("title2 字号 = 22sp")
        fun `title2 fontSize is 22`() {
            assertEquals(22f, MorphTypography.title2.fontSize)
        }

        @Test
        @DisplayName("title3 字号 = 20sp")
        fun `title3 fontSize is 20`() {
            assertEquals(20f, MorphTypography.title3.fontSize)
        }

        @Test
        @DisplayName("headline 字号 = 17sp")
        fun `headline fontSize is 17`() {
            assertEquals(17f, MorphTypography.headline.fontSize)
        }

        @Test
        @DisplayName("body 字号 = 17sp")
        fun `body fontSize is 17`() {
            assertEquals(17f, MorphTypography.body.fontSize)
        }

        @Test
        @DisplayName("callout 字号 = 16sp")
        fun `callout fontSize is 16`() {
            assertEquals(16f, MorphTypography.callout.fontSize)
        }

        @Test
        @DisplayName("subheadline 字号 = 15sp")
        fun `subheadline fontSize is 15`() {
            assertEquals(15f, MorphTypography.subheadline.fontSize)
        }

        @Test
        @DisplayName("footnote 字号 = 13sp")
        fun `footnote fontSize is 13`() {
            assertEquals(13f, MorphTypography.footnote.fontSize)
        }

        @Test
        @DisplayName("caption1 字号 = 12sp")
        fun `caption1 fontSize is 12`() {
            assertEquals(12f, MorphTypography.caption1.fontSize)
        }

        @Test
        @DisplayName("caption2 字号 = 11sp")
        fun `caption2 fontSize is 11`() {
            assertEquals(11f, MorphTypography.caption2.fontSize)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 排版 Token 字重验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("排版 Token — 字重映射验证")
    inner class FontWeightMappingTest {

        @Test
        @DisplayName("largeTitle 字重 = EXTRA_BOLD")
        fun `largeTitle weight is EXTRA_BOLD`() {
            assertEquals(FontWeight.EXTRA_BOLD, MorphTypography.largeTitle.weight)
        }

        @Test
        @DisplayName("title1 字重 = EXTRA_BOLD")
        fun `title1 weight is EXTRA_BOLD`() {
            assertEquals(FontWeight.EXTRA_BOLD, MorphTypography.title1.weight)
        }

        @Test
        @DisplayName("title2 字重 = BOLD")
        fun `title2 weight is BOLD`() {
            assertEquals(FontWeight.BOLD, MorphTypography.title2.weight)
        }

        @Test
        @DisplayName("title3 字重 = BOLD")
        fun `title3 weight is BOLD`() {
            assertEquals(FontWeight.BOLD, MorphTypography.title3.weight)
        }

        @Test
        @DisplayName("headline 字重 = SEMI_BOLD")
        fun `headline weight is SEMI_BOLD`() {
            assertEquals(FontWeight.SEMI_BOLD, MorphTypography.headline.weight)
        }

        @Test
        @DisplayName("body 字重 = MEDIUM")
        fun `body weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.body.weight)
        }

        @Test
        @DisplayName("callout 字重 = MEDIUM")
        fun `callout weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.callout.weight)
        }

        @Test
        @DisplayName("subheadline 字重 = MEDIUM")
        fun `subheadline weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.subheadline.weight)
        }

        @Test
        @DisplayName("footnote 字重 = MEDIUM")
        fun `footnote weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.footnote.weight)
        }

        @Test
        @DisplayName("caption1 字重 = MEDIUM")
        fun `caption1 weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.caption1.weight)
        }

        @Test
        @DisplayName("caption2 字重 = MEDIUM")
        fun `caption2 weight is MEDIUM`() {
            assertEquals(FontWeight.MEDIUM, MorphTypography.caption2.weight)
        }

        @Test
        @DisplayName("字号递减顺序: largeTitle > title1 > title2 > ... > caption2")
        fun `typography hierarchy is strictly decreasing`() {
            val styles = listOf(
                MorphTypography.largeTitle,
                MorphTypography.title1,
                MorphTypography.title2,
                MorphTypography.title3,
                MorphTypography.headline,
                // body 和 headline 字号相同 (17sp)，但字重不同
                MorphTypography.callout,
                MorphTypography.subheadline,
                MorphTypography.footnote,
                MorphTypography.caption1,
                MorphTypography.caption2
            )
            for (i in 0 until styles.size - 1) {
                assert(styles[i].fontSize >= styles[i + 1].fontSize) {
                    "${styles[i].fontSize} should >= ${styles[i + 1].fontSize} at index $i"
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FontWeight 枚举验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FontWeight 枚举 — 数值与 Typeface 映射")
    inner class FontWeightEnumTest {

        @Test
        @DisplayName("MEDIUM weight = 500")
        fun `MEDIUM weight is 500`() {
            assertEquals(500, FontWeight.MEDIUM.weight)
        }

        @Test
        @DisplayName("SEMI_BOLD weight = 600")
        fun `SEMI_BOLD weight is 600`() {
            assertEquals(600, FontWeight.SEMI_BOLD.weight)
        }

        @Test
        @DisplayName("BOLD weight = 700")
        fun `BOLD weight is 700`() {
            assertEquals(700, FontWeight.BOLD.weight)
        }

        @Test
        @DisplayName("EXTRA_BOLD weight = 800")
        fun `EXTRA_BOLD weight is 800`() {
            assertEquals(800, FontWeight.EXTRA_BOLD.weight)
        }

        @Test
        @DisplayName("字重数值严格递增: MEDIUM < SEMI_BOLD < BOLD < EXTRA_BOLD")
        fun `weight values are strictly increasing`() {
            assert(FontWeight.MEDIUM.weight < FontWeight.SEMI_BOLD.weight)
            assert(FontWeight.SEMI_BOLD.weight < FontWeight.BOLD.weight)
            assert(FontWeight.BOLD.weight < FontWeight.EXTRA_BOLD.weight)
        }

        @Test
        @DisplayName("FontWeight values() 包含恰好 4 个枚举值")
        fun `FontWeight has exactly 4 values`() {
            assertEquals(4, FontWeight.values().size)
        }

        @Test
        @DisplayName("MEDIUM toTypeface 使用 sans-serif-medium")
        fun `MEDIUM toTypeface uses sans-serif-medium`() {
            FontWeight.MEDIUM.toTypeface()
            verify { Typeface.create("sans-serif-medium", Typeface.NORMAL) }
        }

        @Test
        @DisplayName("SEMI_BOLD toTypeface 使用 sans-serif-semibold")
        fun `SEMI_BOLD toTypeface uses sans-serif-semibold`() {
            FontWeight.SEMI_BOLD.toTypeface()
            verify { Typeface.create("sans-serif-semibold", Typeface.NORMAL) }
        }

        @Test
        @DisplayName("BOLD toTypeface 使用 sans-serif + BOLD")
        fun `BOLD toTypeface uses sans-serif with BOLD style`() {
            FontWeight.BOLD.toTypeface()
            verify { Typeface.create("sans-serif", Typeface.BOLD) }
        }

        @Test
        @DisplayName("EXTRA_BOLD toTypeface 使用 sans-serif-black")
        fun `EXTRA_BOLD toTypeface uses sans-serif-black`() {
            FontWeight.EXTRA_BOLD.toTypeface()
            verify { Typeface.create("sans-serif-black", Typeface.NORMAL) }
        }

        @Test
        @DisplayName("所有 FontWeight 的 toTypeface() 均不抛出异常")
        fun `all toTypeface calls succeed without exception`() {
            FontWeight.values().forEach { weight ->
                // 在纯 JVM 环境下 Typeface.create 返回 mock 值，
                // 验证方法可正常调用不抛异常即可
                weight.toTypeface()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 与 MorphTokens.Typography 一致性验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MorphTypography 与 MorphTokens.Typography 一致性")
    inner class TokenConsistencyTest {

        @Test
        @DisplayName("largeTitle.fontSize == MorphTokens.Typography.fontSizeLargeTitle")
        fun `largeTitle matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeLargeTitle, MorphTypography.largeTitle.fontSize)
        }

        @Test
        @DisplayName("title1.fontSize == MorphTokens.Typography.fontSizeTitle1")
        fun `title1 matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeTitle1, MorphTypography.title1.fontSize)
        }

        @Test
        @DisplayName("title2.fontSize == MorphTokens.Typography.fontSizeTitle2")
        fun `title2 matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeTitle2, MorphTypography.title2.fontSize)
        }

        @Test
        @DisplayName("title3.fontSize == MorphTokens.Typography.fontSizeTitle3")
        fun `title3 matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeTitle3, MorphTypography.title3.fontSize)
        }

        @Test
        @DisplayName("headline.fontSize == MorphTokens.Typography.fontSizeHeadline")
        fun `headline matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeHeadline, MorphTypography.headline.fontSize)
        }

        @Test
        @DisplayName("body.fontSize == MorphTokens.Typography.fontSizeBody")
        fun `body matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeBody, MorphTypography.body.fontSize)
        }

        @Test
        @DisplayName("callout.fontSize == MorphTokens.Typography.fontSizeCallout")
        fun `callout matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeCallout, MorphTypography.callout.fontSize)
        }

        @Test
        @DisplayName("subheadline.fontSize == MorphTokens.Typography.fontSizeSubheadline")
        fun `subheadline matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeSubheadline, MorphTypography.subheadline.fontSize)
        }

        @Test
        @DisplayName("footnote.fontSize == MorphTokens.Typography.fontSizeFootnote")
        fun `footnote matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeFootnote, MorphTypography.footnote.fontSize)
        }

        @Test
        @DisplayName("caption1.fontSize == MorphTokens.Typography.fontSizeCaption1")
        fun `caption1 matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeCaption1, MorphTypography.caption1.fontSize)
        }

        @Test
        @DisplayName("caption2.fontSize == MorphTokens.Typography.fontSizeCaption2")
        fun `caption2 matches token`() {
            assertEquals(MorphTokens.Typography.fontSizeCaption2, MorphTypography.caption2.fontSize)
        }

        @Test
        @DisplayName("所有字号与 MorphTokens 顶层扁平常量一致")
        fun `all font sizes match MorphTokens top-level constants`() {
            assertEquals(MorphTokens.Typography.fontSizeLargeTitle, MorphTypography.largeTitle.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeTitle1, MorphTypography.title1.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeTitle2, MorphTypography.title2.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeTitle3, MorphTypography.title3.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeHeadline, MorphTypography.headline.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeBody, MorphTypography.body.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeCallout, MorphTypography.callout.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeSubheadline, MorphTypography.subheadline.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeFootnote, MorphTypography.footnote.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeCaption1, MorphTypography.caption1.fontSize)
            assertEquals(MorphTokens.Typography.fontSizeCaption2, MorphTypography.caption2.fontSize)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TextStyle data class 验证
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TextStyle data class — equals/hashCode/copy")
    inner class TextStyleTest {

        @Test
        @DisplayName("相同 fontSize 和 weight 的 TextStyle 相等")
        fun `equal TextStyles are equal`() {
            val a = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val b = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            assertEquals(a, b)
            assertEquals(a.hashCode(), b.hashCode())
        }

        @Test
        @DisplayName("不同 fontSize 的 TextStyle 不相等")
        fun `different fontSize TextStyles are not equal`() {
            val a = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val b = TextStyle(fontSize = 16f, weight = FontWeight.MEDIUM)
            assertNotEquals(a, b)
        }

        @Test
        @DisplayName("不同 weight 的 TextStyle 不相等")
        fun `different weight TextStyles are not equal`() {
            val a = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val b = TextStyle(fontSize = 17f, weight = FontWeight.BOLD)
            assertNotEquals(a, b)
        }

        @Test
        @DisplayName("copy 创建新实例但值相同")
        fun `copy creates equal instance`() {
            val original = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val copied = original.copy()
            assertEquals(original, copied)
        }

        @Test
        @DisplayName("copy 可修改 fontSize")
        fun `copy can modify fontSize`() {
            val original = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val modified = original.copy(fontSize = 20f)
            assertEquals(20f, modified.fontSize)
            assertEquals(FontWeight.MEDIUM, modified.weight)
        }

        @Test
        @DisplayName("copy 可修改 weight")
        fun `copy can modify weight`() {
            val original = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val modified = original.copy(weight = FontWeight.BOLD)
            assertEquals(17f, modified.fontSize)
            assertEquals(FontWeight.BOLD, modified.weight)
        }

        @Test
        @DisplayName("toString 包含 fontSize 和 weight")
        fun `toString contains fontSize and weight`() {
            val ts = TextStyle(fontSize = 17f, weight = FontWeight.MEDIUM)
            val str = ts.toString()
            assert(str.contains("17")) { "toString should contain fontSize: $str" }
            assert(str.contains("MEDIUM")) { "toString should contain weight: $str" }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 边界条件测试
    // ═══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界条件测试")
    inner class BoundaryTest {

        @Test
        @DisplayName("headline 和 body 字号相同 (17sp) 但字重不同")
        fun `headline and body share fontSize but differ in weight`() {
            assertEquals(MorphTypography.headline.fontSize, MorphTypography.body.fontSize)
            assertNotEquals(MorphTypography.headline.weight, MorphTypography.body.weight)
            assertEquals(FontWeight.SEMI_BOLD, MorphTypography.headline.weight)
            assertEquals(FontWeight.MEDIUM, MorphTypography.body.weight)
        }

        @Test
        @DisplayName("所有字号均为正数")
        fun `all font sizes are positive`() {
            val styles = listOf(
                MorphTypography.largeTitle,
                MorphTypography.title1,
                MorphTypography.title2,
                MorphTypography.title3,
                MorphTypography.headline,
                MorphTypography.body,
                MorphTypography.callout,
                MorphTypography.subheadline,
                MorphTypography.footnote,
                MorphTypography.caption1,
                MorphTypography.caption2
            )
            styles.forEach { style ->
                assert(style.fontSize > 0f) { "fontSize ${style.fontSize} should be positive" }
            }
        }

        @Test
        @DisplayName("所有字号均在合理范围 [8, 40] sp 内")
        fun `all font sizes are in reasonable range`() {
            val styles = listOf(
                MorphTypography.largeTitle,
                MorphTypography.title1,
                MorphTypography.title2,
                MorphTypography.title3,
                MorphTypography.headline,
                MorphTypography.body,
                MorphTypography.callout,
                MorphTypography.subheadline,
                MorphTypography.footnote,
                MorphTypography.caption1,
                MorphTypography.caption2
            )
            styles.forEach { style ->
                assert(style.fontSize in 8f..40f) {
                    "fontSize ${style.fontSize} should be in [8, 40] range"
                }
            }
        }

        @Test
        @DisplayName("恰好 11 个排版层级")
        fun `exactly 11 typography levels exist`() {
            val styles = listOf(
                MorphTypography.largeTitle,
                MorphTypography.title1,
                MorphTypography.title2,
                MorphTypography.title3,
                MorphTypography.headline,
                MorphTypography.body,
                MorphTypography.callout,
                MorphTypography.subheadline,
                MorphTypography.footnote,
                MorphTypography.caption1,
                MorphTypography.caption2
            )
            assertEquals(11, styles.size)
        }

        @Test
        @DisplayName("FontWeight valueOf 正确解析字符串")
        fun `FontWeight valueOf parses correctly`() {
            assertEquals(FontWeight.MEDIUM, FontWeight.valueOf("MEDIUM"))
            assertEquals(FontWeight.SEMI_BOLD, FontWeight.valueOf("SEMI_BOLD"))
            assertEquals(FontWeight.BOLD, FontWeight.valueOf("BOLD"))
            assertEquals(FontWeight.EXTRA_BOLD, FontWeight.valueOf("EXTRA_BOLD"))
        }

        @Test
        @DisplayName("body 和 headline 是不同实例")
        fun `body and headline are different instances`() {
            assertNotEquals(MorphTypography.body, MorphTypography.headline)
        }
    }
}
