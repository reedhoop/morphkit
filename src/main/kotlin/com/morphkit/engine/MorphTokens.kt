package com.morphkit.engine

import androidx.annotation.ColorInt

/**
 * MorphKit 统一设计 Token 层。
 *
 * 定义 MorphKit 设计系统的所有原始设计变量，作为 View 体系和 Compose 体系
 * 的**唯一数据源**。View 体系的 [MorphTheme] 和 Compose 体系的
 * [MorphComposeTheme] 均从此处读取 Token，确保双体系视觉完全一致。
 *
 * ## Token 分层架构
 *
 * ```
 * MorphTokens (原始 Token — 本文件)
 *   ├─ MorphTheme (View 体系) — 转换为 Int / Typeface / dp
 *   └─ MorphComposeTheme (Compose 体系) — 转换为 Color / Typography / Dp
 * ```
 *
 * ## iOS ↔ M3 语义映射
 *
 * | iOS 语义                   | M3 语义属性              | Token 名称              |
 * |---------------------------|-------------------------|------------------------|
 * | tintColor                 | colorPrimary            | [primary]               |
 * | systemBackground          | colorSurface            | [surface]               |
 * | secondarySystemBackground | colorSurfaceVariant     | [surfaceVariant]        |
 * | separator                 | colorOutlineVariant     | [outlineVariant]        |
 * | label                     | colorOnSurface          | [onSurface]             |
 * | secondaryLabel            | colorOnSurfaceVariant   | [onSurfaceVariant]      |
 *
 * @see MorphTheme View 体系设计系统
 * @see MorphComposeTheme Compose 体系设计系统
 */
object MorphTokens {

    // ═══════════════════════════════════════════════════════════════════════
    // 颜色 Token — iOS Human Interface 色板 + M3 语义色
    // ═══════════════════════════════════════════════════════════════════════

    // ── 主色系 ──

    /** iOS 蓝 #007AFF / M3 Primary */
    @ColorInt
    val Blue500: Int = 0xFF007AFF.toInt()

    /** iOS 蓝色变体（浅色） */
    @ColorInt
    val Blue100: Int = 0xFFD1E8FF.toInt()

    /** iOS 蓝色变体（深色） */
    @ColorInt
    val Blue700: Int = 0xFF0055CC.toInt()

    // ── 语义色系（M3 对齐） ──

    /** 主色容器色 */
    @ColorInt
    val PrimaryContainer: Int = 0xFFD1E8FF.toInt()

    /** 主色上的文字/图标色 */
    @ColorInt
    val OnPrimary: Int = 0xFFFFFFFF.toInt()

    /** 主色容器上的文字/图标色 */
    @ColorInt
    val OnPrimaryContainer: Int = 0xFF001D3F.toInt()

    /** 表面色（页面背景） */
    @ColorInt
    val Surface: Int = 0xFFF2F2F7.toInt()

    /** 暗色模式表面色 */
    @ColorInt
    val SurfaceDark: Int = 0xFF000000.toInt()

    /** 表面变体色（分组背景、卡片） */
    @ColorInt
    val SurfaceVariant: Int = 0xFFE5E5EA.toInt()

    /** 暗色模式表面变体色 */
    @ColorInt
    val SurfaceVariantDark: Int = 0xFF1C1C1E.toInt()

    /** 表面上的文字/图标色 */
    @ColorInt
    val OnSurface: Int = 0xFF1C1C1E.toInt()

    /** 暗色模式表面上的文字/图标色 */
    @ColorInt
    val OnSurfaceDark: Int = 0xFFE5E5EA.toInt()

    /** 表面变体上的文字/图标色 */
    @ColorInt
    val OnSurfaceVariant: Int = 0xFF3C3C43.toInt()

    /** 暗色模式表面变体上的文字/图标色 */
    @ColorInt
    val OnSurfaceVariantDark: Int = 0xFFC7C7CC.toInt()

    /** 轮廓变体色（分割线、边框） */
    @ColorInt
    val OutlineVariant: Int = 0xFFC7C7CC.toInt()

    /** 暗色模式轮廓变体色 */
    @ColorInt
    val OutlineVariantDark: Int = 0xFF3C3C43.toInt()

    /** 背景色 */
    @ColorInt
    val Background: Int = 0xFFFFFFFF.toInt()

    /** 暗色模式背景色 */
    @ColorInt
    val BackgroundDark: Int = 0xFF000000.toInt()

    /** iOS 系统红色（destructive 操作） */
    @ColorInt
    val Red500: Int = 0xFFFF3B30.toInt()

    /** iOS 系统绿色（成功状态） */
    @ColorInt
    val Green500: Int = 0xFF34C759.toInt()

    /** iOS 系统橙色（警告） */
    @ColorInt
    val Orange500: Int = 0xFFFF9500.toInt()

    // ═══════════════════════════════════════════════════════════════════════
    // 形状 Token — 圆角半径（dp 值，由各体系自行转换为 px / Dp）
    // ═══════════════════════════════════════════════════════════════════════

    /** iOS 风格按钮圆角 */
    const val CornerRadiusButtonIOS = 12

    /** Pixel (M3) 风格按钮圆角 */
    const val CornerRadiusButtonPixel = 8

    /** iOS 风格卡片圆角 */
    const val CornerRadiusCardIOS = 16

    /** Pixel (M3) 风格卡片圆角 */
    const val CornerRadiusCardPixel = 12

    /** iOS 风格输入框圆角 */
    const val CornerRadiusTextFieldIOS = 12

    /** Pixel (M3) 风格输入框圆角 */
    const val CornerRadiusTextFieldPixel = 8

    /** 小圆角 */
    const val CornerRadiusSmall = 8

    /** 中圆角 */
    const val CornerRadiusMedium = 12

    /** 大圆角 */
    const val CornerRadiusLarge = 16

    // ═══════════════════════════════════════════════════════════════════════
    // 排版 Token — 字号（sp）与字重
    // ═══════════════════════════════════════════════════════════════════════

    /** 大标题字号 */
    const val FontSizeLargeTitle = 34f

    /** 标题 1 字号 */
    const val FontSizeTitle1 = 28f

    /** 标题 2 字号 */
    const val FontSizeTitle2 = 22f

    /** 标题 3 字号 */
    const val FontSizeTitle3 = 20f

    /** 标题字号 */
    const val FontSizeHeadline = 17f

    /** 正文字号 */
    const val FontSizeBody = 17f

    /** 副标题字号 */
    const val FontSizeCallout = 16f

    /** 小标题字号 */
    const val FontSizeSubheadline = 15f

    /** 脚注字号 */
    const val FontSizeFootnote = 13f

    /** 说明文字 1 字号 */
    const val FontSizeCaption1 = 12f

    /** 说明文字 2 字号 */
    const val FontSizeCaption2 = 11f

    /** 按钮字号 */
    const val FontSizeButton = 16f

    // ═══════════════════════════════════════════════════════════════════════
    // 交互 Token
    // ═══════════════════════════════════════════════════════════════════════

    /** iOS 按压遮罩最大透明度 */
    const val PressOverlayMaxAlpha = 0.2f

    /** iOS 按压进入动画时长（毫秒） */
    const val PressInDuration = 150L

    /** iOS 按压退出动画时长（毫秒） */
    const val PressOutDuration = 200L

    /** 性能警告阈值（毫秒） */
    const val PerfThresholdMs = 5L

    /** 禁用态不透明度 */
    const val DisabledAlpha = 0.38f
}
