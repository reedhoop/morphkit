package com.morphkit.theme.compose

import android.content.Context
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.morphkit.core.InteractionMode
import com.morphkit.core.StylePolicy
import com.morphkit.theme.MorphStyleResolver
import com.morphkit.theme.MorphTokens

/**
 * MorphKit Compose 主题系统。
 *
 * 从 [MorphTokens] 和 Context Theme 读取设计变量，转换为 Compose 的 [androidx.compose.ui.graphics.Color]、[Typography]，
 * 通过 [CompositionLocalProvider] 向下传递，确保 Compose 组件与 View 体系视觉完全一致。
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
// CompositionLocal
// ═════════════════════════════════════════════════════════════════════════════════

private val defaultIosLightPalette: MorphColorPalette = MorphColorPalette.iosLight()
val LocalMorphColors = staticCompositionLocalOf { defaultIosLightPalette }

private val defaultIosShape: MorphShape = MorphShape.ios()
val LocalMorphShape = staticCompositionLocalOf { defaultIosShape }

/** 当前交互模式（IOS / MATERIAL），由 MorphTheme 根据策略提供 */
val LocalMorphInteractionMode = staticCompositionLocalOf { InteractionMode.IOS }

/** 当前风格策略，由 MorphTheme 根据配置提供 */
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
    // 使用 remember 避免每次重组分配新实例
    val colors = remember(resolvedStyle, isDark) {
        when (resolvedStyle) {
            StylePolicy.IOS -> if (isDark) MorphColorPalette.iosDark() else MorphColorPalette.iosLight()
            StylePolicy.PIXEL -> MorphColorPalette.pixelFromContext(context, isDark)
            StylePolicy.AUTO -> MorphColorPalette.iosLight() // unreachable, for exhaustiveness
        }
    }

    val shape = remember(resolvedStyle) {
        when (resolvedStyle) {
            StylePolicy.IOS -> MorphShape.ios()
            StylePolicy.PIXEL -> MorphShape.pixel()
            StylePolicy.AUTO -> MorphShape.ios() // unreachable
        }
    }

    val interactionMode = when (resolvedStyle) {
        StylePolicy.IOS -> InteractionMode.IOS
        StylePolicy.PIXEL -> InteractionMode.MATERIAL
        StylePolicy.AUTO -> InteractionMode.IOS // unreachable
    }

    val typography = remember { morphTypography() }

    CompositionLocalProvider(
        LocalMorphColors provides colors,
        LocalMorphShape provides shape,
        LocalMorphInteractionMode provides interactionMode,
        LocalMorphStylePolicy provides resolvedStyle
    ) {
        MaterialTheme(
            colorScheme = remember(colors) { material3ColorScheme(colors) },
            typography = typography,
            content = content
        )
    }
}

/**
 * Compose-only 主题入口 — 不依赖 Android Context。
 *
 * 直接传入 [MorphColorPalette] 和 [MorphShape]，适用于：
 * - 纯 Compose 环境（无 Activity Context）
 * - 预览（@Preview）
 * - 测试环境（Robolectric 等 Context Theme 不完整）
 * - 自定义皮肤（非 iOS/Pixel 内置风格）
 *
 * @param colors 颜色色板
 * @param shape 形状体系，默认根据交互模式自动选择
 * @param interactionMode 交互模式，默认 [InteractionMode.IOS]
 * @param content 子 Composable 内容
 */
@Composable
fun MorphTheme(
    colors: MorphColorPalette,
    shape: MorphShape = if (LocalMorphInteractionMode.current == InteractionMode.MATERIAL) MorphShape.pixel() else MorphShape.ios(),
    interactionMode: InteractionMode = InteractionMode.IOS,
    content: @Composable () -> Unit
) {
    val typography = remember { morphTypography() }
    val stylePolicy = if (interactionMode == InteractionMode.MATERIAL) StylePolicy.PIXEL else StylePolicy.IOS

    CompositionLocalProvider(
        LocalMorphColors provides colors,
        LocalMorphShape provides shape,
        LocalMorphInteractionMode provides interactionMode,
        LocalMorphStylePolicy provides stylePolicy
    ) {
        MaterialTheme(
            colorScheme = remember(colors) { material3ColorScheme(colors) },
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
        Log.d("MorphKit", "resolveAutoStyle: MorphStyleResolver 解析失败，降级为 IOS", e)
        StylePolicy.IOS  // 降级：解析失败时默认 IOS
    }
}

private fun morphTypography(): Typography {
    val ff = FontFamily.SansSerif
    return Typography(
        displayLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.Typography.fontSizeLargeTitle.sp),
        displayMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.ExtraBold, fontSize = MorphTokens.Typography.fontSizeTitle1.sp),
        displaySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.Typography.fontSizeTitle2.sp),
        headlineLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.Typography.fontSizeTitle3.sp),
        headlineMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.Typography.fontSizeHeadline.sp),
        headlineSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.Typography.fontSizeSubheadline.sp),
        titleLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Bold, fontSize = MorphTokens.Typography.fontSizeTitle3.sp),
        titleMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.SemiBold, fontSize = MorphTokens.Typography.fontSizeHeadline.sp),
        titleSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeSubheadline.sp),
        bodyLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeBody.sp),
        bodyMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeCallout.sp),
        bodySmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeSubheadline.sp),
        labelLarge = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeButton.sp),
        labelMedium = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeCaption1.sp),
        labelSmall = TextStyle(fontFamily = ff, fontWeight = FontWeight.Medium, fontSize = MorphTokens.Typography.fontSizeCaption2.sp)
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
