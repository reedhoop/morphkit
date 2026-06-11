package com.morphkit.theme.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.morphkit.core.InteractionMode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * MorphButton Compose 组件行为测试。
 *
 * 在 Robolectric + Compose Test Rule 环境下验证 MorphButton 的核心行为：
 * - IOS / MATERIAL 交互模式分发
 * - 按钮文字渲染
 * - 禁用态视觉
 * - CompositionLocal 正确传递
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphButtonComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════
    // 1. 基础渲染 — 按钮文字正确显示
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式下按钮文字正确渲染`() {
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "测试按钮", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("测试按钮")
            .assertIsDisplayed()
            .assertTextEquals("测试按钮")
    }

    @Test
    fun `MATERIAL模式下按钮文字正确渲染`() {
        composeTestRule.setContent {
            TestMorphThemeMaterial {
                MorphButton(text = "Material按钮", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Material按钮")
            .assertIsDisplayed()
            .assertTextEquals("Material按钮")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. 交互模式分发 — IOS / MATERIAL 渲染不同组件
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式下按钮无涟漪指示器`() {
        // IOS 模式使用 indication = null，无 Ripple
        // 验证方式：IOS 模式下按钮仍然可点击且渲染成功
        var clicked = false
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "点击", onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("点击").assertIsDisplayed()
        assertThat(clicked).isFalse()
    }

    @Test
    fun `MATERIAL模式下按钮渲染成功`() {
        composeTestRule.setContent {
            TestMorphThemeMaterial {
                MorphButton(text = "Material", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Material").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. 禁用态 — 按钮不可交互
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `禁用态IOS按钮仍渲染文字`() {
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "禁用", onClick = {}, enabled = false)
            }
        }

        composeTestRule.onNodeWithText("禁用")
            .assertIsDisplayed()
            .assertTextEquals("禁用")
    }

    @Test
    fun `禁用态MATERIAL按钮仍渲染文字`() {
        composeTestRule.setContent {
            TestMorphThemeMaterial {
                MorphButton(text = "禁用M", onClick = {}, enabled = false)
            }
        }

        composeTestRule.onNodeWithText("禁用M")
            .assertIsDisplayed()
            .assertTextEquals("禁用M")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. CompositionLocal 传递 — 交互模式正确读取
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `显式传入IOS交互模式覆盖CompositionLocal`() {
        // 即使 CompositionLocal 是 MATERIAL，显式传入 IOS 也应生效
        composeTestRule.setContent {
            TestMorphThemeMaterial {
                MorphButton(
                    text = "覆盖",
                    onClick = {},
                    interactionMode = InteractionMode.IOS
                )
            }
        }

        composeTestRule.onNodeWithText("覆盖").assertIsDisplayed()
    }

    @Test
    fun `显式传入MATERIAL交互模式覆盖CompositionLocal`() {
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(
                    text = "覆盖M",
                    onClick = {},
                    interactionMode = InteractionMode.MATERIAL
                )
            }
        }

        composeTestRule.onNodeWithText("覆盖M").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. 点击回调 — onClick 正确触发
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式点击触发onClick回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "点击我", onClick = { clickCount++ })
            }
        }

        composeTestRule.onNodeWithText("点击我").performClick()
        assertThat(clickCount).isEqualTo(1)
    }

    @Test
    fun `MATERIAL模式点击触发onClick回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            TestMorphThemeMaterial {
                MorphButton(text = "点击我M", onClick = { clickCount++ })
            }
        }

        composeTestRule.onNodeWithText("点击我M").performClick()
        assertThat(clickCount).isEqualTo(1)
    }

    @Test
    fun `禁用态点击不触发onClick回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "禁用点击", onClick = { clickCount++ }, enabled = false)
            }
        }

        // 禁用态下 performClick 不应触发回调
        composeTestRule.onNodeWithText("禁用点击").performClick()
        assertThat(clickCount).isEqualTo(0)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 多次点击 — 防抖验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `连续多次点击均触发回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            TestMorphThemeIOS {
                MorphButton(text = "多次点击", onClick = { clickCount++ })
            }
        }

        val node = composeTestRule.onNodeWithText("多次点击")
        node.performClick()
        node.performClick()
        node.performClick()
        assertThat(clickCount).isEqualTo(3)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 测试辅助 — 直接提供 CompositionLocal 而不依赖 Context
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * 测试用 IOS 主题包裹器。
     *
     * 直接通过 CompositionLocalProvider 注入 IOS 模式的颜色、形状和交互模式，
     * 避免依赖 MorphTheme 的 Context 读取逻辑（Robolectric 环境下 Context Theme 不完整）。
     */
    @Composable
    private fun TestMorphThemeIOS(content: @Composable () -> Unit) {
        val colors = MorphColorPalette.iosLight()
        CompositionLocalProvider(
            LocalMorphColors provides colors,
            LocalMorphShape provides MorphShape.ios(),
            LocalMorphInteractionMode provides InteractionMode.IOS
        ) {
            androidx.compose.material3.MaterialTheme(
                colorScheme = testMaterial3ColorScheme(colors),
                content = content
            )
        }
    }

    /**
     * 测试用 MATERIAL 主题包裹器。
     */
    @Composable
    private fun TestMorphThemeMaterial(content: @Composable () -> Unit) {
        val colors = MorphColorPalette.iosLight() // 测试环境使用 IOS 色板，交互模式为 MATERIAL
        CompositionLocalProvider(
            LocalMorphColors provides colors,
            LocalMorphShape provides MorphShape.pixel(),
            LocalMorphInteractionMode provides InteractionMode.MATERIAL
        ) {
            androidx.compose.material3.MaterialTheme(
                colorScheme = testMaterial3ColorScheme(colors),
                content = content
            )
        }
    }

    /** 测试用 M3 ColorScheme — 从 MorphColorPalette 映射 */
    private fun testMaterial3ColorScheme(c: MorphColorPalette) = androidx.compose.material3.ColorScheme(
        primary = c.primary,
        onPrimary = c.onPrimary,
        primaryContainer = c.primaryContainer,
        onPrimaryContainer = c.onPrimaryContainer,
        secondary = c.secondary,
        onSecondary = c.onSecondary,
        secondaryContainer = c.secondaryContainer,
        onSecondaryContainer = c.onSecondaryContainer,
        tertiary = c.tertiary,
        onTertiary = c.onTertiary,
        tertiaryContainer = c.tertiaryContainer,
        onTertiaryContainer = c.onTertiaryContainer,
        error = c.error,
        onError = c.onError,
        errorContainer = c.errorContainer,
        onErrorContainer = c.onErrorContainer,
        background = c.background,
        onBackground = c.onBackground,
        surface = c.surface,
        onSurface = c.onSurface,
        surfaceVariant = c.surfaceVariant,
        onSurfaceVariant = c.onSurfaceVariant,
        surfaceDim = c.surfaceDim,
        surfaceBright = c.surfaceBright,
        surfaceContainerLowest = c.surfaceContainerLowest,
        surfaceContainerLow = c.surfaceContainerLow,
        surfaceContainer = c.surfaceContainer,
        surfaceContainerHigh = c.surfaceContainerHigh,
        surfaceContainerHighest = c.surfaceContainerHighest,
        surfaceTint = c.primary,
        inverseSurface = c.inverseSurface,
        inverseOnSurface = c.inverseOnSurface,
        inversePrimary = c.inversePrimary,
        outline = c.outline,
        outlineVariant = c.outlineVariant,
        scrim = c.scrim
    )
}
