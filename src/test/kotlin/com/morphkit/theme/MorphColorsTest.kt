package com.morphkit.theme

import android.content.Context
import android.content.res.Configuration
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import io.mockk.every
import io.mockk.mockk

/**
 * MorphColors 单元测试。
 *
 * 覆盖：
 * 1. isDarkMode — 暗色模式检测
 *
 * 注意：overlayColor / adjustAlpha / blendColor / createColorStateList
 * 依赖 android.graphics.Color 的 ARGB 解析方法，在 JVM 单元测试中
 * 这些方法返回默认值 0（returnDefaultValues=true），无法正确验证。
 * 这些方法已有 Robolectric 行为测试覆盖（见 BackdropBlurHelperBehaviorTest）。
 */
class MorphColorsTest {

    @Nested
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
}
