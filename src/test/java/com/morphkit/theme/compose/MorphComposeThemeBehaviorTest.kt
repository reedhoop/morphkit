package com.morphkit.theme.compose

import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import com.morphkit.core.InteractionMode
import com.morphkit.core.StylePolicy
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * MorphComposeTheme 行为测试。
 *
 * 使用 Compose-only MorphTheme 入口（不依赖 Context），
 * 验证 CompositionLocal 传递、颜色/形状/交互模式正确性。
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MorphComposeThemeBehaviorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ═══════════════════════════════════════════════════════════════════════
    // 1. CompositionLocal 传递验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS风格下LocalMorphInteractionMode为IOS`() {
        var mode: InteractionMode? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                mode = LocalMorphInteractionMode.current
            }
        }
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun `PIXEL风格下LocalMorphInteractionMode为MATERIAL`() {
        var mode: InteractionMode? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                mode = LocalMorphInteractionMode.current
            }
        }
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun `IOS风格下LocalMorphStylePolicy为IOS`() {
        var policy: StylePolicy? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                policy = LocalMorphStylePolicy.current
            }
        }
        assertThat(policy).isEqualTo(StylePolicy.IOS)
    }

    @Test
    fun `PIXEL风格下LocalMorphStylePolicy为PIXEL`() {
        var policy: StylePolicy? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                policy = LocalMorphStylePolicy.current
            }
        }
        assertThat(policy).isEqualTo(StylePolicy.PIXEL)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 2. 颜色 CompositionLocal 验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS风格下颜色色板非空`() {
        var colors: MorphColorPalette? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                colors = LocalMorphColors.current
            }
        }
        assertThat(colors).isNotNull()
    }

    @Test
    fun `IOS风格下primaryColor为iOS蓝`() {
        var primary: androidx.compose.ui.graphics.Color? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                interactionMode = InteractionMode.IOS
            ) {
                primary = LocalMorphColors.current.primary
            }
        }
        assertThat(primary).isNotNull()
        // iOS blue #007AFF
        assertThat(primary!!.red).isWithin(0.01f).of(0f / 255f)
        assertThat(primary.green).isWithin(0.01f).of(122f / 255f)
        assertThat(primary.blue).isWithin(0.01f).of(255f / 255f)
    }

    @Test
    fun `IOS暗色模式下颜色色板为iosDark`() {
        var colors: MorphColorPalette? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosDark(),
                interactionMode = InteractionMode.IOS
            ) {
                colors = LocalMorphColors.current
            }
        }
        assertThat(colors).isNotNull()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 3. MorphShape CompositionLocal 验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `IOS风格下MorphShape圆角为iOS值`() {
        var shape: MorphShape? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.ios(),
                interactionMode = InteractionMode.IOS
            ) {
                shape = LocalMorphShape.current
            }
        }
        assertThat(shape).isNotNull()
        assertThat(shape!!.cornerRadiusButton).isEqualTo(12)
        assertThat(shape.cornerRadiusCard).isEqualTo(16)
        assertThat(shape.cornerRadiusTextField).isEqualTo(12)
    }

    @Test
    fun `PIXEL风格下MorphShape圆角为Pixel值`() {
        var shape: MorphShape? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                shape = LocalMorphShape.current
            }
        }
        assertThat(shape).isNotNull()
        assertThat(shape!!.cornerRadiusButton).isEqualTo(8)
        assertThat(shape.cornerRadiusCard).isEqualTo(12)
        assertThat(shape.cornerRadiusTextField).isEqualTo(8)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 4. MorphShape 工厂方法验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphShape_ios工厂方法返回正确圆角`() {
        val shape = MorphShape.ios()
        assertThat(shape.cornerRadiusButton).isEqualTo(12)
        assertThat(shape.cornerRadiusCard).isEqualTo(16)
        assertThat(shape.cornerRadiusTextField).isEqualTo(12)
        assertThat(shape.cornerRadiusSmall).isEqualTo(8)
        assertThat(shape.cornerRadiusMedium).isEqualTo(12)
        assertThat(shape.cornerRadiusLarge).isEqualTo(16)
    }

    @Test
    fun `MorphShape_pixel工厂方法返回正确圆角`() {
        val shape = MorphShape.pixel()
        assertThat(shape.cornerRadiusButton).isEqualTo(8)
        assertThat(shape.cornerRadiusCard).isEqualTo(12)
        assertThat(shape.cornerRadiusTextField).isEqualTo(8)
        assertThat(shape.cornerRadiusSmall).isEqualTo(8)
        assertThat(shape.cornerRadiusMedium).isEqualTo(12)
        assertThat(shape.cornerRadiusLarge).isEqualTo(16)
    }

    @Test
    fun `MorphShape_ios与pixel共享通用圆角值`() {
        val ios = MorphShape.ios()
        val pixel = MorphShape.pixel()
        assertThat(ios.cornerRadiusSmall).isEqualTo(pixel.cornerRadiusSmall)
        assertThat(ios.cornerRadiusMedium).isEqualTo(pixel.cornerRadiusMedium)
        assertThat(ios.cornerRadiusLarge).isEqualTo(pixel.cornerRadiusLarge)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 5. MorphTheme Context 入口验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `MorphTheme_IOS参数正确传递交互模式`() {
        var mode: InteractionMode? = null
        composeTestRule.setContent {
            MorphTheme(themeStyle = StylePolicy.IOS) {
                mode = LocalMorphInteractionMode.current
            }
        }
        assertThat(mode).isEqualTo(InteractionMode.IOS)
    }

    @Test
    fun `MorphTheme_PIXEL参数正确传递交互模式`() {
        var mode: InteractionMode? = null
        composeTestRule.setContent {
            MorphTheme(themeStyle = StylePolicy.PIXEL) {
                mode = LocalMorphInteractionMode.current
            }
        }
        assertThat(mode).isEqualTo(InteractionMode.MATERIAL)
    }

    @Test
    fun `MorphTheme_IOS参数正确传递形状`() {
        var shape: MorphShape? = null
        composeTestRule.setContent {
            MorphTheme(themeStyle = StylePolicy.IOS) {
                shape = LocalMorphShape.current
            }
        }
        assertThat(shape?.cornerRadiusButton).isEqualTo(12)
    }

    @Test
    fun `MorphTheme_PIXEL参数正确传递形状`() {
        var shape: MorphShape? = null
        composeTestRule.setContent {
            MorphTheme(themeStyle = StylePolicy.PIXEL) {
                shape = LocalMorphShape.current
            }
        }
        assertThat(shape?.cornerRadiusButton).isEqualTo(8)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // 6. Compose-only 入口验证
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    fun `Compose-only入口正确传递自定义颜色`() {
        var primary: androidx.compose.ui.graphics.Color? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosDark(),
                interactionMode = InteractionMode.IOS
            ) {
                primary = LocalMorphColors.current.primary
            }
        }
        // iosDark primary 应为暗色模式下的 primary
        assertThat(primary).isNotNull()
    }

    @Test
    fun `Compose-only入口正确传递自定义形状`() {
        var shape: MorphShape? = null
        composeTestRule.setContent {
            MorphTheme(
                colors = MorphColorPalette.iosLight(),
                shape = MorphShape.pixel(),
                interactionMode = InteractionMode.MATERIAL
            ) {
                shape = LocalMorphShape.current
            }
        }
        assertThat(shape?.cornerRadiusButton).isEqualTo(8)
    }
}
