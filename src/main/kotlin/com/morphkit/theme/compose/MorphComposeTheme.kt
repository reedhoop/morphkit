package com.morphkit.theme.compose

import android.content.Context
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.android.material.color.MaterialColors
import com.morphkit.core.InteractionMode
import com.morphkit.core.StylePolicy
import com.morphkit.theme.MorphStyleResolver
import com.morphkit.theme.MorphTokens

/**
 * MorphKit Compose 主题系统。
 *
 * 从 [MorphTokens] 和 Context Theme 读取设计变量，转换为 Compose 的 [Color]、[Typography]，
 * 通过 [CompositionLocal] 向下传递，确保 Compose 组件与 View 体系视觉完全一致。
 *
 * ## 单一真相源架构
 *
 * 本文件与 View 体系共享同一套枚举类型，消除以往 `MorphStyle` / [StylePolicy]
 * 和 `MorphInteractionMode` / [InteractionMode] 双重概念的歧义：
 *
 * | 概念 | 统一类型 | 所在包 |
 * |------|---------|--------|
 * | 风格策略 | [StylePolicy] | `com.morphkit.core` |
 * | 交互模式 | [InteractionMode] | `com.morphkit.core` |
 *
 * ## 颜色真相源统一
 *
 * | 模式 | 颜色来源 | 与 View 体系一致性 |
 * |------|---------|------------------|
 * | IOS | [MorphTokens] 硬编码常量 | ✅ iOS 皮肤不依赖 Dynamic Color |
 * | PIXEL | Context Theme 的 M3 语义色 | ✅ 与 View 体系读取同一 Theme，支持 Dynamic Color |
 * | AUTO | 根据设备特性自动选择上述之一 | ✅ |
 *
 * ## OEM Compose 接入规范（强制）
 *
 * 对于 Compose 应用，**必须**在根 Activity 的 `setContent` 块最外层包裹 [MorphTheme]：
 *
 * ```kotlin
 * override fun onCreate(savedInstanceState: Bundle?) {
 *     super.onCreate(savedInstanceState)
 *     setContent {
 *         MorphTheme {
 *             MyAppNavigation()
 *         }
 *     }
 * }
 * ```
 *
 * **禁止**业务方直接使用 `androidx.compose.material3.Button`，
 * **必须**使用 [MorphButton]，以保证 ROM 级的交互和视觉统一。
 *
 * @see MorphTokens 统一设计 Token 层
 * @see MorphButton Compose 按钮（交互模式分发）
 * @see StylePolicy 统一风格策略枚举
 * @see InteractionMode 统一交互模式枚举
 */

// ═════════════════════════════════════════════════════════════════════════════════
// MorphColorPalette — Compose 颜色体系
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 颜色色板。
 *
 * 完整覆盖 M3 ColorScheme 的 37 个语义色角色，消除以往 secondary/tertiary 复用 primary、
 * surfaceContainer 系列缺失等问题。每个字段与 M3 ColorScheme 角色一一对应。
 *
 * ## Token 一致性契约
 *
 * [iosLight] / [iosDark] 的每个字段**必须**从 [MorphTokens] 读取，确保编译期可追溯。
 * [pixelFromContext] 从 Context Theme 读取 M3 语义色，降级到 MorphTokens 默认值。
 * 若新增 Token 字段，必须同步在此类的三个工厂方法中添加对应映射。
 */
@Immutable
data class MorphColorPalette(
    // ── Primary ──
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    // ── Secondary ──
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    // ── Tertiary ──
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    // ── Error ──
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    // ── Surface ──
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val surfaceDim: Color,
    val surfaceBright: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    // ── Outline ──
    val outline: Color,
    val outlineVariant: Color,
    // ── Background ──
    val background: Color,
    val onBackground: Color,
    // ── Inverse ──
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    // ── Misc ──
    val scrim: Color,
    val success: Color,
    val warning: Color
) {
    companion object {
        /** iOS 亮色色板 — 从 MorphTokens 读取 */
        fun iosLight() = MorphColorPalette(
            // Primary
            primary = Color(MorphTokens.colorBlue500),
            onPrimary = Color(MorphTokens.colorOnPrimary),
            primaryContainer = Color(MorphTokens.colorPrimaryContainer),
            onPrimaryContainer = Color(MorphTokens.colorOnPrimaryContainer),
            // Secondary
            secondary = Color(MorphTokens.colorSecondary),
            onSecondary = Color(MorphTokens.colorOnSecondary),
            secondaryContainer = Color(MorphTokens.colorSecondaryContainer),
            onSecondaryContainer = Color(MorphTokens.colorOnSecondaryContainer),
            // Tertiary
            tertiary = Color(MorphTokens.colorTertiary),
            onTertiary = Color(MorphTokens.colorOnTertiary),
            tertiaryContainer = Color(MorphTokens.colorTertiaryContainer),
            onTertiaryContainer = Color(MorphTokens.colorOnTertiaryContainer),
            // Error
            error = Color(MorphTokens.colorRed500),
            onError = Color(MorphTokens.colorOnError),
            errorContainer = Color(MorphTokens.colorErrorContainer),
            onErrorContainer = Color(MorphTokens.colorOnErrorContainer),
            // Surface
            surface = Color(MorphTokens.colorSurface),
            onSurface = Color(MorphTokens.colorOnSurface),
            surfaceVariant = Color(MorphTokens.colorSurfaceVariant),
            onSurfaceVariant = Color(MorphTokens.colorOnSurfaceVariant),
            surfaceDim = Color(MorphTokens.colorSurfaceDim),
            surfaceBright = Color(MorphTokens.colorSurfaceBright),
            surfaceContainerLowest = Color(MorphTokens.colorSurfaceContainerLowest),
            surfaceContainerLow = Color(MorphTokens.colorSurfaceContainerLow),
            surfaceContainer = Color(MorphTokens.colorSurfaceContainer),
            surfaceContainerHigh = Color(MorphTokens.colorSurfaceContainerHigh),
            surfaceContainerHighest = Color(MorphTokens.colorSurfaceContainerHighest),
            // Outline
            outline = Color(MorphTokens.colorOutline),
            outlineVariant = Color(MorphTokens.colorOutlineVariant),
            // Background
            background = Color(MorphTokens.colorBackground),
            onBackground = Color(MorphTokens.colorOnSurface),
            // Inverse
            inverseSurface = Color(MorphTokens.colorOnSurface),
            inverseOnSurface = Color(MorphTokens.colorSurface),
            inversePrimary = Color(MorphTokens.colorBlue100),
            // Misc
            scrim = Color.Black,
            success = Color(MorphTokens.colorGreen500),
            warning = Color(MorphTokens.colorOrange500)
        )

        /** iOS 暗色色板 — 从 MorphTokens 读取 */
        fun iosDark() = MorphColorPalette(
            // Primary
            primary = Color(MorphTokens.colorBlue100),
            onPrimary = Color(0xFF001D3F),
            primaryContainer = Color(MorphTokens.colorBlue700),
            onPrimaryContainer = Color(MorphTokens.colorBlue100),
            // Secondary
            secondary = Color(MorphTokens.colorSecondaryDark),
            onSecondary = Color(MorphTokens.colorOnSecondaryDark),
            secondaryContainer = Color(MorphTokens.colorSecondaryContainerDark),
            onSecondaryContainer = Color(MorphTokens.colorOnSecondaryContainerDark),
            // Tertiary
            tertiary = Color(MorphTokens.colorTertiaryDark),
            onTertiary = Color(MorphTokens.colorOnTertiaryDark),
            tertiaryContainer = Color(MorphTokens.colorTertiaryContainerDark),
            onTertiaryContainer = Color(MorphTokens.colorOnTertiaryContainerDark),
            // Error
            error = Color(0xFFFF6961),
            onError = Color(MorphTokens.colorOnErrorDark),
            errorContainer = Color(MorphTokens.colorErrorContainerDark),
            onErrorContainer = Color(MorphTokens.colorOnErrorContainerDark),
            // Surface
            surface = Color(MorphTokens.colorSurfaceDark),
            onSurface = Color(MorphTokens.colorOnSurfaceDark),
            surfaceVariant = Color(MorphTokens.colorSurfaceVariantDark),
            onSurfaceVariant = Color(MorphTokens.colorOnSurfaceVariantDark),
            surfaceDim = Color(MorphTokens.colorSurfaceDimDark),
            surfaceBright = Color(MorphTokens.colorSurfaceBrightDark),
            surfaceContainerLowest = Color(MorphTokens.colorSurfaceContainerLowestDark),
            surfaceContainerLow = Color(MorphTokens.colorSurfaceContainerLowDark),
            surfaceContainer = Color(MorphTokens.colorSurfaceContainerDark),
            surfaceContainerHigh = Color(MorphTokens.colorSurfaceContainerHighDark),
            surfaceContainerHighest = Color(MorphTokens.colorSurfaceContainerHighestDark),
            // Outline
            outline = Color(MorphTokens.colorOutlineDark),
            outlineVariant = Color(MorphTokens.colorOutlineVariantDark),
            // Background
            background = Color(MorphTokens.colorBackgroundDark),
            onBackground = Color(MorphTokens.colorOnSurfaceDark),
            // Inverse
            inverseSurface = Color(MorphTokens.colorOnSurfaceDark),
            inverseOnSurface = Color(MorphTokens.colorSurfaceDark),
            inversePrimary = Color(MorphTokens.colorBlue500),
            // Misc
            scrim = Color.Black,
            success = Color(0xFF30D158),
            warning = Color(0xFFFFB340)
        )

        /**
         * Pixel (M3) 色板 — 从 Context Theme 读取 M3 语义色。
         *
         * 这是 View/Compose 颜色真相源统一的关键：
         * 与 View 体系的 `MorphTheme.morphColorPrimary(context)` 读取同一 Theme，
         * 确保在 Dynamic Color 设备上双体系呈现像素级一致的颜色。
         *
         * 若 Theme 中缺少 M3 属性（如宿主未使用 M3 主题），降级到 MorphTokens 默认值。
         *
         * @param context 上下文，用于读取 Theme 属性
         * @param isDark 是否为暗色模式；影响降级到 MorphTokens 时选择亮色/暗色默认值
         */
        fun pixelFromContext(context: Context, isDark: Boolean = false): MorphColorPalette {
            val fallback = if (isDark) pixelDarkFallback else pixelLightFallback
            return MorphColorPalette(
                // Primary
                primary = resolveM3Color(context, com.google.android.material.R.attr.colorPrimary, fallback.primary),
                onPrimary = resolveM3Color(context, com.google.android.material.R.attr.colorOnPrimary, fallback.onPrimary),
                primaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorPrimaryContainer, fallback.primaryContainer),
                onPrimaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorOnPrimaryContainer, fallback.onPrimaryContainer),
                // Secondary
                secondary = resolveM3Color(context, com.google.android.material.R.attr.colorSecondary, fallback.secondary),
                onSecondary = resolveM3Color(context, com.google.android.material.R.attr.colorOnSecondary, fallback.onSecondary),
                secondaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorSecondaryContainer, fallback.secondaryContainer),
                onSecondaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorOnSecondaryContainer, fallback.onSecondaryContainer),
                // Tertiary
                tertiary = resolveM3Color(context, com.google.android.material.R.attr.colorTertiary, fallback.tertiary),
                onTertiary = resolveM3Color(context, com.google.android.material.R.attr.colorOnTertiary, fallback.onTertiary),
                tertiaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorTertiaryContainer, fallback.tertiaryContainer),
                onTertiaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorOnTertiaryContainer, fallback.onTertiaryContainer),
                // Error
                error = resolveM3Color(context, com.google.android.material.R.attr.colorError, fallback.error),
                onError = resolveM3Color(context, com.google.android.material.R.attr.colorOnError, fallback.onError),
                errorContainer = resolveM3Color(context, com.google.android.material.R.attr.colorErrorContainer, fallback.errorContainer),
                onErrorContainer = resolveM3Color(context, com.google.android.material.R.attr.colorOnErrorContainer, fallback.onErrorContainer),
                // Surface
                surface = resolveM3Color(context, com.google.android.material.R.attr.colorSurface, fallback.surface),
                onSurface = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurface, fallback.onSurface),
                surfaceVariant = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceVariant, fallback.surfaceVariant),
                onSurfaceVariant = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurfaceVariant, fallback.onSurfaceVariant),
                surfaceDim = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceDim, fallback.surfaceDim),
                surfaceBright = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceBright, fallback.surfaceBright),
                surfaceContainerLowest = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceContainerLowest, fallback.surfaceContainerLowest),
                surfaceContainerLow = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceContainerLow, fallback.surfaceContainerLow),
                surfaceContainer = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceContainer, fallback.surfaceContainer),
                surfaceContainerHigh = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, fallback.surfaceContainerHigh),
                surfaceContainerHighest = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceContainerHighest, fallback.surfaceContainerHighest),
                // Outline
                outline = resolveM3Color(context, com.google.android.material.R.attr.colorOutline, fallback.outline),
                outlineVariant = resolveM3Color(context, com.google.android.material.R.attr.colorOutlineVariant, fallback.outlineVariant),
                // Background
                background = resolveM3Color(context, android.R.attr.colorBackground, fallback.background),
                onBackground = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurface, fallback.onBackground),
                // Inverse (Material 1.12.0 无 colorInverseSurface/colorInverseOnSurface 属性)
                inverseSurface = fallback.inverseSurface,
                inverseOnSurface = fallback.inverseOnSurface,
                inversePrimary = resolveM3Color(context, com.google.android.material.R.attr.colorPrimaryInverse, fallback.inversePrimary),
                // Misc
                scrim = Color.Black,
                success = fallback.success,
                warning = fallback.warning
            )
        }

        /**
         * 从 Context 的 Theme 读取 M3 语义色，失败时降级到提供的 fallback 值。
         */
        private fun resolveM3Color(context: Context, attr: Int, fallback: Color): Color {
            return try {
                val resolved = MaterialColors.getColor(context, attr, fallback.toArgb())
                Color(resolved)
            } catch (e: Exception) {
                fallback
            }
        }

        /**
         * Pixel 亮色降级色板 — 当 Context Theme 缺少 M3 属性时使用。
         *
         * 值来源于 [MorphTokens]，与 iOS 亮色共享 Token 基色，确保一致性。
         */
        private val pixelLightFallback = FallbackColors(
            primary = Color(MorphTokens.colorBlue500),
            onPrimary = Color(MorphTokens.colorOnPrimary),
            primaryContainer = Color(MorphTokens.colorPrimaryContainer),
            onPrimaryContainer = Color(MorphTokens.colorOnPrimaryContainer),
            secondary = Color(MorphTokens.colorSecondary),
            onSecondary = Color(MorphTokens.colorOnSecondary),
            secondaryContainer = Color(MorphTokens.colorSecondaryContainer),
            onSecondaryContainer = Color(MorphTokens.colorOnSecondaryContainer),
            tertiary = Color(MorphTokens.colorTertiary),
            onTertiary = Color(MorphTokens.colorOnTertiary),
            tertiaryContainer = Color(MorphTokens.colorTertiaryContainer),
            onTertiaryContainer = Color(MorphTokens.colorOnTertiaryContainer),
            error = Color(MorphTokens.colorRed500),
            onError = Color(MorphTokens.colorOnError),
            errorContainer = Color(MorphTokens.colorErrorContainer),
            onErrorContainer = Color(MorphTokens.colorOnErrorContainer),
            surface = Color(MorphTokens.colorSurface),
            onSurface = Color(MorphTokens.colorOnSurface),
            surfaceVariant = Color(MorphTokens.colorSurfaceVariant),
            onSurfaceVariant = Color(MorphTokens.colorOnSurfaceVariant),
            surfaceDim = Color(MorphTokens.colorSurfaceDim),
            surfaceBright = Color(MorphTokens.colorSurfaceBright),
            surfaceContainerLowest = Color(MorphTokens.colorSurfaceContainerLowest),
            surfaceContainerLow = Color(MorphTokens.colorSurfaceContainerLow),
            surfaceContainer = Color(MorphTokens.colorSurfaceContainer),
            surfaceContainerHigh = Color(MorphTokens.colorSurfaceContainerHigh),
            surfaceContainerHighest = Color(MorphTokens.colorSurfaceContainerHighest),
            outline = Color(MorphTokens.colorOutline),
            outlineVariant = Color(MorphTokens.colorOutlineVariant),
            background = Color(MorphTokens.colorBackground),
            onBackground = Color(MorphTokens.colorOnSurface),
            inverseSurface = Color(MorphTokens.colorOnSurface),
            inverseOnSurface = Color(MorphTokens.colorSurface),
            inversePrimary = Color(MorphTokens.colorBlue100),
            success = Color(MorphTokens.colorGreen500),
            warning = Color(MorphTokens.colorOrange500)
        )

        /**
         * Pixel 暗色降级色板 — 当 Context Theme 缺少 M3 暗色属性时使用。
         *
         * 值来源于 [MorphTokens] 的 Dark 系列常量。此前 Pixel 模式仅有亮色降级，
         * 暗色模式下会错误地回退到亮色 Token，导致暗色模式下出现刺眼的亮色背景。
         */
        private val pixelDarkFallback = FallbackColors(
            primary = Color(MorphTokens.colorBlue100),
            onPrimary = Color(0xFF001D3F),
            primaryContainer = Color(MorphTokens.colorBlue700),
            onPrimaryContainer = Color(MorphTokens.colorBlue100),
            secondary = Color(MorphTokens.colorSecondaryDark),
            onSecondary = Color(MorphTokens.colorOnSecondaryDark),
            secondaryContainer = Color(MorphTokens.colorSecondaryContainerDark),
            onSecondaryContainer = Color(MorphTokens.colorOnSecondaryContainerDark),
            tertiary = Color(MorphTokens.colorTertiaryDark),
            onTertiary = Color(MorphTokens.colorOnTertiaryDark),
            tertiaryContainer = Color(MorphTokens.colorTertiaryContainerDark),
            onTertiaryContainer = Color(MorphTokens.colorOnTertiaryContainerDark),
            error = Color(0xFFFF6961),
            onError = Color(MorphTokens.colorOnErrorDark),
            errorContainer = Color(MorphTokens.colorErrorContainerDark),
            onErrorContainer = Color(MorphTokens.colorOnErrorContainerDark),
            surface = Color(MorphTokens.colorSurfaceDark),
            onSurface = Color(MorphTokens.colorOnSurfaceDark),
            surfaceVariant = Color(MorphTokens.colorSurfaceVariantDark),
            onSurfaceVariant = Color(MorphTokens.colorOnSurfaceVariantDark),
            surfaceDim = Color(MorphTokens.colorSurfaceDimDark),
            surfaceBright = Color(MorphTokens.colorSurfaceBrightDark),
            surfaceContainerLowest = Color(MorphTokens.colorSurfaceContainerLowestDark),
            surfaceContainerLow = Color(MorphTokens.colorSurfaceContainerLowDark),
            surfaceContainer = Color(MorphTokens.colorSurfaceContainerDark),
            surfaceContainerHigh = Color(MorphTokens.colorSurfaceContainerHighDark),
            surfaceContainerHighest = Color(MorphTokens.colorSurfaceContainerHighestDark),
            outline = Color(MorphTokens.colorOutlineDark),
            outlineVariant = Color(MorphTokens.colorOutlineVariantDark),
            background = Color(MorphTokens.colorBackgroundDark),
            onBackground = Color(MorphTokens.colorOnSurfaceDark),
            inverseSurface = Color(MorphTokens.colorOnSurfaceDark),
            inverseOnSurface = Color(MorphTokens.colorSurfaceDark),
            inversePrimary = Color(MorphTokens.colorBlue500),
            success = Color(0xFF30D158),
            warning = Color(0xFFFFB340)
        )
    }

    /**
     * 降级色板内部数据结构。
     *
     * 将 Pixel 模式的亮色/暗色降级数据集中管理，避免 [pixelFromContext] 方法参数膨胀。
     * 字段命名与 [MorphColorPalette] 一一对应，便于审查一致性。
     */
    private data class FallbackColors(
        val primary: Color,
        val onPrimary: Color,
        val primaryContainer: Color,
        val onPrimaryContainer: Color,
        val secondary: Color,
        val onSecondary: Color,
        val secondaryContainer: Color,
        val onSecondaryContainer: Color,
        val tertiary: Color,
        val onTertiary: Color,
        val tertiaryContainer: Color,
        val onTertiaryContainer: Color,
        val error: Color,
        val onError: Color,
        val errorContainer: Color,
        val onErrorContainer: Color,
        val surface: Color,
        val onSurface: Color,
        val surfaceVariant: Color,
        val onSurfaceVariant: Color,
        val surfaceDim: Color,
        val surfaceBright: Color,
        val surfaceContainerLowest: Color,
        val surfaceContainerLow: Color,
        val surfaceContainer: Color,
        val surfaceContainerHigh: Color,
        val surfaceContainerHighest: Color,
        val outline: Color,
        val outlineVariant: Color,
        val background: Color,
        val onBackground: Color,
        val inverseSurface: Color,
        val inverseOnSurface: Color,
        val inversePrimary: Color,
        val success: Color,
        val warning: Color
    )
}

// ═════════════════════════════════════════════════════════════════════════════════
// MorphShape — Compose 形状体系
// ═════════════════════════════════════════════════════════════════════════════════

@Immutable
data class MorphShape(
    val cornerRadiusButton: Int,
    val cornerRadiusCard: Int,
    val cornerRadiusTextField: Int,
    val cornerRadiusSmall: Int,
    val cornerRadiusMedium: Int,
    val cornerRadiusLarge: Int
) {
    companion object {
        fun ios() = MorphShape(
            cornerRadiusButton = MorphTokens.cornerRadiusButtonIos,
            cornerRadiusCard = MorphTokens.cornerRadiusCardIos,
            cornerRadiusTextField = MorphTokens.cornerRadiusTextFieldIos,
            cornerRadiusSmall = MorphTokens.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.cornerRadiusLarge
        )

        fun pixel() = MorphShape(
            cornerRadiusButton = MorphTokens.cornerRadiusButtonPixel,
            cornerRadiusCard = MorphTokens.cornerRadiusCardPixel,
            cornerRadiusTextField = MorphTokens.cornerRadiusTextFieldPixel,
            cornerRadiusSmall = MorphTokens.cornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.cornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.cornerRadiusLarge
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// CompositionLocal
// ═════════════════════════════════════════════════════════════════════════════════

val LocalMorphColors = staticCompositionLocalOf { MorphColorPalette.iosLight() }
val LocalMorphShape = staticCompositionLocalOf { MorphShape.ios() }

/**
 * Compose 交互模式 CompositionLocal。
 *
 * 类型委托至 [InteractionMode]（`com.morphkit.core`），消除以往 Compose 侧
 * `MorphInteractionMode` 与 View 侧 `InteractionMode` 的双重定义。
 */
val LocalMorphInteractionMode = staticCompositionLocalOf { InteractionMode.IOS }

/**
 * Compose 风格策略 CompositionLocal。
 *
 * 类型委托至 [StylePolicy]（`com.morphkit.core`），消除以往 `MorphStyle` 枚举。
 */
val LocalMorphStylePolicy = staticCompositionLocalOf { StylePolicy.AUTO }

// ═════════════════════════════════════════════════════════════════════════════════
// MorphTheme Composable
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 主题包裹器。
 *
 * ## 颜色真相源统一
 *
 * - **IOS 模式**：从 [MorphTokens] 读取（iOS 风格不依赖 Dynamic Color）
 * - **PIXEL 模式**：从 Context Theme 读取 M3 语义色（与 View 体系读取同一 Theme，
 *   支持 Dynamic Color，确保双体系像素级一致）；暗色模式下降级到 MorphTokens Dark 系列
 * - **AUTO 模式**：根据设备特性自动选择
 *
 * @param themeStyle 主题风格策略，默认 [StylePolicy.AUTO]
 * @param content 子 Composable 内容
 */
@Composable
fun MorphTheme(
    themeStyle: StylePolicy = StylePolicy.AUTO,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val resolvedStyle = resolveStyle(themeStyle)

    // ── 颜色选择：iOS 从 Token，Pixel 从 Context Theme（支持暗色降级） ──
    // resolveStyle() 保证返回 IOS 或 PIXEL，不会返回 AUTO
    val colors = when (resolvedStyle) {
        StylePolicy.IOS -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
        StylePolicy.PIXEL -> MorphColorPalette.pixelFromContext(context, isDark)
        StylePolicy.AUTO -> MorphColorPalette.iosLight() // unreachable, for exhaustiveness
    }

    val shape = when (resolvedStyle) {
        StylePolicy.IOS -> MorphShape.ios()
        StylePolicy.PIXEL -> MorphShape.pixel()
        StylePolicy.AUTO -> MorphShape.ios() // unreachable
    }

    val interactionMode = when (resolvedStyle) {
        StylePolicy.IOS -> InteractionMode.IOS
        StylePolicy.PIXEL -> InteractionMode.MATERIAL
        StylePolicy.AUTO -> InteractionMode.IOS // unreachable
    }

    val typography = morphTypography()

    CompositionLocalProvider(
        LocalMorphColors provides colors,
        LocalMorphShape provides shape,
        LocalMorphInteractionMode provides interactionMode,
        LocalMorphStylePolicy provides resolvedStyle
    ) {
        MaterialTheme(
            colorScheme = material3ColorScheme(colors),
            typography = typography,
            content = content
        )
    }
}

@Composable
private fun resolveStyle(style: StylePolicy): StylePolicy {
    if (style != StylePolicy.AUTO) return style
    val context = LocalContext.current
    return resolveAutoStyle(context)
}

/**
 * AUTO 模式：委托 [MorphStyleResolver] 判断。
 * 如果最终 Theme 是 Pixel 主题，则走 PIXEL；否则走 IOS。
 */
private fun resolveAutoStyle(context: Context): StylePolicy {
    return try {
        val themeResId = MorphStyleResolver.resolve(context, StylePolicy.AUTO)
        if (themeResId == com.morphkit.R.style.Theme_MorphKit_Pixel) StylePolicy.PIXEL else StylePolicy.IOS
    } catch (e: Exception) {
        StylePolicy.IOS  // 降级：解析失败时默认 IOS
    }
}

private fun morphTypography(): Typography {
    val ff = FontFamily.SansSerif
    return Typography(
        displayLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.fontSizeLargeTitle.sp),
        displayMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.fontSizeTitle1.sp),
        displaySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.fontSizeTitle2.sp),
        headlineLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.fontSizeTitle3.sp),
        headlineMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.fontSizeHeadline.sp),
        headlineSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.fontSizeHeadline.sp),
        titleLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.fontSizeTitle3.sp),
        titleMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.fontSizeHeadline.sp),
        titleSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeSubheadline.sp),
        bodyLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeBody.sp),
        bodyMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeCallout.sp),
        bodySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeSubheadline.sp),
        labelLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeButton.sp),
        labelMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeCaption1.sp),
        labelSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.fontSizeCaption2.sp)
    )
}

/**
 * 将 MorphColorPalette 映射到 M3 ColorScheme。
 *
 * 每个 M3 角色与 MorphColorPalette 字段一一对应，消除以往 secondary/tertiary 复用 primary、
 * surfaceContainer 系列缺失、onError 误用 onPrimary 等问题。
 */
private fun material3ColorScheme(colors: MorphColorPalette) = androidx.compose.material3.ColorScheme(
    primary = colors.primary,
    onPrimary = colors.onPrimary,
    primaryContainer = colors.primaryContainer,
    onPrimaryContainer = colors.onPrimaryContainer,
    secondary = colors.secondary,
    onSecondary = colors.onSecondary,
    secondaryContainer = colors.secondaryContainer,
    onSecondaryContainer = colors.onSecondaryContainer,
    tertiary = colors.tertiary,
    onTertiary = colors.onTertiary,
    tertiaryContainer = colors.tertiaryContainer,
    onTertiaryContainer = colors.onTertiaryContainer,
    error = colors.error,
    onError = colors.onError,
    errorContainer = colors.errorContainer,
    onErrorContainer = colors.onErrorContainer,
    background = colors.background,
    onBackground = colors.onBackground,
    surface = colors.surface,
    onSurface = colors.onSurface,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.onSurfaceVariant,
    surfaceDim = colors.surfaceDim,
    surfaceBright = colors.surfaceBright,
    surfaceContainerLowest = colors.surfaceContainerLowest,
    surfaceContainerLow = colors.surfaceContainerLow,
    surfaceContainer = colors.surfaceContainer,
    surfaceContainerHigh = colors.surfaceContainerHigh,
    surfaceContainerHighest = colors.surfaceContainerHighest,
    surfaceTint = colors.primary,
    inverseSurface = colors.inverseSurface,
    inverseOnSurface = colors.inverseOnSurface,
    inversePrimary = colors.inversePrimary,
    outline = colors.outline,
    outlineVariant = colors.outlineVariant,
    scrim = colors.scrim
)
