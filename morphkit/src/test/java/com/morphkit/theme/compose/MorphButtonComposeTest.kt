package com.morphkit.theme.compose

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
 * 使用 Compose-only MorphTheme 入口（不依赖 Context），
 * 在 Robolectric + Compose Test Rule 环境下验证核心行为。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MorphButtonComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════
    // 1. 基础渲染 — 按钮文字正确显示
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式下按钮文字正确渲染`() {
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
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
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                MorphButton(text = "Material按钮", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Material按钮")
            .assertIsDisplayed()
            .assertTextEquals("Material按钮")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. 交互模式分发
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式下按钮渲染成功`() {
        var clicked = false
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                MorphButton(text = "点击", onClick = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("点击").assertIsDisplayed()
        assertThat(clicked).isFalse()
    }

    @Test
    fun `MATERIAL模式下按钮渲染成功`() {
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                MorphButton(text = "Material", onClick = {})
            }
        }

        composeTestRule.onNodeWithText("Material").assertIsDisplayed()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. 禁用态
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `禁用态IOS按钮仍渲染文字`() {
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
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
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                MorphButton(text = "禁用M", onClick = {}, enabled = false)
            }
        }

        composeTestRule.onNodeWithText("禁用M")
            .assertIsDisplayed()
            .assertTextEquals("禁用M")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. 显式交互模式覆盖
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `显式传入IOS交互模式覆盖CompositionLocal`() {
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
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
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
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
    // 5. 点击回调
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS模式点击触发onClick回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
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
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
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
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                MorphButton(text = "禁用点击", onClick = { clickCount++ }, enabled = false)
            }
        }

        composeTestRule.onNodeWithText("禁用点击").performClick()
        assertThat(clickCount).isEqualTo(0)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. 多次点击
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `连续多次点击均触发回调`() {
        var clickCount = 0
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                MorphButton(text = "多次点击", onClick = { clickCount++ })
            }
        }

        val node = composeTestRule.onNodeWithText("多次点击")
        node.performClick()
        node.performClick()
        node.performClick()
        assertThat(clickCount).isEqualTo(3)
    }
}
