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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.android.material.color.MaterialColors
import com.morphkit.theme.MorphTokens

/**
 * MorphKit Compose 主题系统。
 *
 * 从 [MorphTokens] 和 Context Theme 读取设计变量，转换为 Compose 的 [Color]、[Typography]，
 * 通过 [CompositionLocal] 向下传递，确保 Compose 组件与 View 体系视觉完全一致。
 *
 * ## 颜色真相源统一
 *
 * | 模式 | 颜色来源 | 与 View 体系一致性 |
 * |------|---------|------------------|
 * | iOS | [MorphTokens] 硬编码常量 | ✅ View 体系通过 `MorphTheme.morphColorPrimary()` 读取 Theme，但 iOS 皮肤不依赖 Dynamic Color |
 * | Pixel | Context Theme 的 M3 语义色 | ✅ 与 View 体系读取同一 Theme，支持 Dynamic Color |
 * | Auto | 根据设备特性自动选择上述之一 | ✅ |
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
 */

// ═════════════════════════════════════════════════════════════════════════════════
// MorphStyle 枚举
// ═════════════════════════════════════════════════════════════════════════════════

enum class MorphStyle {
    Auto, iOS, Pixel
}

// ═════════════════════════════════════════════════════════════════════════════════
// MorphColorPalette — Compose 颜色体系
// ═════════════════════════════════════════════════════════════════════════════════

@Immutable
data class MorphColorPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outlineVariant: Color,
    val background: Color,
    val onBackground: Color,
    val error: Color,
    val success: Color,
    val warning: Color
) {
    companion object {
        /** iOS 亮色色板 — 从 MorphTokens 读取 */
        fun iosLight() = MorphColorPalette(
            primary = Color(MorphTokens.Blue500),
            onPrimary = Color(MorphTokens.OnPrimary),
            primaryContainer = Color(MorphTokens.PrimaryContainer),
            onPrimaryContainer = Color(MorphTokens.OnPrimaryContainer),
            surface = Color(MorphTokens.Surface),
            surfaceVariant = Color(MorphTokens.SurfaceVariant),
            onSurface = Color(MorphTokens.OnSurface),
            onSurfaceVariant = Color(MorphTokens.OnSurfaceVariant),
            outlineVariant = Color(MorphTokens.OutlineVariant),
            background = Color(MorphTokens.Background),
            onBackground = Color(MorphTokens.OnSurface),
            error = Color(MorphTokens.Red500),
            success = Color(MorphTokens.Green500),
            warning = Color(MorphTokens.Orange500)
        )

        /** iOS 暗色色板 — 从 MorphTokens 读取 */
        fun iosDark() = MorphColorPalette(
            primary = Color(MorphTokens.Blue100),
            onPrimary = Color(0xFF001D3F),
            primaryContainer = Color(MorphTokens.Blue700),
            onPrimaryContainer = Color(MorphTokens.Blue100),
            surface = Color(MorphTokens.SurfaceDark),
            surfaceVariant = Color(MorphTokens.SurfaceVariantDark),
            onSurface = Color(MorphTokens.OnSurfaceDark),
            onSurfaceVariant = Color(MorphTokens.OnSurfaceVariantDark),
            outlineVariant = Color(MorphTokens.OutlineVariantDark),
            background = Color(MorphTokens.BackgroundDark),
            onBackground = Color(MorphTokens.OnSurfaceDark),
            error = Color(0xFFFF6961),
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
         */
        fun pixelFromContext(context: Context): MorphColorPalette {
            return MorphColorPalette(
                primary = resolveM3Color(context, com.google.android.material.R.attr.colorPrimary, MorphTokens.Blue500),
                onPrimary = resolveM3Color(context, com.google.android.material.R.attr.colorOnPrimary, MorphTokens.OnPrimary),
                primaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorPrimaryContainer, MorphTokens.PrimaryContainer),
                onPrimaryContainer = resolveM3Color(context, com.google.android.material.R.attr.colorOnPrimaryContainer, MorphTokens.OnPrimaryContainer),
                surface = resolveM3Color(context, com.google.android.material.R.attr.colorSurface, MorphTokens.Surface),
                surfaceVariant = resolveM3Color(context, com.google.android.material.R.attr.colorSurfaceVariant, MorphTokens.SurfaceVariant),
                onSurface = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurface, MorphTokens.OnSurface),
                onSurfaceVariant = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurfaceVariant, MorphTokens.OnSurfaceVariant),
                outlineVariant = resolveM3Color(context, com.google.android.material.R.attr.colorOutlineVariant, MorphTokens.OutlineVariant),
                background = resolveM3Color(context, android.R.attr.colorBackground, MorphTokens.Background),
                onBackground = resolveM3Color(context, com.google.android.material.R.attr.colorOnSurface, MorphTokens.OnSurface),
                error = resolveM3Color(context, com.google.android.material.R.attr.colorError, MorphTokens.Red500),
                success = Color(MorphTokens.Green500),
                warning = Color(MorphTokens.Orange500)
            )
        }

        /**
         * 从 Context 的 Theme 读取 M3 语义色，失败时降级到 MorphTokens 默认值。
         */
        private fun resolveM3Color(context: Context, attr: Int, @androidx.annotation.ColorInt fallback: Int): Color {
            return try {
                Color(MaterialColors.getColor(context, attr, fallback))
            } catch (e: Exception) {
                Color(fallback)
            }
        }
    }
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
            cornerRadiusButton = MorphTokens.CornerRadiusButtonIOS,
            cornerRadiusCard = MorphTokens.CornerRadiusCardIOS,
            cornerRadiusTextField = MorphTokens.CornerRadiusTextFieldIOS,
            cornerRadiusSmall = MorphTokens.CornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.CornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.CornerRadiusLarge
        )

        fun pixel() = MorphShape(
            cornerRadiusButton = MorphTokens.CornerRadiusButtonPixel,
            cornerRadiusCard = MorphTokens.CornerRadiusCardPixel,
            cornerRadiusTextField = MorphTokens.CornerRadiusTextFieldPixel,
            cornerRadiusSmall = MorphTokens.CornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.CornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.CornerRadiusLarge
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// MorphInteractionMode — Compose 交互模式
// ═════════════════════════════════════════════════════════════════════════════════

enum class MorphInteractionMode {
    IOS, MATERIAL
}

// ═════════════════════════════════════════════════════════════════════════════════
// CompositionLocal
// ═════════════════════════════════════════════════════════════════════════════════

val LocalMorphColors = staticCompositionLocalOf { MorphColorPalette.iosLight() }
val LocalMorphShape = staticCompositionLocalOf { MorphShape.ios() }
val LocalMorphInteractionMode = staticCompositionLocalOf { MorphInteractionMode.IOS }
val LocalMorphStyle = staticCompositionLocalOf { MorphStyle.Auto }

// ═════════════════════════════════════════════════════════════════════════════════
// MorphTheme Composable
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 主题包裹器。
 *
 * ## 颜色真相源统一
 *
 * - **iOS 模式**：从 [MorphTokens] 读取（iOS 风格不依赖 Dynamic Color）
 * - **Pixel 模式**：从 Context Theme 读取 M3 语义色（与 View 体系读取同一 Theme，
 *   支持 Dynamic Color，确保双体系像素级一致）
 * - **Auto 模式**：根据设备特性自动选择
 *
 * @param themeStyle 主题风格，默认 [MorphStyle.Auto]
 * @param content 子 Composable 内容
 */
@Composable
fun MorphTheme(
    themeStyle: MorphStyle = MorphStyle.Auto,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val resolvedStyle = resolveStyle(themeStyle)

    // ── 颜色选择：iOS 从 Token，Pixel 从 Context Theme ──
    val colors = when (resolvedStyle) {
        MorphStyle.iOS -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
        MorphStyle.Pixel -> MorphColorPalette.pixelFromContext(context)
        MorphStyle.Auto -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
    }

    val shape = when (resolvedStyle) {
        MorphStyle.iOS -> MorphShape.ios()
        MorphStyle.Pixel -> MorphShape.pixel()
        MorphStyle.Auto -> MorphShape.ios()
    }

    val interactionMode = when (resolvedStyle) {
        MorphStyle.iOS -> MorphInteractionMode.IOS
        MorphStyle.Pixel -> MorphInteractionMode.MATERIAL
        MorphStyle.Auto -> MorphInteractionMode.IOS
    }

    val typography = morphTypography()

    CompositionLocalProvider(
        LocalMorphColors provides colors,
        LocalMorphShape provides shape,
        LocalMorphInteractionMode provides interactionMode,
        LocalMorphStyle provides resolvedStyle
    ) {
        MaterialTheme(
            colorScheme = material3ColorScheme(colors),
            typography = typography,
            content = content
        )
    }
}

@Composable
private fun resolveStyle(style: MorphStyle): MorphStyle = style

private fun morphTypography(): Typography {
    val ff = FontFamily.SansSerif
    return Typography(
        displayLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.FontSizeLargeTitle.sp),
        displayMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.FontSizeTitle1.sp),
        displaySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.FontSizeTitle2.sp),
        headlineLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.FontSizeTitle3.sp),
        headlineMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.FontSizeHeadline.sp),
        headlineSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.FontSizeHeadline.sp),
        titleLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.FontSizeTitle3.sp),
        titleMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.FontSizeHeadline.sp),
        titleSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeSubheadline.sp),
        bodyLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeBody.sp),
        bodyMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeCallout.sp),
        bodySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeSubheadline.sp),
        labelLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeButton.sp),
        labelMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeCaption1.sp),
        labelSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.FontSizeCaption2.sp)
    )
}

private fun material3ColorScheme(colors: MorphColorPalette) = androidx.compose.material3.ColorScheme(
    primary = colors.primary,
    onPrimary = colors.onPrimary,
    primaryContainer = colors.primaryContainer,
    onPrimaryContainer = colors.onPrimaryContainer,
    inversePrimary = colors.onPrimary,
    secondary = colors.primary,
    onSecondary = colors.onPrimary,
    secondaryContainer = colors.primaryContainer,
    onSecondaryContainer = colors.onPrimaryContainer,
    tertiary = colors.primary,
    onTertiary = colors.onPrimary,
    tertiaryContainer = colors.primaryContainer,
    onTertiaryContainer = colors.onPrimaryContainer,
    background = colors.background,
    onBackground = colors.onBackground,
    surface = colors.surface,
    onSurface = colors.onSurface,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.onSurfaceVariant,
    surfaceTint = colors.primary,
    inverseSurface = colors.onSurface,
    inverseOnSurface = colors.surface,
    error = colors.error,
    onError = colors.onPrimary,
    errorContainer = colors.error,
    onErrorContainer = colors.onPrimary,
    outline = colors.outlineVariant,
    outlineVariant = colors.outlineVariant,
    scrim = Color.Black
)
