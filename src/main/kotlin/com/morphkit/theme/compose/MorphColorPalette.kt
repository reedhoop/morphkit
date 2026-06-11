package com.morphkit.theme.compose

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.MaterialColors
import com.morphkit.theme.MorphTokens

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
            onPrimary = Color(MorphTokens.colorOnPrimaryDark),
            primaryContainer = Color(MorphTokens.colorPrimaryContainerDark),
            onPrimaryContainer = Color(MorphTokens.colorOnPrimaryContainerDark),
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
            error = Color(MorphTokens.colorErrorDark),
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
            success = Color(MorphTokens.colorSuccessDark),
            warning = Color(MorphTokens.colorWarningDark)
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
         * Pixel 亮色降级色板 — 直接复用 iOS 亮色 Token。
         *
         * 值来源于 [MorphTokens]，与 iOS 亮色共享 Token 基色，确保一致性。
         * 通过 [MorphColorPalette.toFallback] 转换，新增 Token 只需在 iosLight() 中添加一次。
         */
        private val pixelLightFallback: FallbackColors by lazy { iosLight().toFallback() }

        /**
         * Pixel 暗色降级色板 — 直接复用 iOS 暗色 Token。
         *
         * 值来源于 [MorphTokens] 的 Dark 系列常量。此前 Pixel 模式仅有亮色降级，
         * 暗色模式下会错误地回退到亮色 Token，导致暗色模式下出现刺眼的亮色背景。
         * 通过 [MorphColorPalette.toFallback] 转换，新增 Token 只需在 iosDark() 中添加一次。
         */
        private val pixelDarkFallback: FallbackColors by lazy { iosDark().toFallback() }
    }

    /**
     * 将 [MorphColorPalette] 转换为 [FallbackColors]。
     *
     * 用于 Pixel 模式降级色板的自动生成，确保新增 Token 字段只需在
     * [iosLight] / [iosDark] 中添加一次，pixelFallback 自动同步。
     */
    private fun toFallback() = FallbackColors(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceDim = surfaceDim,
        surfaceBright = surfaceBright,
        surfaceContainerLowest = surfaceContainerLowest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        outline = outline,
        outlineVariant = outlineVariant,
        background = background,
        onBackground = onBackground,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        inversePrimary = inversePrimary,
        success = success,
        warning = warning
    )

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
