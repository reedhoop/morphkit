package com.morphkit.engine.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.morphkit.engine.MorphTokens

/**
 * MorphKit Compose 主题系统。
 *
 * 从 [MorphTokens] 读取统一设计变量，转换为 Compose 的 [Color]、[Typography]、[Dp]，
 * 通过 [CompositionLocal] 向下传递，确保 Compose 组件与 View 体系视觉完全一致。
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
 *             // 所有业务 UI 必须在此包裹内
 *             MyAppNavigation()
 *         }
 *     }
 * }
 * ```
 *
 * **禁止**业务方直接使用 `androidx.compose.material3.Button`，
 * **必须**使用 [MorphButton]，以保证 ROM 级的交互和视觉统一。
 * 违反此规范将导致 ROM 大促换肤时 Compose 页面无法跟随变化。
 *
 * ## 主题切换
 *
 * ```kotlin
 * // 自动模式（推荐）：根据 Dynamic Color 和暗黑模式自动选择
 * MorphTheme(themeStyle = MorphStyle.Auto) { ... }
 *
 * // 强制 iOS 极简风
 * MorphTheme(themeStyle = MorphStyle.iOS) { ... }
 *
 * // 强制 Pixel (Material You) 原生风
 * MorphTheme(themeStyle = MorphStyle.Pixel) { ... }
 * ```
 *
 * ## 基线保护声明
 *
 * 本 AAR 仅限于替换本 OEM 预装应用内的控件，不具备干涉第三方应用的能力。
 * 若需全局系统级换肤，请结合 Android RRO (Runtime Resource Overlay) 机制使用。
 *
 * @see MorphTokens 统一设计 Token 层
 * @see MorphButton Compose 按钮（交互模式分发）
 */

// ═════════════════════════════════════════════════════════════════════════════════
// MorphStyle 枚举 — 决定 Compose 主题风格
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 主题风格枚举。
 *
 * | 风格 | 视觉特征 | 交互特征 |
 * |------|---------|---------|
 * | [Auto] | 根据 Dynamic Color 和暗黑模式自动选择 | 自动匹配 |
 * | [iOS] | 大圆角 12dp、零涟漪、极简风格 | 按压整体变色 |
 * | [Pixel] | M3 标准圆角 8dp、Material You 风格 | Ripple 涟漪 |
 */
enum class MorphStyle {
    /** 自动模式：根据设备特性自动选择 iOS 或 Pixel */
    Auto,
    /** iOS 极简风格 */
    iOS,
    /** Pixel (Material You) 原生风格 */
    Pixel
}

// ═════════════════════════════════════════════════════════════════════════════════
// MorphColorPalette — Compose 颜色体系
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 颜色体系。
 *
 * 从 [MorphTokens] 转换而来，同时提供亮色和暗色两套色板。
 * 通过 [LocalMorphColors] 向下传递给所有 Compose 组件。
 */
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
        /** iOS 亮色色板 */
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

        /** iOS 暗色色板 */
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

        /** Pixel (M3) 亮色色板 — 使用 M3 标准色值 */
        fun pixelLight() = MorphColorPalette(
            primary = Color(MorphTokens.Blue500),
            onPrimary = Color(MorphTokens.OnPrimary),
            primaryContainer = Color(MorphTokens.PrimaryContainer),
            onPrimaryContainer = Color(MorphTokens.OnPrimaryContainer),
            surface = Color(0xFFFDFBFF),
            surfaceVariant = Color(0xFFE1E0EC),
            onSurface = Color(0xFF1B1B1F),
            onSurfaceVariant = Color(0xFF46464F),
            outlineVariant = Color(0xFFC7C5D0),
            background = Color(0xFFFFFBFE),
            onBackground = Color(0xFF1B1B1F),
            error = Color(0xFFBA1A1A),
            success = Color(0xFF006E2C),
            warning = Color(0xFF7C5800)
        )

        /** Pixel (M3) 暗色色板 */
        fun pixelDark() = MorphColorPalette(
            primary = Color(0xFFA9C7FF),
            onPrimary = Color(0xFF003062),
            primaryContainer = Color(0xFF00468A),
            onPrimaryContainer = Color(0xFFD1E4FF),
            surface = Color(0xFF1B1B1F),
            surfaceVariant = Color(0xFF46464F),
            onSurface = Color(0xFFE3E2E9),
            onSurfaceVariant = Color(0xFFC7C5D0),
            outlineVariant = Color(0xFF46464F),
            background = Color(0xFF1B1B1F),
            onBackground = Color(0xFFE3E2E9),
            error = Color(0xFFFFB4AB),
            success = Color(0xFF7CDA9B),
            warning = Color(0xFFFFB870)
        )
    }
}

// ═════════════════════════════════════════════════════════════════════════════════
// MorphShape — Compose 形状体系
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 形状体系。
 *
 * 从 [MorphTokens] 转换而来，提供 iOS 和 Pixel 两套圆角规范。
 * 通过 [LocalMorphShape] 向下传递给所有 Compose 组件。
 */
@Immutable
data class MorphShape(
    /** 按钮圆角 */
    val cornerRadiusButton: Int,
    /** 卡片圆角 */
    val cornerRadiusCard: Int,
    /** 输入框圆角 */
    val cornerRadiusTextField: Int,
    /** 小圆角 */
    val cornerRadiusSmall: Int,
    /** 中圆角 */
    val cornerRadiusMedium: Int,
    /** 大圆角 */
    val cornerRadiusLarge: Int
) {
    companion object {
        /** iOS 形状规范 */
        fun ios() = MorphShape(
            cornerRadiusButton = MorphTokens.CornerRadiusButtonIOS,
            cornerRadiusCard = MorphTokens.CornerRadiusCardIOS,
            cornerRadiusTextField = MorphTokens.CornerRadiusTextFieldIOS,
            cornerRadiusSmall = MorphTokens.CornerRadiusSmall,
            cornerRadiusMedium = MorphTokens.CornerRadiusMedium,
            cornerRadiusLarge = MorphTokens.CornerRadiusLarge
        )

        /** Pixel (M3) 形状规范 */
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

/**
 * MorphKit Compose 交互模式。
 *
 * 与 View 体系的 [com.morphkit.engine.MorphButton.InteractionMode] 对应，
 * 决定 Compose 组件的触控反馈行为。
 */
enum class MorphInteractionMode {
    /** iOS 风格：按压整体变色，无涟漪 */
    IOS,
    /** Material 风格：保留系统 Ripple 涟漪 */
    MATERIAL
}

// ═════════════════════════════════════════════════════════════════════════════════
// CompositionLocal 提供者
// ═════════════════════════════════════════════════════════════════════════════════

/** MorphKit 颜色 CompositionLocal */
val LocalMorphColors = staticCompositionLocalOf { MorphColorPalette.iosLight() }

/** MorphKit 形状 CompositionLocal */
val LocalMorphShape = staticCompositionLocalOf { MorphShape.ios() }

/** MorphKit 交互模式 CompositionLocal */
val LocalMorphInteractionMode = staticCompositionLocalOf { MorphInteractionMode.IOS }

/** MorphKit 当前风格 CompositionLocal */
val LocalMorphStyle = staticCompositionLocalOf { MorphStyle.Auto }

// ═════════════════════════════════════════════════════════════════════════════════
// MorphTheme Composable — 主题包裹器
// ═════════════════════════════════════════════════════════════════════════════════

/**
 * MorphKit Compose 主题包裹器。
 *
 * 在 `setContent` 最外层调用，为所有子 Composable 提供 MorphKit 设计 Token。
 *
 * ## 使用方式
 *
 * ```kotlin
 * setContent {
 *     MorphTheme(themeStyle = MorphStyle.Auto) {
 *         // 所有业务 UI
 *         MorphButton(text = "确认", onClick = { ... })
 *     }
 * }
 * ```
 *
 * ## Auto 模式判定逻辑
 *
 * ```
 * isSystemInDarkTheme()
 *   ├─ 亮色 → iOS: iosLight() / Pixel: pixelLight()
 *   └─ 暗色 → iOS: iosDark() / Pixel: pixelDark()
 * ```
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

    // 根据风格和暗黑模式选择色板
    val resolvedStyle = resolveStyle(themeStyle)
    val colors = when (resolvedStyle) {
        MorphStyle.iOS -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
        MorphStyle.Pixel -> if (isDark) MorphColorPalette.pixelDark() else MorphColorPalette.pixelLight()
        MorphStyle.Auto -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
    }

    // 根据风格选择形状
    val shape = when (resolvedStyle) {
        MorphStyle.iOS -> MorphShape.ios()
        MorphStyle.Pixel -> MorphShape.pixel()
        MorphStyle.Auto -> MorphShape.ios()
    }

    // 根据风格选择交互模式
    val interactionMode = when (resolvedStyle) {
        MorphStyle.iOS -> MorphInteractionMode.IOS
        MorphStyle.Pixel -> MorphInteractionMode.MATERIAL
        MorphStyle.Auto -> MorphInteractionMode.IOS
    }

    // 构建 MorphKit 排版
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

/**
 * 解析最终风格。
 *
 * Auto 模式下默认使用 iOS 风格（与 View 体系的 MorphStyleResolver 保持一致）。
 */
@Composable
private fun resolveStyle(style: MorphStyle): MorphStyle = style

/**
 * 构建 MorphKit Compose 排版体系。
 *
 * 从 [MorphTokens] 的字号 Token 转换为 Compose [Typography]。
 */
private fun morphTypography(): Typography {
    val defaultFontFamily = FontFamily.SansSerif

    return Typography(
        displayLarge = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = MorphTokens.FontSizeLargeTitle.sp
        ),
        displayMedium = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = MorphTokens.FontSizeTitle1.sp
        ),
        displaySmall = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = MorphTokens.FontSizeTitle2.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = MorphTokens.FontSizeTitle3.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = MorphTokens.FontSizeHeadline.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = MorphTokens.FontSizeHeadline.sp
        ),
        titleLarge = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = MorphTokens.FontSizeTitle3.sp
        ),
        titleMedium = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = MorphTokens.FontSizeHeadline.sp
        ),
        titleSmall = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeSubheadline.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeBody.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeCallout.sp
        ),
        bodySmall = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeSubheadline.sp
        ),
        labelLarge = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeButton.sp
        ),
        labelMedium = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeCaption1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = defaultFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = MorphTokens.FontSizeCaption2.sp
        )
    )
}

/**
 * 将 MorphColorPalette 转换为 Material3 ColorScheme。
 *
 * 确保 Material3 内置组件（如 TopAppBar、NavigationBar 等）也能使用 MorphKit 色板。
 */
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
