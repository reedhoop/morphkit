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
        /** iOS 亮色色板 — 从 MorphTokens 读取（缓存单例，避免重复构造） */
        private val cachedIosLight: MorphColorPalette by lazy {
            MorphColorPalette(
                // Primary
                primary = Color(MorphTokens.Colors.colorBlue500),
                onPrimary = Color(MorphTokens.Colors.colorOnPrimary),
                primaryContainer = Color(MorphTokens.Colors.colorPrimaryContainer),
                onPrimaryContainer = Color(MorphTokens.Colors.colorOnPrimaryContainer),
                // Secondary
                secondary = Color(MorphTokens.Colors.colorSecondary),
                onSecondary = Color(MorphTokens.Colors.colorOnSecondary),
                secondaryContainer = Color(MorphTokens.Colors.colorSecondaryContainer),
                onSecondaryContainer = Color(MorphTokens.Colors.colorOnSecondaryContainer),
                // Tertiary
                tertiary = Color(MorphTokens.Colors.colorTertiary),
                onTertiary = Color(MorphTokens.Colors.colorOnTertiary),
                tertiaryContainer = Color(MorphTokens.Colors.colorTertiaryContainer),
                onTertiaryContainer = Color(MorphTokens.Colors.colorOnTertiaryContainer),
                // Error
                error = Color(MorphTokens.Colors.colorRed500),
                onError = Color(MorphTokens.Colors.colorOnError),
                errorContainer = Color(MorphTokens.Colors.colorErrorContainer),
                onErrorContainer = Color(MorphTokens.Colors.colorOnErrorContainer),
                // Surface
                surface = Color(MorphTokens.Colors.colorSurface),
                onSurface = Color(MorphTokens.Colors.colorOnSurface),
                surfaceVariant = Color(MorphTokens.Colors.colorSurfaceVariant),
                onSurfaceVariant = Color(MorphTokens.Colors.colorOnSurfaceVariant),
                surfaceDim = Color(MorphTokens.Colors.colorSurfaceDim),
                surfaceBright = Color(MorphTokens.Colors.colorSurfaceBright),
                surfaceContainerLowest = Color(MorphTokens.Colors.colorSurfaceContainerLowest),
                surfaceContainerLow = Color(MorphTokens.Colors.colorSurfaceContainerLow),
                surfaceContainer = Color(MorphTokens.Colors.colorSurfaceContainer),
                surfaceContainerHigh = Color(MorphTokens.Colors.colorSurfaceContainerHigh),
                surfaceContainerHighest = Color(MorphTokens.Colors.colorSurfaceContainerHighest),
                // Outline
                outline = Color(MorphTokens.Colors.colorOutline),
                outlineVariant = Color(MorphTokens.Colors.colorOutlineVariant),
                // Background
                background = Color(MorphTokens.Colors.colorBackground),
                onBackground = Color(MorphTokens.Colors.colorOnSurface),
                // Inverse
                inverseSurface = Color(MorphTokens.Colors.colorOnSurface),
                inverseOnSurface = Color(MorphTokens.Colors.colorSurface),
                inversePrimary = Color(MorphTokens.Colors.colorBlue100),
                // Misc
                scrim = Color(MorphTokens.Colors.colorScrim),
                success = Color(MorphTokens.Colors.colorGreen500),
                warning = Color(MorphTokens.Colors.colorOrange500)
            )
        }
        fun iosLight(): MorphColorPalette = cachedIosLight

        /** iOS 暗色色板 — 从 MorphTokens 读取（缓存单例，避免重复构造） */
        private val cachedIosDark: MorphColorPalette by lazy {
            MorphColorPalette(
                // Primary
                primary = Color(MorphTokens.Colors.colorBlue100),
                onPrimary = Color(MorphTokens.Colors.colorOnPrimaryDark),
                primaryContainer = Color(MorphTokens.Colors.colorPrimaryContainerDark),
                onPrimaryContainer = Color(MorphTokens.Colors.colorOnPrimaryContainerDark),
                // Secondary
                secondary = Color(MorphTokens.Colors.colorSecondaryDark),
                onSecondary = Color(MorphTokens.Colors.colorOnSecondaryDark),
                secondaryContainer = Color(MorphTokens.Colors.colorSecondaryContainerDark),
                onSecondaryContainer = Color(MorphTokens.Colors.colorOnSecondaryContainerDark),
                // Tertiary
                tertiary = Color(MorphTokens.Colors.colorTertiaryDark),
                onTertiary = Color(MorphTokens.Colors.colorOnTertiaryDark),
                tertiaryContainer = Color(MorphTokens.Colors.colorTertiaryContainerDark),
                onTertiaryContainer = Color(MorphTokens.Colors.colorOnTertiaryContainerDark),
                // Error
                error = Color(MorphTokens.Colors.colorErrorDark),
                onError = Color(MorphTokens.Colors.colorOnErrorDark),
                errorContainer = Color(MorphTokens.Colors.colorErrorContainerDark),
                onErrorContainer = Color(MorphTokens.Colors.colorOnErrorContainerDark),
                // Surface
                surface = Color(MorphTokens.Colors.colorSurfaceDark),
                onSurface = Color(MorphTokens.Colors.colorOnSurfaceDark),
                surfaceVariant = Color(MorphTokens.Colors.colorSurfaceVariantDark),
                onSurfaceVariant = Color(MorphTokens.Colors.colorOnSurfaceVariantDark),
                surfaceDim = Color(MorphTokens.Colors.colorSurfaceDimDark),
                surfaceBright = Color(MorphTokens.Colors.colorSurfaceBrightDark),
                surfaceContainerLowest = Color(MorphTokens.Colors.colorSurfaceContainerLowestDark),
                surfaceContainerLow = Color(MorphTokens.Colors.colorSurfaceContainerLowDark),
                surfaceContainer = Color(MorphTokens.Colors.colorSurfaceContainerDark),
                surfaceContainerHigh = Color(MorphTokens.Colors.colorSurfaceContainerHighDark),
                surfaceContainerHighest = Color(MorphTokens.Colors.colorSurfaceContainerHighestDark),
                // Outline
                outline = Color(MorphTokens.Colors.colorOutlineDark),
                outlineVariant = Color(MorphTokens.Colors.colorOutlineVariantDark),
                // Background
                background = Color(MorphTokens.Colors.colorBackgroundDark),
                onBackground = Color(MorphTokens.Colors.colorOnSurfaceDark),
                // Inverse
                inverseSurface = Color(MorphTokens.Colors.colorOnSurfaceDark),
                inverseOnSurface = Color(MorphTokens.Colors.colorSurfaceDark),
                inversePrimary = Color(MorphTokens.Colors.colorBlue500),
                // Misc
                scrim = Color(MorphTokens.Colors.colorScrim),
                success = Color(MorphTokens.Colors.colorSuccessDark),
                warning = Color(MorphTokens.Colors.colorWarningDark)
            )
        }
        fun iosDark(): MorphColorPalette = cachedIosDark

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
                // Inverse — Material 1.12.0 尚未提供 colorInverseSurface/colorInverseOnSurface 属性，使用 fallback
                inverseSurface = fallback.inverseSurface,
                inverseOnSurface = fallback.inverseOnSurface,
                inversePrimary = resolveM3Color(context, com.google.android.material.R.attr.colorPrimaryInverse, fallback.inversePrimary),
                // Misc
                scrim = Color(MorphTokens.Colors.colorScrim),
                success = fallback.success,
                warning = fallback.warning
            )
        }

        /**
         * 从 Context 的 Theme 读取 M3 语义色，失败时降级到提供的 fallback 值。
         *
         * 区分两种失败场景：
         * - [Resources.NotFoundException]：Theme 中缺少该属性（宿主未使用 M3 主题），静默降级
         * - 其他异常：Theme 可能损坏，记录警告后降级
         */
        private fun resolveM3Color(context: Context, attr: Int, fallback: Color): Color {
            return try {
                val resolved = MaterialColors.getColor(context, attr, fallback.toArgb())
                Color(resolved)
            } catch (e: android.content.res.Resources.NotFoundException) {
                // Theme 中缺少 M3 属性 — 静默降级到 MorphTokens 默认值
                fallback
            } catch (e: Exception) {
                // Theme 可能损坏 — 记录警告后降级
                android.util.Log.w("MorphKit", "resolveM3Color: Theme 解析异常 attr=0x${attr.toString(16)}, 降级到默认值", e)
                fallback
            }
        }

        /**
         * Pixel 亮色降级色板 — 直接复用 iOS 亮色 Token。
         *
         * 值来源于 [MorphTokens]，与 iOS 亮色共享 Token 基色，确保一致性。
         * 直接使用 [MorphColorPalette] 实例，新增 Token 只需在 iosLight() 中添加一次，
         * pixelFromContext 的降级值自动同步，无需维护额外的 FallbackColors 数据结构。
         */
        private val pixelLightFallback: MorphColorPalette by lazy { iosLight() }

        /**
         * Pixel 暗色降级色板 — 直接复用 iOS 暗色 Token。
         *
         * 值来源于 [MorphTokens] 的 Dark 系列常量。此前 Pixel 模式仅有亮色降级，
         * 暗色模式下会错误地回退到亮色 Token，导致暗色模式下出现刺眼的亮色背景。
         * 直接使用 [MorphColorPalette] 实例，新增 Token 只需在 iosDark() 中添加一次，
         * pixelFromContext 的降级值自动同步。
         */
        private val pixelDarkFallback: MorphColorPalette by lazy { iosDark() }
    }
}
